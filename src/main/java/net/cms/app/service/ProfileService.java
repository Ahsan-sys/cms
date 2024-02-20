package net.cms.app.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class ProfileService {
    @Autowired
    private JdbcTemplate jdbc;

    public JSONArray getAllProfiles(){
        try{
            List<Map<String, Object>> rows = jdbc.queryForList("SELECT * FROM profiles order by id");

            JSONArray jsonArray = new JSONArray();
            for (Map<String, Object> row : rows) {
                JSONObject jsonObject = new JSONObject(row);
                jsonArray.put(jsonObject);
            }
            return jsonArray;
        }catch (Exception e){
            System.out.println(e.getMessage() + " || Trace: "+e.getStackTrace()[0]+ " || "+e.getStackTrace()[1]);
            return null;
        }
    }

    public JSONObject getProfileWithId(int id){
        try{
            List<Map<String, Object>> rows = jdbc.queryForList("SELECT * FROM profiles WHERE id = ?", id);

            if (!rows.isEmpty()) {
                Map<String, Object> row = rows.get(0);
                return new JSONObject(row);
            } else {
                return null;
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public JSONArray getAllUrls(){
        try{
            List<Map<String, Object>> rows = jdbc.queryForList("SELECT * FROM urls order by id");

            JSONArray jsonArray = new JSONArray();
            for (Map<String, Object> row : rows) {
                JSONObject jsonObject = new JSONObject(row);
                jsonArray.put(jsonObject);
            }
            return jsonArray;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public JSONArray getAllAuhtorizedUrls(String roleId){
        try{
            List<Map<String, Object>> rows = jdbc.queryForList("SELECT pa.profile_id,p.role,pa.url_id,u.url,pa.request_methods FROM profile_authorities pa LEFT JOIN urls u ON u.id = pa.url_id " +
                " LEFT JOIN profiles p on pa.profile_id=p.id WHERE p.id=?",roleId);

            JSONArray jsonArray = new JSONArray();
            for (Map<String, Object> row : rows) {
                JSONObject jsonObject = new JSONObject(row);
                jsonArray.put(jsonObject);
            }
            return jsonArray;
        }catch (Exception e){
            return new JSONArray();
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean createRole(JSONObject profileObj){
        String role = profileObj.getString("role").replaceAll(" ","_");
        String createdBy = profileObj.getString("created_by");
        String name = profileObj.getString("name");
        JSONArray authorities = profileObj.getJSONArray("authorities");

        try{
            final String sql = "insert into profiles (role,name,created_by) values (?,?,?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbc.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                    ps.setString(1, role);
                    ps.setString(2, name);
                    ps.setString(3, createdBy);
                    return ps;
                },
            keyHolder);

            int profileId = Objects.requireNonNull(keyHolder.getKey()).intValue();
            String sql2 = "INSERT ignore INTO profile_authorities (profile_id, url_id, request_methods) VALUES (?, ?, ?)";

            for (int i = 0; i < authorities.length(); i++) {
                JSONObject authority = authorities.getJSONObject(i);
                int urlId = authority.getInt("url_id");
                String reqMeth = authority.getString("request_method");

                jdbc.update(sql2, profileId, urlId, reqMeth);
            }
            sql2 = "INSERT ignore INTO profile_authorities (profile_id, url_id, request_methods) select ?,id,'*' from urls where url in ('/api/cms/refresh_token','/api/cms/logout','/api/cms/updateUser','/api/cms/documents','/api/cms/documentCategories','/api/cms/updateUser')";
            jdbc.update(sql2, profileId);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean updateRole(JSONObject profileObj){
        String updatedBy = profileObj.getString("updated_by");
        JSONArray authorities = profileObj.getJSONArray("authorities");
        String id = profileObj.getString("profile_id");

        try{
            int rows = jdbc.update("update profiles set updated_by=? where id=?",updatedBy,id);
            if(rows > 0){
                deleteProfileAuthorities(Integer.parseInt(id));
                final String sql2 = "INSERT INTO profile_authorities (profile_id, url_id, request_methods) VALUES (?, ?, ?)";

                for (int i = 0; i < authorities.length(); i++) {
                    JSONObject authority = authorities.getJSONObject(i);

                    int urlId = authority.getInt("url_id");
                    String reqMeth = authority.getString("request_method");

                    jdbc.update(sql2, id, urlId, reqMeth);
                }
                return true;
            }else{
                return false;
            }
        }catch (Exception e){
            System.out.println(Arrays.toString(e.getStackTrace()));
            return false;
        }
    }

    public Boolean isProfileValid(int id){
        return jdbc.execute("SELECT COUNT(*) as count FROM profiles WHERE id = ?",(PreparedStatementCallback<Boolean>) ps->{
            try{
                ps.setInt(1,id);
                ResultSet rs = ps.executeQuery();
                if(rs.next()){
                    return rs.getInt("count")>0;
                }
                else{
                    return false;
                }
            }catch (Exception e){
                System.out.println(Arrays.toString(e.getStackTrace()));
                return false;
            }
        } );
    }

    public Boolean profileHasUsers(int id){
        return jdbc.execute("SELECT COUNT(*) as count FROM users WHERE profile_id = ?",(PreparedStatementCallback<Boolean>) ps->{
            try{
                ps.setInt(1,id);
                ResultSet rs = ps.executeQuery();
                if(rs.next()){
                    return rs.getInt("count")>0;
                }
                else{
                    return false;
                }
            }catch (Exception e){
                System.out.println(Arrays.toString(e.getStackTrace()));
                return false;
            }
        } );
    }

    public void deleteProfileAuthorities(int id){
        jdbc.update("delete from profile_authorities where profile_id=?",id);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRole(int id){
        try{
            int result = jdbc.update("delete from profiles where id=?",id);
            if(result>0){
                deleteProfileAuthorities(id);
                return true;
            }
            return false;
        }catch (Exception e){
            System.out.println(e.getMessage() + " || Trace: "+ Arrays.toString(e.getStackTrace()));
            return false;
        }
    }

    public boolean isRoleAndNameExist(String role, String name){
        String query = "select count(*) as count from profiles where role =? or name =? ";

        return Boolean.TRUE.equals(jdbc.execute(query, (PreparedStatementCallback<Boolean>) ps -> {
            try {
                ps.setString(1, role);
                ps.setString(2, name);

                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return rs.getInt("count") <= 0;
                } else {
                    return true;
                }
            } catch (Exception e) {
                System.out.println(Arrays.toString(e.getStackTrace()));
                return true;
            }
        }));
    }
}
