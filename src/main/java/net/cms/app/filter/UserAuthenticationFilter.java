package net.cms.app.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.cms.app.entity.User;
import net.cms.app.response.GenericResponse;
import net.cms.app.service.UserService;
import net.cms.app.utility.JwtUtil;
import net.cms.app.utility.ResponseMessage;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

@Slf4j
@AllArgsConstructor
public class UserAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @SneakyThrows
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if(null == email || email.isEmpty()){
            unsuccessfulAuthentication(request, response,new BadCredentialsException(ResponseMessage.EMAIL_REQUIRED));
        }
        else if(null == password || password.isEmpty()){
            unsuccessfulAuthentication(request, response,new BadCredentialsException(ResponseMessage.PASSWORD_REQUIRED));
        }else{
            log.info("User: "+email+" is trying to login");

            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(email,password);
            return authenticationManager.authenticate(authenticationToken);
        }
        return null;
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        User user = (User) authResult.getPrincipal();

        JSONObject rsp = new JSONObject();

        boolean isAlreadyLoggedIn = userService.verifyIfLogedIn(user.getUserId());
        if(!isAlreadyLoggedIn){
            String accessToken = jwtUtil.generateAccessToken(user.getUserId(),user.getEmail(),user.getRole());
            String refreshToken = jwtUtil.generateRefreshToken(user.getUserId());

            JSONObject userProfile = userService.findByIdOrEmail(user.getUserId(),null);
            if(userProfile.has("data")){
                JSONObject userObj = userProfile.getJSONObject("data");

                rsp.put("message","Signed In Successfully!");
                rsp.put("status",1);
                rsp.put("Access-Token",accessToken);
                rsp.put("Refresh-Token",refreshToken);
                rsp.put("user",userObj);

                userService.setToken(userObj.getInt("id"),accessToken,refreshToken);
            }else{
                rsp.put("message",userProfile.getString("message"));
                rsp.put("status",0);
            }
        }else{
            rsp.put("message","User already logged in");
            rsp.put("status",0);
        }

        response.setContentType("application/json");

        new ObjectMapper().writeValue(response.getOutputStream(),rsp.toString());
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        GenericResponse rsp = new GenericResponse();
        rsp.setMessage("Email or password invalid");
        rsp.setStatus(0);

        response.setContentType("application/json");
        response.setStatus(200);

        log.info("Login Failed! ");
        new ObjectMapper().writeValue(response.getOutputStream(),rsp.rspToJson().toString());
        response.getOutputStream().flush();
    }
}
