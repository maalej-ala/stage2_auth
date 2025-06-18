package stage2.authentification.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

@Component
public class JwtUtil {

	private final String SECRET_KEY = "your_secret_key_your_secret_key_123456"; // should be at least 256 bits

	private SecretKey getSigningKey() {
	    byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
	    return Keys.hmacShaKeyFor(keyBytes);
	}

	public String generateToken(UserDetails userDetails) {
	    // Récupère le rôle principal, ou "USER" par défaut si aucun rôle n’est défini
	    String role = userDetails.getAuthorities().stream()
	        .findFirst()
	        .map(GrantedAuthority::getAuthority)
	        .orElse("ROLE_USER");

	    // Crée et retourne le token JWT
	    return Jwts.builder()
	        .setSubject(userDetails.getUsername())
	        .claim("role", role)
	        .setIssuedAt(new Date(System.currentTimeMillis()))
	        .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 )) // 15 min
	        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
	        .compact();
	}
	
	public String generateRefreshToken(UserDetails userDetails) {
	    return Jwts.builder()
	        .setSubject(userDetails.getUsername())
	        .setIssuedAt(new Date())
	        .setExpiration(new Date(System.currentTimeMillis() + 1000L * 60 * 2)) // 24 hours
	        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
	        .compact();
	}

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public Date extractExpiration(String token) {
        return extractClaims(token).getExpiration();
    }

    private Claims extractClaims(String token) {
        
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

    }


    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}
