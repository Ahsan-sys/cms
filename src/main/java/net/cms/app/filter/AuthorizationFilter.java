package net.cms.app.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import net.cms.app.response.GenericResponse;
import net.cms.app.service.UserService;
import net.cms.app.utility.CommonMethods;
import net.cms.app.utility.JwtUtil;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
public class AuthorizationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil = new JwtUtil();

    @Autowired
    private UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestPath = request.getServletPath();
        String requestMethod = request.getMethod();
        GenericResponse errorRsp = new GenericResponse();

        if(requestPath.startsWith("/api/admin") || requestPath.startsWith("/api/cms")){
            if(requestPath.startsWith("/api/cms/refresh_token")){
                String refreshToken = CommonMethods.parseNullString(request.getHeader("Refresh-Token"));
                if(refreshToken.isEmpty()){
                    errorRsp.setStatus(0);
                    errorRsp.setMessage("Refresh-token missing");
                }else{
                    if(jwtUtil.isTokenExpired(refreshToken)){
                        errorRsp.setStatus(0);
                        errorRsp.setMessage("Refresh-token is expired");
                    }else{
                        try {
                            JSONObject validJson = jwtUtil.validateToken(refreshToken,"refresh_token",userService);
                            if(!validJson.getBoolean("isValid")){
                                errorRsp.setStatus(0);
                                errorRsp.setMessage("Invalid user for token refresh");
                            }
                        } catch (Exception e) {
                            log.debug(e.getMessage() + " || Trace: "+e.getStackTrace()[0]+ " || "+e.getStackTrace()[1]);
                            errorRsp.setStatus(0);
                            errorRsp.setMessage(e.getMessage());
                        }
                    }
                }
            }else{
                String accessToken = CommonMethods.parseNullString(request.getHeader("Access-Token"));
                if(accessToken.isEmpty()){
                    errorRsp.setStatus(0);
                    errorRsp.setMessage("Access-token missing");
                }else{
                    if(jwtUtil.isTokenExpired(accessToken)){
                        errorRsp.setStatus(0);
                        errorRsp.setMessage("Access-token is expired");
                    }else{
                        try {
                            JSONObject validJson = jwtUtil.validateToken(accessToken,"access_token",userService);
                            if(!validJson.getBoolean("isValid")){
                                errorRsp.setStatus(0);
                                errorRsp.setMessage("Invalid User");
                            }else{
                                String role = jwtUtil.extractUserRole(accessToken);
                                Boolean userAuthorized = userService.checkUrlValidity(role,requestPath,requestMethod);
                                if(!userAuthorized){
                                    errorRsp.setStatus(0);
                                    errorRsp.setMessage("Unauthorized User");
                                }
                            }
                        } catch (Exception e) {
                            log.debug(e.getMessage() + " || Trace: "+e.getStackTrace()[0]+ " || "+e.getStackTrace()[1]);
                            errorRsp.setStatus(0);
                            errorRsp.setMessage(e.getMessage());
                        }
                    }
                }
            }
        }else{
            filterChain.doFilter(request, response);
        }

        if(errorRsp.getStatus()==1){
            new ObjectMapper().writeValue(response.getOutputStream(),errorRsp);
        }else{
            filterChain.doFilter(request, response);
        }
    }
}
