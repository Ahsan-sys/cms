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
import org.springframework.transaction.annotation.Transactional;
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

    public JSONArray getTemplates(int userId, String categoryId,String type, String searchTxt){
        try{
            List<Object> params = new ArrayList<>();
            String query = "SELECT * FROM templates where created_by=? and category_id=? ";
            params.add(userId);
            params.add(categoryId);

            if(!CommonMethods.parseNullString(searchTxt).isEmpty()){
                query+=" and title like ? ";
                params.add("%"+searchTxt+"%");
            }
            query+=" order by id ";

            List<JSONObject> list = jdbc.query(query,params.toArray(), new RowMapper<JSONObject>() {
                @Override
                public JSONObject mapRow(ResultSet rs, int rowNum) throws SQLException {
                    JSONObject obj = new JSONObject();
                    obj.put("id", rs.getInt("id"));
                    obj.put("uuid", rs.getString("uuid"));
                    obj.put("title", rs.getString("title"));
                    obj.put("description", CommonMethods.parseNullString(rs.getString("description")));
                    obj.put("category_id", rs.getString("category_id"));
                    obj.put("actual_file_name", rs.getString("actual_file_name"));

                    String docUrl="";
                    if(type.equals("doc")){
                        obj.put("version", rs.getString("version"));
                        obj.put("expiry_date", CommonMethods.parseNullString(rs.getString("expiry_date")));

                        docUrl = getTemplateUrl(jdbc,"user_documents",userId,rs.getString("category_id"));
                    }else{
                        docUrl = getTemplateUrl(jdbc,"admin_documents",userId,rs.getString("category_id"));
                    }

                    obj.put("doc_url", docUrl+rs.getString("actual_file_name"));
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
            String query = "SELECT * FROM templates where id=? and created_by=? ";
            return jdbc.queryForObject(query, new Object[]{templateId,userId}, new RowMapper<JSONObject>() {
                @Override
                public JSONObject mapRow(ResultSet rs, int rowNum) throws SQLException {
                    JSONObject obj = new JSONObject();
                    obj.put("id", rs.getInt("id"));
                    obj.put("uuid", rs.getString("uuid"));
                    obj.put("title", rs.getString("title"));
                    obj.put("description", CommonMethods.parseNullString(rs.getString("description")));
                    obj.put("category_id", rs.getString("category_id"));
                    obj.put("actual_file_name", rs.getString("actual_file_name"));

                    String docUrl="";
                    if(type.equals("doc")){
                        obj.put("version", rs.getString("version"));
                        obj.put("expiry_date", CommonMethods.parseNullString(rs.getString("expiry_date")));

                        docUrl = getTemplateUrl(jdbc,"user_documents",userId,rs.getString("category_id"));
                    }else{
                        docUrl = getTemplateUrl(jdbc,"admin_documents",userId,rs.getString("category_id"));
                    }
                    obj.put("doc_url", docUrl+rs.getString("actual_file_name"));
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

    @Transactional
    public boolean createTemplate(MultipartFile file, JSONObject obj, String type, int userId){
        try{
            String docUrl = getTemplateUrl(jdbc, type.equals("doc") ? "user_documents" : "admin_documents", userId, obj.getString("category_id"));
            String actualFileName = obj.getString("title") + getFileExtension(Objects.requireNonNull(file.getOriginalFilename()));

            if(uploadFile(file, docUrl,actualFileName)){
                List<Object> queryParams = new ArrayList<>();
                StringBuilder query1 = new StringBuilder("INSERT IGNORE INTO templates (category_id, title, created_by,version,actual_file_name ");
                StringBuilder query2 = new StringBuilder(" VALUES (?, ?, ?,?,?");
                queryParams.add(obj.get("category_id"));
                queryParams.add(obj.get("title"));
                queryParams.add(userId);
                queryParams.add(1);
                queryParams.add(actualFileName);

                if (obj.has("description")) {
                    query1.append(", description");
                    query2.append(", ?");
                    queryParams.add(obj.getString("description"));
                }
                if (obj.has("expiry_date")) {
                    query1.append(", expiry_date");
                    query2.append(", ?");
                    queryParams.add(obj.getString("expiry_date"));
                }
                query1.append(")");
                query2.append(")");
                String qry = query1.toString() + query2.toString();
                if (jdbc.update(qry, queryParams.toArray()) > 0) {
                    return true;
                } else {
                    deleteFile(docUrl+actualFileName);
                    return false;
                }
            }else{
                return false;
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateTemplate(MultipartFile file, JSONObject obj, String type, int userId){
        try{
            String oldFileName = obj.getString("actual_file_name");
            String docUrl = getTemplateUrl(jdbc, type.equals("doc") ? "user_documents" : "admin_documents", userId, obj.getString("category_id"));
            String newFileName = obj.getString("title") + getFileExtension(Objects.requireNonNull(file.getOriginalFilename()));
            if(uploadFile(file, docUrl,newFileName)){

                List<Object> queryParams = new ArrayList<>();
                StringBuilder query = new StringBuilder("update templates set updated_by=?,version=version+1,actual_file_name=? ");
                queryParams.add(userId);
                queryParams.add(newFileName);

                if (obj.has("description")) {
                    query.append(", description=?");
                    queryParams.add(obj.getString("description"));
                }
                if (obj.has("expiry_date")) {
                    query.append(", expiry_date=?");
                    queryParams.add(obj.getString("expiry_date"));
                }

                query.append(" where id=?");
                queryParams.add(obj.get("template_id"));

                if(jdbc.update(query.toString(), queryParams.toArray()) > 0){
                    if(!newFileName.equals(oldFileName)) {
                        deleteFile(docUrl+oldFileName);
                    }
                    return true;
                }else{
                    if(!newFileName.equals(oldFileName)) {
                        deleteFile(docUrl+newFileName);
                    }
                    return false;
                }

            }else{
                return false;
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteTemplate(int templateId, String type, int userId,JSONObject obj){
        try{
            String docUrl = getTemplateUrl(jdbc, type.equals("doc") ? "user_documents" : "admin_documents", userId, obj.getString("category_id"));
            System.out.println(docUrl+obj.getString("actual_file_name"));
            if(deleteFile(docUrl+obj.getString("actual_file_name"))){
                return jdbc.update("DELETE FROM templates WHERE id = ? and created_by=?", templateId,userId) > 0;
            }else{
                System.out.println("here");
                return false;
            }
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private boolean uploadFile(MultipartFile file, String uploadDir,String customFileName) throws IOException {
        Path destinationPath = Paths.get(uploadDir).resolve(customFileName).normalize().toAbsolutePath();
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

    private String getTemplateUrl(JdbcTemplate jdbc,String code, int userId, String categoryId){
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
                        url=basePath+"/"+userUuid+"/"+categoryUuid+"/";
                    }
                }
            }
        } catch (EmptyResultDataAccessException e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            return null;
        }
        return url;
    }

    public boolean isTemplateUnique(int userId, String categoryId,String title){
        try{
            String basePath = jdbc.queryForObject("SELECT id from templates where category_id=? and title=? and created_by=?", new Object[]{categoryId,title,userId}, String.class);
            return CommonMethods.parseNullString(basePath).isEmpty();
        }catch (EmptyResultDataAccessException e){
            return true;
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
            return fileName.substring(fileName.lastIndexOf("."));
        } else {
            return ""; // Empty extension
        }
    }

}
