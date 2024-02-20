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
    private final CategoriesService categoriesService;

    @Autowired
    private JwtUtil jwtUtil;
    @GetMapping
    public ResponseEntity<String> getTemplatesApi(HttpServletRequest request, @RequestParam String category_id,@RequestParam(required = false) String search){
        GenericResponse rsp = new GenericResponse();
        try {
            if(CommonMethods.parseNullString(category_id).isEmpty()){
                rsp.setStatus(0);
                rsp.setMessage("Category id is missing");
            }else{
                String userId = jwtUtil.extractUserId(CommonMethods.parseNullString(request.getHeader("Access-Token")));

                String type= CommonMethods.getTemplateType(request.getServletPath());
                if(categoriesService.getCategory(Integer.parseInt(category_id),userId).isEmpty()){
                    rsp.setStatus(0);
                    rsp.setMessage("Invalid category id");
                }else{
                    JSONArray rspArray = templatesService.getTemplates(Integer.parseInt(userId),category_id,type,search);
                    if(rspArray.isEmpty()){
                        rsp.setStatus(0);
                        rsp.setMessage("Error Fetching data");
                    }else{
                        rsp.setDataArray(rspArray);
                    }
                }
            }
        }catch (Exception e){
            rsp.setStatus(0);
            rsp.setMessage(e.getMessage());
        }
        return ResponseEntity.status(200).body(rsp.rspToJson().toString());
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getTemplateApi(HttpServletRequest request, @PathVariable int id){
        GenericResponse rsp = new GenericResponse();

        try{
            String userId = jwtUtil.extractUserId(CommonMethods.parseNullString(request.getHeader("Access-Token")));

            String type= CommonMethods.getTemplateType(request.getServletPath());

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
        return ResponseEntity.status(200).body(rsp.rspToJson().toString());
    }

    @PostMapping
    public ResponseEntity<String> createTemplateApi(HttpServletRequest request, @RequestParam("file") MultipartFile file,@RequestParam("data") String obj){
        GenericResponse rsp = new GenericResponse();
        try{
            JSONObject templateObj = new JSONObject(obj);

            if(file.isEmpty()){
                rsp.setStatus(0);
                rsp.setMessage("File should not be empty");
            }else if(templateObj.isEmpty()){
                rsp.setStatus(0);
                rsp.setMessage("Template data missing");
            }else if(!templateObj.has("category_id") || CommonMethods.parseNullString(templateObj.getString("category_id")).isEmpty()){
                rsp.setStatus(0);
                rsp.setMessage("Category id is missing");
            }else if(!templateObj.has("title") || CommonMethods.parseNullString(templateObj.getString("title")).isEmpty()){
                rsp.setStatus(0);
                rsp.setMessage("Title is missing");
            }else if(!CommonMethods.isSupportedFileType(file.getContentType())){
                rsp.setStatus(0);
                rsp.setMessage("File type is invalid");
            }else if(!CommonMethods.isValidFileSize(file.getSize())){
                rsp.setStatus(0);
                rsp.setMessage("File is too large");
            }else{
                String userId = jwtUtil.extractUserId(CommonMethods.parseNullString(request.getHeader("Access-Token")));

                String type= CommonMethods.getTemplateType(request.getServletPath());

                if(request.getServletPath().contains("admin")){
                    if(templateObj.has("expiry_date")){
                        templateObj.remove("expiry_date");
                    }
                }

                if(categoriesService.getCategory(Integer.parseInt(templateObj.getString("category_id")),userId).isEmpty()){
                    rsp.setStatus(0);
                    rsp.setMessage("Invalid category id passed");
                }else if(!templatesService.isTemplateUnique(Integer.parseInt(userId),templateObj.getString("category_id"),templateObj.getString("title"))){
                    rsp.setStatus(0);
                    rsp.setMessage("Template already exists with this title");
                }else{
                    boolean isInserted = templatesService.createTemplate(file,templateObj,type, Integer.parseInt(userId));
                    if(isInserted){
                        rsp.setMessage("Template created successfully");
                    }else{
                        rsp.setStatus(0);
                        rsp.setMessage("Error uploading template");
                    }
                }
            }
        }catch (Exception e){
            rsp.setStatus(0);
            rsp.setMessage(e.getMessage());
        }
        return ResponseEntity.status(200).body(rsp.rspToJson().toString());
    }

    @PutMapping
    public ResponseEntity<String> updateTemplateApi(HttpServletRequest request, @RequestParam("file") MultipartFile file,@RequestParam("data") String obj){
        GenericResponse rsp = new GenericResponse();
        try{
            JSONObject templateObj = new JSONObject(obj);
            if(file.isEmpty()){
                rsp.setMessage("File should not be empty");
                rsp.setStatus(0);
            }else if(!CommonMethods.isSupportedFileType(file.getContentType())){
                rsp.setMessage("File type is invalid");
                rsp.setStatus(0);
            }else if(!CommonMethods.isValidFileSize(file.getSize())){
                rsp.setMessage("File is too large");
                rsp.setStatus(0);
            }else if(!templateObj.has("template_id") || CommonMethods.parseNullString(templateObj.getString("template_id")).isEmpty()){
                rsp.setMessage("Template id is missing");
                rsp.setStatus(0);
            }else{
                String userId = jwtUtil.extractUserId(CommonMethods.parseNullString(request.getHeader("Access-Token")));
                String type= CommonMethods.getTemplateType(request.getServletPath());

                JSONObject templateObject = templatesService.getTemplate(Integer.parseInt(userId), Integer.parseInt(templateObj.getString("template_id")),type);
                if(templateObject.isEmpty()){
                    rsp.setMessage("Invalid template id");
                    rsp.setStatus(0);
                }else{
                    if(request.getServletPath().contains("admin")){
                        if(templateObj.has("expiry_date")){
                            templateObj.remove("expiry_date");
                        }
                    }

                    templateObj.put("category_id",templateObject.getString("category_id"));
                    templateObj.put("title",templateObject.getString("title"));
                    templateObj.put("actual_file_name",templateObject.getString("actual_file_name"));
                    boolean isUpdated = templatesService.updateTemplate(file,templateObj,type, Integer.parseInt(userId));
                    if(isUpdated){
                        rsp.setMessage("Template updated successfully");
                    }else{
                        rsp.setMessage("Error updating template");
                        rsp.setStatus(0);
                    }
                }
            }
        }catch (Exception e){
            rsp.setMessage(e.getMessage());
            rsp.setStatus(0);
        }
        return ResponseEntity.status(200).body(rsp.rspToJson().toString());
    }

    @DeleteMapping
    public ResponseEntity<String> deleteTemplateApi(HttpServletRequest request,@RequestParam String id){
        GenericResponse rsp = new GenericResponse();
        try{
            if(CommonMethods.parseNullString(id).isEmpty()){
                rsp.setMessage("Temaplte id missing");
                rsp.setStatus(0);
            }else {
                String userId = jwtUtil.extractUserId(CommonMethods.parseNullString(request.getHeader("Access-Token")));

                String type = CommonMethods.getTemplateType(request.getServletPath());
                JSONObject templateObj = templatesService.getTemplate(Integer.parseInt(userId), Integer.parseInt(id), type);
                if (templateObj.isEmpty()) {
                    rsp.setMessage("Invalid template id");
                    rsp.setStatus(0);
                } else {
                    if (templatesService.deleteTemplate(Integer.parseInt(id), type, Integer.parseInt(userId), templateObj)) {
                        rsp.setMessage("Template deleted successfully");
                    } else {
                        rsp.setMessage("Error deleting template");
                        rsp.setStatus(0);
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
            rsp.setMessage(e.getMessage());
            rsp.setStatus(0);
        }
        return ResponseEntity.status(200).body(rsp.rspToJson().toString());
    }
}
