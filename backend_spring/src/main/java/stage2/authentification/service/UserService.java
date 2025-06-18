package stage2.authentification.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import stage2.authentification.controller.UserController.CreateUserRequest;
import stage2.authentification.controller.UserController.LoginRequest;
import stage2.authentification.controller.UserController.SignupRequest;
import stage2.authentification.entity.User;
import stage2.authentification.repository.UserRepository;
import stage2.authentification.security.JwtUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

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

    // Helper method to create User DTO for response
    private Map<String, Object> createUserResponse(User user) {
        Map<String, Object> userResponse = new HashMap<>();
        userResponse.put("id", user.getId());
        userResponse.put("email", user.getEmail());
        userResponse.put("firstName", user.getFirstName());
        userResponse.put("lastName", user.getLastName());
        userResponse.put("role", user.getRole());
        return userResponse;
    }

    public Map<String, Object> register(final SignupRequest request) {
        if (existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email déjà utilisé");
        }

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");

        User savedUser = userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());
        String accessToken = jwtUtil.generateToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails); // New refresh token with 7-day expiration

        Map<String, Object> response = new HashMap<>();
        response.put("token", accessToken);
        response.put("refreshToken", refreshToken);
        response.put("user", createUserResponse(savedUser));
        response.put("expiresIn", 900); // 15 minutes in seconds

        return response;
    }

    public Map<String, Object> login(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String accessToken = jwtUtil.generateToken(userDetails);
            String refreshToken = jwtUtil.generateRefreshToken(userDetails); // New refresh token with 7-day expiration

            User user = findByEmail(loginRequest.getEmail());

            Map<String, Object> response = new HashMap<>();
            response.put("token", accessToken);
            response.put("refreshToken", refreshToken);
            response.put("user", createUserResponse(user));
            response.put("expiresIn", 900); // 15 minutes

            return response;
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Identifiants invalides");
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'authentification: " + e.getMessage());
        }
    }

    public Map<String, Object> refreshToken(String refreshToken) {
        try {
            String username = jwtUtil.extractUsername(refreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtUtil.validateToken(refreshToken, userDetails)) {
                String newAccessToken = jwtUtil.generateToken(userDetails);
                String newRefreshToken = jwtUtil.generateRefreshToken(userDetails); // New refresh token with 7-day expiration

                User user = findByEmail(username);

                Map<String, Object> response = new HashMap<>();
                response.put("token", newAccessToken);
                response.put("refreshToken", newRefreshToken);
                response.put("user", createUserResponse(user));
                response.put("expiresIn", 900); // 15 minutes

                System.out.println("Refresh response: " + response); // DEBUG
                return response;
            } else {
                throw new BadCredentialsException("Token de rafraîchissement invalide ou expiré");
            }
        } catch (Exception e) {
            System.out.println("Refresh error: " + e.getMessage()); // DEBUG
            throw new BadCredentialsException("Erreur lors du rafraîchissement du token: " + e.getMessage());
        }
    }

    public Map<String, Object> createUser(CreateUserRequest request, String authToken) {
        try {
            String username = jwtUtil.extractUsername(authToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (!jwtUtil.validateToken(authToken, userDetails)) {
                throw new BadCredentialsException("Invalid or expired token");
            }

            // Create new user
            if (existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email déjà utilisé");
            }

            User user = new User();
            user.setId(UUID.randomUUID().toString());
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setRole(request.getRole());

            User savedUser = userRepository.save(user);

            // Generate new tokens for the requesting user
            String newAccessToken = jwtUtil.generateToken(userDetails);
            String newRefreshToken = jwtUtil.generateRefreshToken(userDetails); // New refresh token with 7-day expiration

            Map<String, Object> response = new HashMap<>();
            response.put("message", "User created successfully");
            response.put("user", createUserResponse(savedUser));
            response.put("token", newAccessToken);
            response.put("refreshToken", newRefreshToken);
            response.put("expiresIn", 900); // 15 minutes

            return response;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la création de l'utilisateur: " + e.getMessage());
        }
    }
}