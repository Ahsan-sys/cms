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
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Component
public class JwtUtil {
    @Autowired
    private UserService userService;

    @Value("${jwt.secret.key}")
    private String SECRET_KEY;

    @Value("${app.name}")
    private String APP_NAME;

    @Value("${access.token.expiry.time}")
    private String ACCESS_TOKEN_EXPIRY_TIME;

    private SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS256;

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

    public Claims extractAllClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Boolean isTokenExpired(String token){ return extractExpiration(token).before(new Date()); }

    public String generateAccessToken(String userId,String email,String role){
        Map<String,Object> claims = new HashMap<>();
        claims.put("userId",userId);
        claims.put("role",role);
        claims.put("email",email);

        return Jwts.builder()
                .setIssuer(APP_NAME)
                .setClaims(claims)
                .setSubject(userId)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(createExpiry(Long.valueOf(ACCESS_TOKEN_EXPIRY_TIME)))
                .signWith(SIGNATURE_ALGORITHM,SECRET_KEY).compact();
    }

    public String generateRefreshToken(String userId){
        return Jwts.builder()
                .setIssuer(APP_NAME)
                .setSubject(userId)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .signWith(SIGNATURE_ALGORITHM,SECRET_KEY).compact();
    }

    public Date createExpiry(Long expiryTime){
        return new Date(new Date().getTime() + (expiryTime*86400*1000));
    }


}
