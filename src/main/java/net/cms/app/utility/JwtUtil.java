package net.cms.app.utility;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import net.cms.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.function.Function;

@Slf4j
@Component
public class JwtUtil {
    @Autowired
    private UserService userService;

    @Value("${jwt.secret.key}")
    private String SECRET_KEY;

    @Value("${app.name}")
    private String appName;

    @Value("${access.token.expiry.time}")
    private String accessTokenExpiryTime;

    private SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    public String extractUserId(String token){
        return extractClaim(token, Claims::getSubject);
    }

    public String extractUserRole(String token){
        Claims claims = extractAllClaims(token);
        return claims.get("role").toString();
    }

    public String extractUserName(String token){
        Claims claims = extractAllClaims(token);
        return claims.get("email").toString();
    }

    public Date extractExpiration(String token){
        return extractClaim(token,Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims,T> claimsResolver){
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
