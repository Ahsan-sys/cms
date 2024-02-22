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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
public class CategoriesService {
    @Autowired
    private JdbcTemplate jdbc;

    public JSONArray getCategories(int userId, String type, String searchTxt){
        try{
            List<Object> params = new ArrayList<>();
            String query = "SELECT c.*,COUNT(t.id) as templates_count from categories c LEFT JOIN templates t ON c.id=t.category_id GROUP BY c.id having c.created_by=? and type=? ";

            params.add(userId);
            params.add(type);

            if(!CommonMethods.parseNullString(searchTxt).isEmpty()){
                System.out.println(searchTxt.length());
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
                    obj.put("templates_count", rs.getString("templates_count"));
                    obj.put("created_dt", rs.getString("created_dt"));
                    obj.put("created_by", rs.getString("created_by"));
                    obj.put("updated_dt", rs.getString("updated_dt"));
                    obj.put("updated_by", rs.getString("updated_by"));
                    return obj;
                }
            });

            return new JSONArray(list);

        }catch (Exception e){
            e.printStackTrace();
            return new JSONArray();
        }
    }

    public JSONArray getAllCategories(String type, int userId){
        try{
            List<Object> params = new ArrayList<>();
            String query = "SELECT c.*,COUNT(t.id) as templates_count from categories c LEFT JOIN templates t ON c.id=t.category_id GROUP BY c.id having type=? ";
            params.add(type);

            if(userId > 0 ){
                query += " and created_by = ?";
                params.add(userId);
            }
            query+= " order by id";

            List<JSONObject> list = jdbc.query(query,params.toArray(), new RowMapper<JSONObject>() {
                @Override
                public JSONObject mapRow(ResultSet rs, int rowNum) throws SQLException {
                    JSONObject obj = new JSONObject();
                    obj.put("id", rs.getInt("id"));
                    obj.put("title", rs.getString("title"));
                    obj.put("templates_count", rs.getString("templates_count"));
                    obj.put("created_dt", rs.getString("created_dt"));
                    obj.put("created_by", rs.getString("created_by"));
                    obj.put("updated_dt", rs.getString("updated_dt"));
                    obj.put("updated_by", rs.getString("updated_by"));
                    return obj;
                }
            });

            return new JSONArray(list);

        }catch (Exception e){
            e.printStackTrace();
            return new JSONArray();
        }
    }

    public JSONObject getCategory(int id, String userId){
        try {
            String query = "SELECT c.*,COUNT(t.id) as templates_count from categories c LEFT JOIN templates t ON c.id=t.category_id GROUP BY c.id having c.id=? and c.created_by=?";
            return jdbc.queryForObject(query, new Object[]{id,userId}, new RowMapper<JSONObject>() {
                @Override
                public JSONObject mapRow(ResultSet rs, int rowNum) throws SQLException {
                    JSONObject obj = new JSONObject();
                    obj.put("id", rs.getInt("id"));
                    obj.put("title", rs.getString("title"));
                    obj.put("templates_count", rs.getString("templates_count"));
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

    public boolean createCategory(String title, String type, String createdBy){
        try{
            return jdbc.update("insert ignore into categories (title, type, created_by) values (?,?,?)", title,type,createdBy)>0;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateCategory(int categoryId,String title, String updatedBy){
        try{
            jdbc.update("UPDATE categories SET title = ?,updated_by=? WHERE id = ?", title, updatedBy,categoryId);
            return true;
        }catch (DuplicateKeyException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteCategory(String categoryId, String userId){
        try{
            return jdbc.update("DELETE FROM categories WHERE id = ? and created_by=?", categoryId,userId) > 0;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

}
