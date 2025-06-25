//package stage2.authentification.security;
//
//import io.jsonwebtoken.ExpiredJwtException;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Map;
//
//@ControllerAdvice
//public class JwtExceptionHandler {
//
//    @ExceptionHandler(ExpiredJwtException.class)
//    public ResponseEntity<Map<String, String>> handleExpiredJwtException(ExpiredJwtException ex) {
//        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                .body(Map.of("message", "JWT token expired"));
//    }
//    
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(Map.of("message", "Unexpected error: " + ex.getMessage()));
//    }
//}
