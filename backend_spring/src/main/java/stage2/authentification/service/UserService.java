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

import stage2.authentification.controller.UserController.LoginRequest;
import stage2.authentification.controller.UserController.SignupRequest;
import stage2.authentification.entity.User;
import stage2.authentification.repository.UserRepository;
import stage2.authentification.security.JwtUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public Map<String, Object> register(final SignupRequest request) {
        if (existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email déjà utilisé");
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");

        User savedUser = userRepository.save(user);

        Map<String, Object> userResponse = new HashMap<>();
        userResponse.put("id", savedUser.getId());
        userResponse.put("email", savedUser.getEmail());
        userResponse.put("firstName", savedUser.getFirstName());
        userResponse.put("lastName", savedUser.getLastName());
        userResponse.put("role", savedUser.getRole());

        Map<String, Object> response = new HashMap<>();
        response.put("user", userResponse);
        response.put("expiresIn", 36000); // en secondes
        return response;
    }
    @Autowired
    private UserDetailsService userDetailsService;

    public Map<String, Object> refreshToken(String refreshToken) {
        String username = jwtUtil.extractUsername(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (jwtUtil.validateToken(refreshToken, userDetails)) {
            String newAccessToken = jwtUtil.generateToken(userDetails);

            Map<String, Object> response = new HashMap<>();
            response.put("token", newAccessToken);
            response.put("expiresIn", 900); // 15 minutes
            return response;
        } else {
            throw new BadCredentialsException("Token invalide ou expiré");
        }
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
            String refreshToken = jwtUtil.generateRefreshToken(userDetails);

            Map<String, Object> response = new HashMap<>();
            response.put("token", accessToken);
            response.put("refreshToken", refreshToken);
            response.put("expiresIn", 900); // 15 min

            return response;
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Identifiants invalides");
        }
    }
}
