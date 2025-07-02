package stage.authentification.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import stage.authentification.dto.AuthResponse;
import stage.authentification.entity.User;
import stage.authentification.security.JwtUtil;
import stage.authentification.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class UserController {
    
    private static final String MESSAGE_KEY = "message";
    private static final String SERVER_ERROR_MESSAGE = "Erreur serveur";
    private static final String INVALID_AUTH_HEADER = "Missing or invalid Authorization header";
    
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final UserDetailsService userDetailsService;

    public UserController(JwtUtil jwtUtil, UserService userService, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/register")
    public ResponseEntity<Object> registerUser(@RequestBody SignupRequest signupRequest) {
        try {
            return ResponseEntity.ok(userService.register(signupRequest));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(MESSAGE_KEY, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(Map.of(MESSAGE_KEY, SERVER_ERROR_MESSAGE));
        }
    }

    public static class CreateUserRequest {
        private String firstName;
        private String lastName;
        private String email;
        private String password;
        private String role;

        // Getters and setters
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }

    @PostMapping("/create")
    public ResponseEntity<Object> createUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody CreateUserRequest request) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(MESSAGE_KEY, INVALID_AUTH_HEADER));
            }

            String token = authHeader.replace("Bearer ", "");
            String username = jwtUtil.extractUsername(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            boolean isValidToken = jwtUtil.validateToken(token, userDetails);
            if (!isValidToken) {
                throw new BadCredentialsException("Invalid or expired token");
            }

            Map<String, Object> response = new HashMap<>();
            response.put(MESSAGE_KEY, "User received successfully (test mode)");
            response.put("user", request);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(MESSAGE_KEY, "Unauthorized: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody LoginRequest loginRequest) {
        try {
            return ResponseEntity.ok(userService.login(loginRequest));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(Map.of(MESSAGE_KEY, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(Map.of(MESSAGE_KEY, SERVER_ERROR_MESSAGE));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<Object> refresh(@RequestBody Map<String, String> payload) {
        try {
            String refreshToken = payload.get("token");
            AuthResponse response = userService.refreshToken(refreshToken);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(Map.of(MESSAGE_KEY, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(Map.of(MESSAGE_KEY, SERVER_ERROR_MESSAGE));
        }
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    public static class SignupRequest {
        private String firstName;
        private String lastName;
        private String email;
        private String password;

        public SignupRequest(String firstName, String lastName, String email, String password) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.password = password;
        }

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class LoginRequest {
        private String email;
        private String password;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}