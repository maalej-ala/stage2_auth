package stage.authentification.dto;

public class UserResponse {
    private String message;
    private UserDto user;
    private String token;
    private String refreshToken;
    private int expiresIn;

    public UserResponse(String message, UserDto user, String token, String refreshToken, int expiresIn) {
        this.message = message;
        this.user = user;
        this.token = token;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
    }

    // Getters
    public String getMessage() { return message; }
    public UserDto getUser() { return user; }
    public String getToken() { return token; }
    public String getRefreshToken() { return refreshToken; }
    public int getExpiresIn() { return expiresIn; }
}