package net.cms.app.service;

import net.cms.app.utility.CommonMethods;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class CategoriesService {
    @Autowired
    private JdbcTemplate jdbc;

    public JSONArray getCategories(int userId, String type, String searchTxt){
        try{
            List<Object> params = new ArrayList<>();
            String query = "SELECT * FROM categories where created_by=? and type=? ";

            params.add(userId);
            params.add(type);

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
                    obj.put("title", rs.getString("title"));
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

    public JSONObject getCategory(int id){
        try {
            String query = "SELECT * FROM categories where id=? ";
            return jdbc.queryForObject(query, new Object[]{id}, new RowMapper<JSONObject>() {
                @Override
                public JSONObject mapRow(ResultSet rs, int rowNum) throws SQLException {
                    JSONObject obj = new JSONObject();
                    // Assuming the table has columns: id, name, value
                    obj.put("id", rs.getInt("id"));
                    obj.put("title", rs.getString("title"));
                    obj.put("created_dt", rs.getString("created_dt"));
                    obj.put("created_by", rs.getString("created_by"));
                    obj.put("updated_dt", rs.getString("updated_dt"));
                    obj.put("updated_by", rs.getString("updated_by"));
                    return obj;
                }
            });
        } catch (EmptyResultDataAccessException e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            return new JSONObject();
        }
    }

    public boolean createCategory(String title, String type, String createdBy){
        try{
            jdbc.update("insert ignore into categories (title, type, created_by) values (?,?,?)", title,type,createdBy);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public boolean updateCategory(int categoryId,String title, String updatedBy){
        try{
            jdbc.update("UPDATE categories SET title = ?,updated_by=? WHERE id = ?", title, updatedBy,categoryId);
            return true;
        }catch (DuplicateKeyException e){
            System.out.println(Arrays.toString(e.getStackTrace()));
            return false;
        }
    }

    public boolean deleteCategory(int categoryId){
        try{
            return jdbc.update("DELETE FROM categories WHERE id = ?", categoryId) > 0;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

}
