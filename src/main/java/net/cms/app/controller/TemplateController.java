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
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

@RestController
@RequestMapping({"/api/cms/documents", "/api/admin/templates"})
@AllArgsConstructor
public class TemplateController {

    @Autowired
    private final TemplatesService templatesService;

    @Autowired
    private JwtUtil jwtUtil;
    @GetMapping
    public ResponseEntity<String> getCategoriesApi(HttpServletRequest request, @RequestBody String obj){
        GenericResponse rsp = new GenericResponse();
        try {
            JSONObject reqData = new JSONObject(obj);

            if(reqData.isEmpty() || !reqData.has("categoryId") || CommonMethods.parseNullString(reqData.optString("categoryId")).isEmpty()){
                rsp.setStatus(0);
                rsp.setMessage("Required data is missing");
            }
            String accessToken = CommonMethods.parseNullString(request.getHeader("Access-Token"));
            String userId = jwtUtil.extractUserId(accessToken);

            String type="doc";
            if(request.getServletPath().contains("admin")){
                type="tmp";
            }

            JSONArray rspArray = templatesService.getTemplates(Integer.parseInt(userId),reqData.optString("search"),reqData.getString("categoryId"),type);
            if(rspArray.isEmpty()){
                rsp.setStatus(0);
                rsp.setMessage("Error Fetching data");
            }else{
                rsp.setDataArray(rspArray);
            }
        }catch (Exception e){
            System.out.println(Arrays.toString(e.getStackTrace()));
            rsp.setStatus(0);
            rsp.setMessage(e.getMessage());
        }
        return ResponseEntity.ok(rsp.rspToJson().toString());
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getTemplateApi(HttpServletRequest request, @PathVariable int id){
        GenericResponse rsp = new GenericResponse();

        try{
            String accessToken = CommonMethods.parseNullString(request.getHeader("Access-Token"));
            String userId = jwtUtil.extractUserId(accessToken);

            String type="doc";
            if(request.getServletPath().contains("admin")){
                type="tmp";
            }

            JSONObject rspObj = templatesService.getTemplate(Integer.parseInt(userId),id,type);
            if(rspObj.isEmpty()){
                rsp.setStatus(0);
                rsp.setMessage("Error Fetching data");
            }else{
                rsp.setData(rspObj);
            }
        }catch (Exception e){
            rsp.setStatus(0);
            rsp.setMessage(e.getMessage());
        }
        return ResponseEntity.ok(rsp.rspToJson().toString());
    }

    @PostMapping
    public ResponseEntity<String> createTemplateApi(HttpServletRequest request, @RequestParam("file") MultipartFile file,@RequestBody String obj){
        try{
            JSONObject templateObj = new JSONObject(obj);

            if(file.isEmpty()){
                return ResponseEntity.status(401).body("File should not be empty");
            }else if(templateObj.isEmpty()){
                return ResponseEntity.status(401).body("Template data missing");
            }else if(!templateObj.has("category_id")){
                return ResponseEntity.status(401).body("Category id is missing");
            }else if(!templateObj.has("title")){
                return ResponseEntity.status(401).body("Title is missing");
            }else if(!CommonMethods.isSupportedFileType(file.getContentType())){
                return ResponseEntity.status(401).body("File type is invalid");
            }else if(!CommonMethods.isValidFileSize(file.getSize())){
                return ResponseEntity.status(401).body("File is too large");
            }else{
                String accessToken = CommonMethods.parseNullString(request.getHeader("Access-Token"));
                String userId = jwtUtil.extractUserId(accessToken);

                String type="doc";
                if(request.getServletPath().contains("admin")){
                    if(templateObj.has("expiry_date")){
                        templateObj.remove("expiry_date");
                    }
                    type="tmp";
                }

                boolean isInserted = templatesService.createTemplate(file,templateObj,type, Integer.parseInt(userId));
                if(isInserted){
                    return ResponseEntity.status(200).body("Template created successfully");
                }else{
                    return ResponseEntity.status(200).body("Error creating template");
                }
            }
        }catch (Exception e){
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    @PutMapping
    public ResponseEntity<String> updateTemplateApi(HttpServletRequest request, @RequestParam("file") MultipartFile file,@RequestBody String obj){
        try{
            JSONObject templateObj = new JSONObject(obj);

            if(file.isEmpty()){
                return ResponseEntity.status(401).body("File should not be empty");
            }else if(templateObj.isEmpty()){
                return ResponseEntity.status(401).body("Template data missing");
            }else if(!templateObj.has("category_id")){
                return ResponseEntity.status(401).body("Category id is missing");
            }else if(!templateObj.has("title")){
                return ResponseEntity.status(401).body("Title is missing");
            }else if(!CommonMethods.isSupportedFileType(file.getContentType())){
                return ResponseEntity.status(401).body("File type is invalid");
            }else if(!CommonMethods.isValidFileSize(file.getSize())){
                return ResponseEntity.status(401).body("File is too large");
            }else if(!templateObj.has("template_id")){
                return ResponseEntity.status(401).body("Template id is missing");
            }else{
                String accessToken = CommonMethods.parseNullString(request.getHeader("Access-Token"));
                String userId = jwtUtil.extractUserId(accessToken);

                String type="doc";
                if(request.getServletPath().contains("admin")){
                    if(templateObj.has("expiry_date")){
                        templateObj.remove("expiry_date");
                    }
                    type="tmp";
                }

                boolean isInserted = templatesService.createTemplate(file,templateObj,type, Integer.parseInt(userId));
                if(isInserted){
                    return ResponseEntity.status(200).body("Template created successfully");
                }else{
                    return ResponseEntity.status(200).body("Error creating template");
                }
            }
        }catch (Exception e){
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    @DeleteMapping
    public ResponseEntity<String> deleteCategoryApi(HttpServletRequest request,@RequestParam int id){
        try{
            String accessToken = CommonMethods.parseNullString(request.getHeader("Access-Token"));
            String userId = jwtUtil.extractUserId(accessToken);

            String type="doc";
            if(request.getServletPath().contains("admin")){
                type="tmp";
            }
            boolean isDeleted = templatesService.deleteTemplate(id,type, Integer.parseInt(userId));
            if(isDeleted){
                return ResponseEntity.status(200).body("Template deleted successfully");
            }else{
                return ResponseEntity.status(200).body("Error deleting template");
            }
        }catch(Exception e){
            e.printStackTrace();
            return ResponseEntity.status(200).body(e.getMessage());
        }
    }
}
