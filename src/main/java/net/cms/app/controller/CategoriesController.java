package net.cms.app.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.cms.app.response.GenericResponse;
import net.cms.app.service.CategoriesService;
import net.cms.app.utility.CommonMethods;
import net.cms.app.utility.JwtUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/cms/categories", "/api/admin/categories"})
@AllArgsConstructor
public class CategoriesController {

    @Autowired
    private final CategoriesService categoriesService;

    @Autowired
    private JwtUtil jwtUtil;
    @GetMapping
    public ResponseEntity<String> getCategoriesApi(HttpServletRequest request, @RequestParam String search){
        //Need to make search optional
        String accessToken = CommonMethods.parseNullString(request.getHeader("Access-Token"));
        String userId = jwtUtil.extractUserId(accessToken);

        String type="doc";
        if(request.getServletPath().contains("admin")){
            type="tmp";
        }

        GenericResponse rsp = new GenericResponse();
        JSONArray rspArray = categoriesService.getCategories(Integer.parseInt(userId),type,search);
        if(rspArray.isEmpty()){
            rsp.setStatus(0);
            rsp.setMessage("Error Fetching data");
        }else{
            rsp.setDataArray(rspArray);
        }
        return ResponseEntity.ok(rsp.rspToJson().toString());
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getCategoryApi(@PathVariable int id){
        GenericResponse rsp = new GenericResponse();
        if(CommonMethods.parseNullInt(id)==0){
            rsp.setStatus(0);
            rsp.setMessage("Category id is missing");
        }else{
            JSONObject rspObj = categoriesService.getCategory(id);
            if(rspObj.isEmpty()){
                rsp.setStatus(0);
                rsp.setMessage("Error Fetching data");
            }else{
                rsp.setData(rspObj);
            }
        }
        return ResponseEntity.ok(rsp.rspToJson().toString());
    }

    @PostMapping
    public ResponseEntity<String> createCategoryApi(HttpServletRequest request, @RequestBody String title){
        GenericResponse rsp = new GenericResponse();
        if(CommonMethods.parseNullString(title).isEmpty()){
            rsp.setStatus(0);
            rsp.setMessage("Title is required");
        }else{
            String accessToken = CommonMethods.parseNullString(request.getHeader("Access-Token"));
            String userId = jwtUtil.extractUserId(accessToken);

            String type="doc";
            if(request.getServletPath().contains("admin")){
                type="tmp";
            }

            boolean isInserted = categoriesService.createCategory(title,type,userId);
            if(isInserted){
                rsp.setMessage("Category created successfully");
            }else{
                rsp.setStatus(0);
                rsp.setMessage("Error creating category");
            }
        }
        return ResponseEntity.ok(rsp.rspToJson().toString());
    }

    @DeleteMapping
    public ResponseEntity<String> deleteCategoryApi(@RequestParam int id){
        if(CommonMethods.parseNullInt(id)==0){
            return ResponseEntity.status(401).body("Category id is missing");
        }else{
            boolean isDeleted = categoriesService.deleteCategory(id);
            if(isDeleted){
                return ResponseEntity.status(200).body("Category deleted successfully");
            }else{
                return ResponseEntity.status(200).body("Error deleting category");
            }
        }
    }
}
