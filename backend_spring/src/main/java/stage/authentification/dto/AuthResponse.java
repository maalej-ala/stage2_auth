package stage.authentification.dto;

public class AuthResponse {
    private String token;
    private String refreshToken;
    private UserDto user;
    private int expiresIn;

    public AuthResponse(String token, String refreshToken, UserDto user, int expiresIn) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.user = user;
        this.expiresIn = expiresIn;
    }

    // Getters
    public String getToken() { return token; }
    public String getRefreshToken() { return refreshToken; }
    public UserDto getUser() { return user; }
    public int getExpiresIn() { return expiresIn; }
}