package net.cms.app.service;

import io.jsonwebtoken.io.IOException;
import net.cms.app.utility.CommonMethods;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
public class TemplatesService {
    @Autowired
    private JdbcTemplate jdbc;

    public JSONArray getTemplates(int userId, String searchTxt, String categoryId,String type){
        try{
            List<Object> params = new ArrayList<>();
            String query = "SELECT * FROM templates where created_by=? and category_id=? ";

            params.add(userId);
            params.add(categoryId);

            if(!CommonMethods.parseNullString(searchTxt).isEmpty()){
                query+=" and title like ?";
                params.add("%" + searchTxt.trim() + "%");
            }
            query+= " order by id";


            List<JSONObject> list = jdbc.query(query,params.toArray(), new RowMapper<JSONObject>() {
                @Override
                public JSONObject mapRow(ResultSet rs, int rowNum) throws SQLException {
                    JSONObject obj = new JSONObject();
                    obj.put("id", rs.getInt("id"));
                    obj.put("uuid", rs.getInt("uuid"));
                    obj.put("title", rs.getString("title"));
                    obj.put("description", CommonMethods.parseNullString(rs.getString("description")));
                    obj.put("category_id", rs.getString("category_id"));

                    String docUrl="";
                    if(type.equals("doc")){
                        obj.put("version", rs.getString("version"));
                        obj.put("expiry_date", CommonMethods.parseNullString(rs.getString("expiry_date")));

                        docUrl = getTemplateUrl(jdbc,"user_documents",userId,rs.getString("category_id"),rs.getString("title"));
                    }else{
                        docUrl = getTemplateUrl(jdbc,"admin_documents",userId,rs.getString("category_id"),rs.getString("title"));
                    }

                    obj.put("doc_url", docUrl);
                    obj.put("created_dt", rs.getString("created_dt"));
                    obj.put("created_by", rs.getString("created_by"));
                    obj.put("updated_dt", rs.getString("updated_dt"));
                    obj.put("updated_by", rs.getString("updated_by"));
                    return obj;
                }
            });

            return new JSONArray(list);

        }catch (Exception e){
            System.out.println(Arrays.toString(e.getStackTrace()));
            return new JSONArray();
        }
    }

    public JSONObject getTemplate(int userId, int templateId, String type){
        try {
            String query = "SELECT * FROM templates where id=? ";
            return jdbc.queryForObject(query, new Object[]{templateId}, new RowMapper<JSONObject>() {
                @Override
                public JSONObject mapRow(ResultSet rs, int rowNum) throws SQLException {
                    JSONObject obj = new JSONObject();
                    obj.put("id", rs.getInt("id"));
                    obj.put("uuid", rs.getInt("uuid"));
                    obj.put("title", rs.getString("title"));
                    obj.put("description", CommonMethods.parseNullString(rs.getString("description")));
                    obj.put("category_id", rs.getString("category_id"));

                    String docUrl="";
                    if(type.equals("doc")){
                        obj.put("version", rs.getString("version"));
                        obj.put("expiry_date", CommonMethods.parseNullString(rs.getString("expiry_date")));

                        docUrl = getTemplateUrl(jdbc,"user_documents",userId,rs.getString("category_id"),rs.getString("title"));
                    }else{
                        docUrl = getTemplateUrl(jdbc,"admin_documents",userId,rs.getString("category_id"),rs.getString("title"));
                    }

                    obj.put("doc_url", docUrl);
                    obj.put("created_dt", rs.getString("created_dt"));
                    obj.put("created_by", rs.getString("created_by"));
                    obj.put("updated_dt", rs.getString("updated_dt"));
                    obj.put("updated_by", rs.getString("updated_by"));
                    return obj;
                }
            });
        } catch (EmptyResultDataAccessException e) {
            e.printStackTrace();
            return new JSONObject();
        }
    }

