package stage.authentification.service;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import stage.authentification.dto.AuthResponse;
import stage.authentification.dto.UserDto;
import stage.authentification.dto.UserResponse;
import stage.authentification.entity.User;
import stage.authentification.exception.AuthenticationException;
import stage.authentification.repository.UserRepository;
import stage.authentification.security.JwtUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private static final int TOKEN_EXPIRATION = 900;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final EmailService emailService;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtUtil jwtUtil,
            UserDetailsService userDetailsService,
            EmailService emailService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.emailService = emailService;
    }
    @PreAuthorize("hasRole('ADMIN')")
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
            user.getRole(),
            user.isActive()
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
            "USER",
            false // Inactive until admin activation
        );

        User savedUser = userRepository.save(user);

        try {
            emailService.sendRegistrationPendingEmail(savedUser.getEmail(), savedUser.getFirstName());
            logger.info("Registration pending email sent to {}", savedUser.getEmail());
        } catch (MessagingException e) {
            logger.error("Failed to send registration pending email to {}: {}", savedUser.getEmail(), e.getMessage());
            // Do not block registration
        }

        return new AuthResponse(
            null, // No access token until activated
            null, // No refresh token until activated
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
            User user = findByEmail(loginRequest.getEmail());
            if (!user.isActive()) {
                throw new BadCredentialsException("Votre compte est désactivé. Veuillez attendre l'activation par un administrateur ou contactez support@yourdomain.com.");
            }

            String accessToken = jwtUtil.generateToken(userDetails);
            String refreshToken = jwtUtil.generateRefreshToken(userDetails);
            return new AuthResponse(
                accessToken,
                refreshToken,
                createUserDto(user),
                TOKEN_EXPIRATION
            );
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException(e.getMessage());
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
            throw e;
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
            request.getRole(),
            request.isActive()
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

    @PreAuthorize("hasRole('ADMIN')")
    public User updateUser(Long id, User updateRequest) {
        Optional<User> existingUser = userRepository.findById(id);
        if (existingUser.isEmpty()) {
            throw new IllegalArgumentException("Utilisateur introuvable avec l'ID : " + id);
        }

        User user = existingUser.get();
        boolean activeChangedToTrue = !user.isActive() && updateRequest.isActive();
        boolean activeChangedToFalse = user.isActive() && !updateRequest.isActive();

        user.setFirstName(updateRequest.getFirstName());
        user.setLastName(updateRequest.getLastName());
        user.setEmail(updateRequest.getEmail());
        user.setActive(updateRequest.isActive());

        if (updateRequest.getPassword() != null && !updateRequest.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(updateRequest.getPassword()));
        }

        user.setRole(updateRequest.getRole() != null ? updateRequest.getRole() : "USER");
        User savedUser = userRepository.save(user);

        if (activeChangedToTrue) {
            try {
                emailService.sendAccountActivatedEmail(savedUser.getEmail(), savedUser.getFirstName());
                logger.info("Account activated email sent to {}", savedUser.getEmail());
            } catch (MessagingException e) {
                logger.error("Failed to send account activated email to {}: {}", savedUser.getEmail(), e.getMessage());
                // Do not block update
            }
        } else if (activeChangedToFalse) {
            try {
                emailService.sendAccountDeactivatedEmail(savedUser.getEmail(), savedUser.getFirstName());
                logger.info("Account deactivated email sent to {}", savedUser.getEmail());
            } catch (MessagingException e) {
                logger.error("Failed to send account deactivated email to {}: {}", savedUser.getEmail(), e.getMessage());
                // Do not block update
            }
        }

        return savedUser;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("Utilisateur introuvable avec l'ID : " + id);
        }

        userRepository.deleteById(id);
    }
}