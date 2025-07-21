// UserController.java
package stage.authentification.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public ResponseEntity<Object> registerUser(@RequestBody User signupRequest) {
        try {
            return ResponseEntity.ok(userService.register(signupRequest));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(MESSAGE_KEY, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(Map.of(MESSAGE_KEY, SERVER_ERROR_MESSAGE));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<Object> createUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody User request) {
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
    public ResponseEntity<Object> login(@RequestBody User loginRequest) {
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
    @PreAuthorize("hasRole('ADMIN')")

    public ResponseEntity<List<User>> getAllUsers(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(null);
            }

            String token = authHeader.replace("Bearer ", "");
            String username = jwtUtil.extractUsername(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            boolean isValidToken = jwtUtil.validateToken(token, userDetails);
            if (!isValidToken) {
                throw new BadCredentialsException("Invalid or expired token");
            }

            return ResponseEntity.ok(userService.getAllUsers());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(null);
        }
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<Object> updateUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody User updateRequest) {
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

            User updatedUser = userService.updateUser(id, updateRequest);
            Map<String, Object> response = new HashMap<>();
            response.put(MESSAGE_KEY, "User updated successfully");
            response.put("user", updatedUser);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(MESSAGE_KEY, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(MESSAGE_KEY, SERVER_ERROR_MESSAGE));
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Object> deleteUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
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

            userService.deleteUser(id);
            return ResponseEntity.ok(Map.of(MESSAGE_KEY, "User deleted successfully"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(MESSAGE_KEY, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(MESSAGE_KEY, SERVER_ERROR_MESSAGE));
        }
    }
}