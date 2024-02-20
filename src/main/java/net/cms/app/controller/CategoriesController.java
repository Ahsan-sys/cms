package net.cms.app.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.cms.app.response.GenericResponse;
import net.cms.app.service.CategoriesService;
import net.cms.app.service.TemplatesService;
import net.cms.app.utility.CommonMethods;
import net.cms.app.utility.JwtUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/cms/documentCategories", "/api/admin/templateCategories"})
@AllArgsConstructor
public class CategoriesController {

    @Autowired
    private final CategoriesService categoriesService;

    @Autowired
    private final TemplatesService templatesService;

    @Autowired
    private JwtUtil jwtUtil;
    @GetMapping
    public ResponseEntity<String> getCategoriesApi(HttpServletRequest request, @RequestParam(required = false) String search){
        String accessToken = CommonMethods.parseNullString(request.getHeader("Access-Token"));
        String userId = jwtUtil.extractUserId(accessToken);

        String type= CommonMethods.getTemplateType(request.getServletPath());

        GenericResponse rsp = new GenericResponse();
        JSONArray rspArray = categoriesService.getCategories(Integer.parseInt(userId),type,search);
        if(rspArray.isEmpty()){
            rsp.setStatus(0);
            rsp.setMessage("Error Fetching data");
        }else{
            rsp.setDataArray(rspArray);
        }
        return ResponseEntity.status(200).body(rsp.rspToJson().toString());
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getCategoryApi(HttpServletRequest request, @PathVariable int id){
        String accessToken = CommonMethods.parseNullString(request.getHeader("Access-Token"));
        String userId = jwtUtil.extractUserId(accessToken);

        GenericResponse rsp = new GenericResponse();
        if(CommonMethods.parseNullInt(id)==0){
            rsp.setStatus(0);
            rsp.setMessage("Category id is missing");
        }else{
            JSONObject rspObj = categoriesService.getCategory(id,userId);
            if(rspObj.isEmpty()){
                rsp.setStatus(0);
                rsp.setMessage("Error Fetching data");
            }else{
                rsp.setData(rspObj);
            }
        }
        return ResponseEntity.status(200).body(rsp.rspToJson().toString());
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

            String type= CommonMethods.getTemplateType(request.getServletPath());

            boolean isInserted = categoriesService.createCategory(title,type,userId);
            if(isInserted){
                rsp.setMessage("Category created successfully");
            }else{
                rsp.setStatus(0);
                rsp.setMessage("Error creating category");
            }
        }
        return ResponseEntity.status(200).body(rsp.rspToJson().toString());
    }

    @DeleteMapping
    public ResponseEntity<String> deleteCategoryApi(HttpServletRequest request,@RequestParam String id){
        GenericResponse rsp = new GenericResponse();
        if(CommonMethods.parseNullInt(id)==0){
            rsp.setMessage("Category id is missing");
            rsp.setStatus(0);
        }else{
            String accessToken = CommonMethods.parseNullString(request.getHeader("Access-Token"));
            String userId = jwtUtil.extractUserId(accessToken);
            String type= CommonMethods.getTemplateType(request.getServletPath());

            if(!templatesService.getTemplates(Integer.parseInt(userId),id,type,null).isEmpty()){
                rsp.setMessage("Cannot delete category, it has templates inside");
                rsp.setStatus(0);
            }else{
                boolean isDeleted = categoriesService.deleteCategory(id,userId);
                if(isDeleted){
                    rsp.setMessage("Category deleted successfully");
                }else{
                    rsp.setMessage("Error deleting category");
                    rsp.setStatus(0);
                }
            }
        }
        return ResponseEntity.status(200).body(rsp.rspToJson().toString());
    }
}
