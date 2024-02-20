package net.cms.app.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.cms.app.response.RestResponse;
import net.cms.app.service.UserService;
import net.cms.app.utility.CommonMethods;
import net.cms.app.utility.JwtUtil;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class AuthorizationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestPath = request.getServletPath();
        String requestMethod = request.getMethod();
        RestResponse errorRsp = new RestResponse();

        if(requestPath.startsWith("/api/admin") || requestPath.startsWith("/api/cms")){
            if(requestPath.startsWith("/api/cms/refresh_token")){
                String refreshToken = CommonMethods.parseNullString(request.getHeader("Refresh-Token"));
                if(refreshToken.isEmpty()){
                    errorRsp.setStatus(0);
                    errorRsp.setMessage("Refresh-token missing");
                }else{
                    try{
                        jwtUtil.isTokenExpired(refreshToken);

                        try {
                            JSONObject validJson = jwtUtil.validateToken(refreshToken,"refresh_token",userService);
                            if(!validJson.getBoolean("isValid")){
                                errorRsp.setStatus(0);
                                errorRsp.setMessage("Invalid user for token refresh");
                            }else{
                                filterChain.doFilter(request, response);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            response.setStatus(403);
                            errorRsp.setStatus(403);
                            errorRsp.setMessage(e.getMessage());
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        response.setStatus(403);
                        errorRsp.setStatus(403);
                        errorRsp.setMessage(e.getMessage());
                    }
                }
            }else{
                String accessToken = CommonMethods.parseNullString(request.getHeader("Access-Token"));
                if(accessToken.isEmpty()){
                    response.setStatus(403);
                    errorRsp.setStatus(403);
                    errorRsp.setMessage("Access-token missing");
                }else{
                    try{
                        jwtUtil.isTokenExpired(accessToken);
                        try {
                            JSONObject validJson = jwtUtil.validateToken(accessToken,"access_token",userService);
                            if(!validJson.getBoolean("isValid")){
                                errorRsp.setStatus(0);
                                errorRsp.setMessage("Invalid User");
                            }else{
                                String role = jwtUtil.extractUserRole(accessToken);
                                String refinedpath = requestPath;
                                if(requestPath.split("/").length>4){
                                    refinedpath = "/"+requestPath.split("/")[1]+"/"+requestPath.split("/")[2]+"/"+requestPath.split("/")[3];
                                }
                                Boolean userAuthorized = userService.checkUrlValidity(role,refinedpath,requestMethod);
                                if(!userAuthorized){
                                    errorRsp.setStatus(0);
                                    errorRsp.setMessage("Unauthorized User");
                                }else{
                                    filterChain.doFilter(request, response);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            response.setStatus(403);
                            errorRsp.setStatus(403);
                            errorRsp.setMessage(e.getMessage());
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        response.setStatus(403);
                        errorRsp.setStatus(403);
                        errorRsp.setMessage(e.getMessage());
                    }
                }
            }
        }else{
            filterChain.doFilter(request, response);
        }
        new ObjectMapper().writeValue(response.getOutputStream(),errorRsp);
    }
}