    public boolean createTemplate(MultipartFile file, JSONObject obj, String type, int userId){
        try{
            List<Object> queryParams = new ArrayList<>();
            StringBuilder query1 = new StringBuilder("INSERT IGNORE INTO templates (category_id, title, created_by,version ");
            StringBuilder query2 = new StringBuilder(" VALUES (?, ?, ?,?");
            queryParams.add(obj.get("category_id"));
            queryParams.add(obj.get("title"));
            queryParams.add(userId);
            queryParams.add(1);

            if (obj.has("description")) {
                query1.append(", description");
                query2.append(", ?");
                queryParams.add(obj.get("description"));
            } else if (obj.has("expiry_date")) {
                query1.append(", expiry_date");
                query2.append(", ?");
                queryParams.add(obj.get("expiry_date"));
            }
            query1.append(")");
            query2.append(")");
            String qry = query1.toString() + query2.toString();

            String docConfig = type.equals("doc") ? "user_documents" : "admin_documents";
            String docUrl = getTemplateUrl(jdbc, docConfig, userId, obj.getString("category_id"), obj.getString("title"));

            int rows = jdbc.update(qry, queryParams.toArray());
            if (rows > 0) {
                return uploadFile(file, docUrl);
            } else {
                return false;
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateTemplate(MultipartFile file, JSONObject obj, String type, int userId){
        try{
            List<Object> queryParams = new ArrayList<>();
            StringBuilder query = new StringBuilder("update templates set updated_by=?,version=version+1 ");
            queryParams.add(userId);

            if (obj.has("description")) {
                query.append(", description=?");
                queryParams.add(obj.get("description"));
            } else if (obj.has("expiry_date")) {
                query.append(", expiry_date");
                queryParams.add(obj.get("expiry_date"));
            }

            query.append(" where id=?");
            queryParams.add(obj.get("template_id"));

            String docConfig = type.equals("doc") ? "user_documents" : "admin_documents";
            String docUrl = getTemplateUrl(jdbc, docConfig, userId, obj.getString("category_id"), obj.getString("title"));

            int rows = jdbc.update(query.toString(), queryParams.toArray());
            if (rows > 0) {
                return uploadFile(file, docUrl);
            } else {
                return false;
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteTemplate(int templateId, String type, int userId){
        try{
            JSONObject obj = getTemplate(userId,templateId,type);
            String docConfig = type.equals("doc") ? "user_documents" : "admin_documents";
            String docUrl = getTemplateUrl(jdbc, docConfig, userId, obj.getString("category_id"), obj.getString("title"));
            deleteFile(docUrl);
            return jdbc.update("DELETE FROM templates WHERE id = ?", templateId) > 0;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private boolean uploadFile(MultipartFile file, String uploadDir) throws IOException {
        Path destinationPath = Paths.get(uploadDir).resolve(Paths.get(Objects.requireNonNull(file.getOriginalFilename()))).normalize().toAbsolutePath();
        try {
            if (Files.notExists(destinationPath.getParent())) {
                Files.createDirectories(destinationPath.getParent());
            }
            Files.copy(file.getInputStream(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteFile(String filePath) {
        File file = new File(filePath);
        return file.delete();
    }

    private String getTemplateUrl(JdbcTemplate jdbc,String code, int userId, String categoryId, String title){
        String url="";
        String basePath;
        String userUuid;
        String categoryUuid;
        try {
            basePath = jdbc.queryForObject("SELECT val FROM config WHERE code = ?", new Object[]{code}, String.class);
            if(!CommonMethods.parseNullString(basePath).isEmpty()){
                userUuid = jdbc.queryForObject("SELECT uuid FROM users WHERE id = ?", new Object[]{userId}, String.class);

                if(!CommonMethods.parseNullString(userUuid).isEmpty()){
                    categoryUuid = jdbc.queryForObject("SELECT uuid FROM categories WHERE id = ?", new Object[]{categoryId}, String.class);

                    if(!CommonMethods.parseNullString(categoryUuid).isEmpty()){
                        url=basePath+"/"+userUuid+"/"+categoryUuid+"/"+title;
                    }
                }
            }
        } catch (EmptyResultDataAccessException e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            return null;
        }
        return url;
    }
}
