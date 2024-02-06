package net.cms.app.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cms.app.entity.User;
import net.cms.app.response.GenericResponse;
import net.cms.app.utility.CommonMethods;
import net.cms.app.utility.JwtUtil;
import net.cms.app.utility.ResponseMessage;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService{
    @Autowired
    private JdbcTemplate jdbc;

    public User findByEmail(String email) {
        User user = new User();
        String query = "select u.*,p.role from users u left join profiles p on p.id=u.profile_id where u.email = ?";
        jdbc.execute(query,(PreparedStatementCallback<Void>) ps->{
            try{
                ps.setString(1,email);
                ResultSet rs = ps.executeQuery();
                if(rs.next()){
                    user.setUserId(rs.getString("id"));
                    user.setEmail(rs.getString("email"));
                    user.setName(rs.getString("name"));
                    user.setRole(rs.getString("role"));
                    user.setPassword(rs.getString("password"));
                    user.setPhoneNumber(rs.getString("phone_number"));
                }
            }catch(Exception e){
                log.debug(e.getMessage() + " || Trace: "+e.getStackTrace()[0]+ " || "+e.getStackTrace()[1]);
                throw e;
            }
            return null;
        });

        if(user.getEmail() != null){
            return user;
        }else{
            throw new UsernameNotFoundException("User '"+email+"' Not Found");
        }
    }

    public JSONObject findByIdOrEmail(String userId,String email) {
        GenericResponse rsp = new GenericResponse();
        JSONObject user= new JSONObject();

        String query = "select u.*,p.role from users u left join profiles p on p.id=u.profile_id where ";
        if(userId !=null && !userId.isEmpty()){
            query+=" u.id=?";
        }else{
            query+=" u.email=?";
        }
        jdbc.execute(query,(PreparedStatementCallback<Void>) ps->{
            try{
                if(userId !=null && !userId.isEmpty()){
                    ps.setString(1,email);
                }else{
                    ps.setString(1,userId);
                }

                ResultSet rs = ps.executeQuery();
                if(rs.next()){
                    user.put("id",rs.getInt("id"));
                    user.put("uuid",rs.getString("uuid"));
                    user.put("email",rs.getString("email"));
                    user.put("name",rs.getString("name"));
                    user.put("role",rs.getString("role"));
                    user.put("password",rs.getString("password"));
                    user.put("phoneNumber",rs.getString("phone_number"));
                    user.put("isDeleted",rs.getBoolean("is_deleted"));
                    user.put("isActive",rs.getBoolean("is_active"));
                    rsp.setData(user);
                }else {
                    rsp.setMessage(ResponseMessage.USER_NOT_FOUND_ERROR);
                    rsp.setStatus(0);
                }
            }catch(Exception e){
                log.debug(e.getMessage() + " || Trace: "+e.getStackTrace()[0]+ " || "+e.getStackTrace()[1]);
                rsp.setMessage(e.getMessage());
                rsp.setStatus(0);
            }
            return null;
        });

        return rsp.rspToJson();
    }

    public void setToken(int userId,String accessToken,String refreshToken){
        String query = "insert into user_sessions (user_id,access_token,refresh_token) values (?,?,?)";
        jdbc.execute(query,(PreparedStatementCallback<Void>) ps->{
             try{
                 ps.setInt(1,userId);
                 ps.setString(2,accessToken);
                 ps.setString(3,refreshToken);
                 ps.executeUpdate();
             }catch (Exception e){
                 log.debug(e.getMessage() + " || Trace: "+e.getStackTrace()[0]+ " || "+e.getStackTrace()[1]);
             }
             return null;
        });
    }

    public boolean verifyIfLogedin(String userId){
        JwtUtil jwtUtil = new JwtUtil();

        return Boolean.TRUE.equals(jdbc.execute("select * from user_sessions where user_id=?", (PreparedStatementCallback<Boolean>) ps -> {
            try {
                ps.setString(1, userId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String refreshToken = CommonMethods.parseNullString(rs.getString("refresh_token"));
                    if (jwtUtil.isTokenExpired(refreshToken)) {
                        deleteUserSession(jwtUtil.extractUserId(refreshToken));
                        return false;
                    } else {
                        return true;
                    }
                } else {
                    return false;
                }
            } catch (Exception e) {
                log.debug(e.getMessage() + " || Trace: " + e.getStackTrace()[0] + " || " + e.getStackTrace()[1]);
                return true;
            }
        }));
    }

    public void deleteUserSession(String userId){
        int rowsAffected = jdbc.update("delete from user_sessions where user_id=?", userId);
    }

    public Boolean validateUserToken(String userId,String token, String tokenType){
        String query = "select count(*) as count from users u join user_sessions us on us.user_id=u.id where u.id=? and us."+tokenType+"=?";
        return jdbc.execute(query,(PreparedStatementCallback<Boolean>) ps->{
            try{
                ps.setString(1,userId);
                ps.setString(2,token);
                ResultSet rs = ps.executeQuery();

                if(rs.next()) return rs.getInt("count")>0;
                else return false;
            }catch (Exception e){
                log.debug(e.getMessage() + " || Trace: "+e.getStackTrace()[0]+ " || "+e.getStackTrace()[1]);
                return false;
            }
        } );
    }

    public Boolean checkUrlValidity(String role, String url,String method){
        String query = "SELECT COUNT(*) as count FROM profiles p " +
                "LEFT JOIN profile_authorities pa ON p.id = pa.profile_id " +
                "LEFT JOIN urls u ON pa.url_id = u.id " +
                "WHERE p.role = ? AND pa.request_methods LIKE ? AND u.url = ?";

        return jdbc.execute(query,(PreparedStatementCallback<Boolean>) ps->{
            try{
                ps.setString(1,role);
                ps.setString(2,"%"+method+"%");
                ps.setString(3,url);
                ResultSet rs = ps.executeQuery();
                if(rs.next()){
                    return rs.getInt("count")>0;
                }
                else{
                    return false;
                }
            }catch (Exception e){
                log.debug(e.getMessage() + " || Trace: "+e.getStackTrace()[0]+ " || "+e.getStackTrace()[1]);
                return false;
            }
        } );
    }
}
