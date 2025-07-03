package stage.authentification.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import stage.authentification.dto.AuthResponse;
import stage.authentification.dto.UserDto;
import stage.authentification.dto.UserResponse;
import stage.authentification.entity.User;
import stage.authentification.exception.AuthenticationException;
import stage.authentification.repository.UserRepository;
import stage.authentification.security.JwtUtil;

import java.util.List;

@Service
public class UserService {

    private static final int TOKEN_EXPIRATION = 900;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    // ✅ Constructor Injection
    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtUtil jwtUtil,
            UserDetailsService userDetailsService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable avec l'email : " + email));
    }

    private UserDto createUserDto(User user) {
        return new UserDto(
            user.getId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getRole()
        );
    }

    public AuthResponse register(final User request) {
        if (existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email déjà utilisé");
        }

        User user = new User(
            request.getFirstName(),
            request.getLastName(),
            request.getEmail(),
            passwordEncoder.encode(request.getPassword()),
            "USER"
        );

        User savedUser = userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());
        String accessToken = jwtUtil.generateToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        return new AuthResponse(
            accessToken,
            refreshToken,
            createUserDto(savedUser),
            TOKEN_EXPIRATION
        );
    }

     public AuthResponse login(User loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
                )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String accessToken = jwtUtil.generateToken(userDetails);
            String refreshToken = jwtUtil.generateRefreshToken(userDetails);

            User user = findByEmail(loginRequest.getEmail());

            return new AuthResponse(
                accessToken,
                refreshToken,
                createUserDto(user),
                TOKEN_EXPIRATION
            );
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Identifiants invalides");
        } catch (Exception e) {
            throw new AuthenticationException("Erreur lors de l'authentification", e);
        }
    }

    public AuthResponse refreshToken(String refreshToken) {
        try {
            String username = jwtUtil.extractUsername(refreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            boolean isValid = jwtUtil.validateToken(refreshToken, userDetails);
            if (!isValid) {
                throw new BadCredentialsException("Token de rafraîchissement invalide ou expiré");
            }

            String newAccessToken = jwtUtil.generateToken(userDetails);
            String newRefreshToken = jwtUtil.generateRefreshToken(userDetails);
            User user = findByEmail(username);

            return new AuthResponse(
                newAccessToken,
                newRefreshToken,
                createUserDto(user),
                TOKEN_EXPIRATION
            );
        } catch (BadCredentialsException e) {
            throw e; // still meaningful
        } catch (Exception e) {
            throw new AuthenticationException("Erreur lors du rafraîchissement du token", e);
        }
    }

    public UserResponse createUser(User request, String authToken) {
        String username = jwtUtil.extractUsername(authToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        boolean isValid = jwtUtil.validateToken(authToken, userDetails);
        if (!isValid) {
            throw new BadCredentialsException("Invalid or expired token");
        }


        if (existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email déjà utilisé");
        }

        User user = new User(
            request.getFirstName(),
            request.getLastName(),
            request.getEmail(),
            passwordEncoder.encode(request.getPassword()),
            request.getRole()
        );

        User savedUser = userRepository.save(user);
        String newAccessToken = jwtUtil.generateToken(userDetails);
        String newRefreshToken = jwtUtil.generateRefreshToken(userDetails);

        return new UserResponse(
            "User created successfully",
            createUserDto(savedUser),
            newAccessToken,
            newRefreshToken,
            TOKEN_EXPIRATION
        );
    }
}
