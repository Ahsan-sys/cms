package net.cms.app.service;

import lombok.extern.slf4j.Slf4j;
import net.cms.app.entity.User;
import net.cms.app.response.GenericResponse;
import net.cms.app.utility.CommonMethods;
import net.cms.app.utility.JwtUtil;
import net.cms.app.utility.ResponseMessage;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
public class UserService{
    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private ProfileService profileService;

    @Autowired
    JwtUtil jwtUtil;


    private final PasswordEncoder passwordEncoder;

    public UserService(PasswordEncoder passwordEncoder){
        this.passwordEncoder = passwordEncoder;
    }

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
                System.out.println(e.getMessage() + " || Trace: "+e.getStackTrace()[0]+ " || "+e.getStackTrace()[1]);
                throw e;
            }
            return null;
        });

        if(user.getEmail() != null){
            return user;
        }else{
            return null;
        }
    }

    public JSONObject findByIdOrEmail(String userId,String email) {
        GenericResponse rsp = new GenericResponse();
        JSONObject user= new JSONObject();

        String query = "select u.*,p.role,p.id as profile_id from users u left join profiles p on p.id=u.profile_id where ";
        if(!CommonMethods.parseNullString(userId).isEmpty()){
            query+=" u.id=?";
        }else{
            query+=" u.email=?";
        }
        jdbc.execute(query,(PreparedStatementCallback<Void>) ps->{
            try{
                if(!CommonMethods.parseNullString(userId).isEmpty()){
                    ps.setString(1,userId);
                }else{
                    ps.setString(1,email);
                }

                ResultSet rs = ps.executeQuery();
                if(rs.next()){
                    user.put("id",rs.getInt("id"));
                    user.put("uuid",rs.getString("uuid"));
                    user.put("email",rs.getString("email"));
                    user.put("name",rs.getString("name"));
                    user.put("role",rs.getString("role"));
                    user.put("profile_id",rs.getString("profile_id"));
                    if(!CommonMethods.parseNullString(userId).isEmpty() && userId.equals(String.valueOf(rs.getInt("id")))){
                        user.put("password",rs.getString("password"));
                    }
                    user.put("phoneNumber",rs.getString("phone_number"));
                    user.put("isDeleted",rs.getBoolean("is_deleted"));
                    user.put("isActive",rs.getBoolean("is_active"));
                    user.put("urls",profileService.getAllAuhtorizedUrls(rs.getString("profile_id")));
                    rsp.setData(user);
                }else {
                    rsp.setMessage(ResponseMessage.USER_NOT_FOUND_ERROR);
                    rsp.setStatus(0);
                }
            }catch(Exception e){
                System.out.println(e.getMessage() + " || Trace: "+e.getStackTrace()[0]+ " || "+e.getStackTrace()[1]);
                rsp.setMessage(e.getMessage());
                rsp.setStatus(0);
            }
            return null;
        });
        return rsp.rspToJson();
    }

    public void setToken(int userId,String accessToken,String refreshToken){
        deleteUserSession(String.valueOf(userId));
        String query = "insert into user_sessions (user_id,access_token";
        String queryValues ="values (?,?";

        if(!CommonMethods.parseNullString(refreshToken).isEmpty()){
            query += ",refresh_token";
            queryValues += ",?";
        }
        String sql = query+") "+queryValues+")";
        jdbc.execute(sql,(PreparedStatementCallback<Void>) ps->{
             try{
                 ps.setInt(1,userId);
                 ps.setString(2,accessToken);
                 if(!CommonMethods.parseNullString(refreshToken).isEmpty()) {
                     ps.setString(3, refreshToken);
                 }
                 ps.executeUpdate();
             }catch (Exception e){
                 e.printStackTrace();
             }
             return null;
        });
    }


    public boolean verifyIfLogedIn(String userId){
        String accessToken = jdbc.execute("select * from user_sessions where user_id=?", (PreparedStatementCallback<String>) ps -> {
            try {
                ps.setString(1, userId);
                ResultSet rs = ps.executeQuery();
                if(rs.next()){
                    return CommonMethods.parseNullString(rs.getString("access_token"));
                }else{
                    return "";
                }
            } catch (Exception e) {
                System.out.println(Arrays.toString(e.getStackTrace()));
                throw e;
            }
        });

        if(!CommonMethods.parseNullString(accessToken).isEmpty()){
            try{
                jwtUtil.isTokenExpired(accessToken);
                return true;
            }catch (Exception e){
                System.out.println(Arrays.toString(e.getStackTrace()));
                jdbc.update("delete from user_sessions where user_id=?", userId);
                return false;
            }
        }else{
            return false;
        }
    }

    public boolean deleteUserSession(String userId){return jdbc.update("delete from user_sessions where user_id=?", userId)>0;}

    public Boolean validateUserToken(String userId,String token, String tokenType){
        String query = "select count(*) as count from users u join user_sessions us on us.user_id=u.id where u.id=? and us."+tokenType+"=?";
        return jdbc.execute(query,(PreparedStatementCallback<Boolean>) ps->{
            try{
                ps.setString(1,userId);
                ps.setString(2,token);
                ResultSet rs = ps.executeQuery();
                if(rs.next()) {
                    return rs.getInt("count")>0;
                }
                else return false;
            }catch (Exception e){
                System.out.println(e.getMessage() + " || Trace: "+e.getStackTrace()[0]+ " || "+e.getStackTrace()[1]);
                return false;
            }
        } );
    }

    public Boolean checkUrlValidity(String role, String url,String method){
        String query = "SELECT COUNT(*) as count FROM profiles p " +
                "LEFT JOIN profile_authorities pa ON p.id = pa.profile_id " +
                "LEFT JOIN urls u ON pa.url_id = u.id " +
                "WHERE p.role = ? AND (pa.request_methods = ? or pa.request_methods='*') AND u.url like ?";

        return jdbc.execute(query,(PreparedStatementCallback<Boolean>) ps->{
            try{
                ps.setString(1,role);
                ps.setString(2,method);
                ps.setString(3,url+"%");
                ResultSet rs = ps.executeQuery();
                if(rs.next()){
                    return rs.getInt("count")>0;
                }
                else{
                    return false;
                }
            }catch (Exception e){
                System.out.println(e.getMessage() + " || Trace: "+e.getStackTrace()[0]+ " || "+e.getStackTrace()[1]);
                throw e;
            }
        } );
    }

    public JSONArray getAllUsers(String userId){
        try{
            List<Map<String, Object>> rows = jdbc.queryForList("SELECT * FROM users order by id");

            JSONArray jsonArray = new JSONArray();
            for (Map<String, Object> row : rows) {
                JSONObject jsonObject = new JSONObject(row);
                if(jsonObject.getInt("id") != Integer.parseInt(userId)){
                    jsonObject.remove("password");
                }
                jsonArray.put(jsonObject);
            }
            return jsonArray;
        }catch (Exception e){
            System.out.println(e.getMessage() + " || Trace: "+e.getStackTrace()[0]+ " || "+e.getStackTrace()[1]);
            return null;
        }
    }

    public JSONObject getUserWithId(int id,String userId){
        try{
            List<Map<String, Object>> rows = jdbc.queryForList("SELECT * FROM users WHERE id = ?", id);

            if (!rows.isEmpty()) {
                Map<String, Object> row = rows.get(0);
                JSONObject obj = new JSONObject(row);
                if(obj.getInt("id") != Integer.parseInt(userId)){
                    obj.remove("password");
                }
                return obj;
            } else {
                return null;
            }
        }catch (Exception e){
            System.out.println(e.getMessage() + " || Trace: "+e.getStackTrace()[0]+ " || "+e.getStackTrace()[1]);
            return null;
        }
    }

    public boolean createUser(JSONObject obj){
        try {
            String phoneNumber = "";
            if(obj.has("phone_number") && !CommonMethods.parseNullString(obj.getString("phone_number")).isEmpty()){
                phoneNumber = obj.getString("phone_number");
            }

            int createdBy = 0;
            if(obj.has("created_by")){
                createdBy = CommonMethods.parseNullInt(obj.getInt("created_by"));
            }

            Integer userProfilId =0;
            if(obj.has("profile_id")){
                userProfilId = CommonMethods.parseNullInt(obj.getInt("profile_id"));
            }
            if(userProfilId<=0){
                userProfilId = jdbc.queryForObject("select id from profiles where role=?", Integer.class, "user");
            }

            int rows = jdbc.update("insert into users (name,password,email,phone_number,profile_id,created_by) values (?,?,?,?,?,?)",
                    obj.getString("name"), passwordEncoder.encode(obj.getString("password")), obj.getString("email"),
                    phoneNumber, userProfilId,createdBy);
            return rows > 0;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateUser(JSONObject obj,int id){
        try{
            String phoneNumber = "";
            if(obj.has("phone_number") && !CommonMethods.parseNullString(obj.getString("phone_number")).isEmpty()){
                phoneNumber = obj.getString("phone_number");
            }

            String query = "UPDATE users SET name = ?, email = ?, phone_number = ?";

            List<Object> parameters = new ArrayList<>();
            parameters.add(obj.getString("name"));
            parameters.add(obj.getString("email"));
            parameters.add(phoneNumber);

            if (obj.has("profile_id")) {
                query += ", profile_id = ?";
                parameters.add(obj.getInt("profile_id"));
            }
            if (obj.has("updated_by")) {
                query += ", updated_by = ?";
                parameters.add(obj.getInt("updated_by"));
            }
            query += " WHERE id = ?";

            parameters.add(id);

            int result = jdbc.update(query, parameters.toArray());

            return result>0;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(String id){
        try{
            int result = jdbc.update("delete from users where id=?",id);
            if(result>0){
                deleteUserSession(id);
                return true;
            }
            return false;
        }catch (Exception e){
            System.out.println(e.getMessage() + " || Trace: "+ Arrays.toString(e.getStackTrace()));
            return false;
        }
    }

    public String bcryptPassword(String pswrd){
        return passwordEncoder.encode(pswrd);
    }
}
