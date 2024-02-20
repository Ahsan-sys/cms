package net.cms.app.utility;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import net.cms.app.service.UserService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
public class JwtUtil {

    @Value("${jwt.secret.key}")
    private String SECRET_KEY;

    @Value("${app.name}")
    private String APP_NAME;

    @Value("${access.token.expiry.time}")
    private String ACCESS_TOKEN_EXPIRY_TIME;

    private final SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS256;



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
        SecretKey secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));

        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenExpired(String token){ return extractExpiration(token).before(new Date()); }

    public String generateAccessToken(String userId,String email,String role){
        return generateAccessToken(userId,email,role,true);
    }
    public String generateAccessToken(String userId,String email,String role,boolean canTokenExpire){
        Map<String,Object> claims = new HashMap<>();
        claims.put("userId",userId);
        claims.put("role",role);
        claims.put("email",email);
        SecretKey secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));

        return Jwts.builder()
                .setIssuer(APP_NAME)
                .setClaims(claims)
                .setSubject(userId)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(createExpiry(Long.valueOf(ACCESS_TOKEN_EXPIRY_TIME),canTokenExpire))
                .signWith(secretKey,SIGNATURE_ALGORITHM).compact();

    }

    public String generateRefreshToken(String userId){
        SecretKey secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));
        return Jwts.builder()
                .setIssuer(APP_NAME)
                .setSubject(userId)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(createExpiry(Long.valueOf(ACCESS_TOKEN_EXPIRY_TIME+2),true))
                .signWith(secretKey,SIGNATURE_ALGORITHM).compact();
    }

    public Date createExpiry(Long expiryTime,boolean canTokenExpire){
        if(!canTokenExpire){
            expiryTime=expiryTime*10000;
        }
        return new Date(System.currentTimeMillis() + expiryTime * 60 * 1000);
    }

    public JSONObject validateToken(String token,String tokenType,UserService userService) throws Exception{
        final String userId = extractUserId(token);

        JSONObject response = new JSONObject();

        try{
            if(userService.validateUserToken(userId,token,tokenType)){
                JSONObject rsp = userService.findByIdOrEmail(userId,null);

                if(rsp.getInt("status") == 1) {
                    JSONObject user = rsp.getJSONObject("data");
                    if(CommonMethods.parseNullInt(user.getInt("id"))>0) {
                        response.put("isValid", true);
                        response.put("userId", user.getInt("id"));
                        response.put("isDeleted", user.getBoolean("isDeleted"));
                        response.put("role", user.getString("role"));
                    }
                }
            }
        }catch (Exception e){
            log.debug(e.getMessage() + " || Trace: "+e.getStackTrace()[0]+ " || "+e.getStackTrace()[1]);
        }

        if(response.isEmpty()){
            response.put("isValid",false);
            response.put("userId",0);
        }

        return response;
    }
}
