//package net.cms.app.controller;
//
//import jakarta.servlet.http.HttpServletRequest;
//import lombok.AllArgsConstructor;
//import net.cms.app.response.GenericResponse;
//import net.cms.app.service.CategoriesService;
//import net.cms.app.utility.CommonMethods;
//import net.cms.app.utility.JwtUtil;
//import org.json.JSONArray;
//import org.json.JSONObject;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/admin/tmpCategories")
//@AllArgsConstructor
//public class TempCategoriesController {
//    @Autowired
//    private final CategoriesService categoriesService;
//
//    @Autowired
//    private JwtUtil jwtUtil;
//    @GetMapping
//    public ResponseEntity<String> getCategoriesApi(HttpServletRequest request, @RequestBody String search){
//        String accessToken = CommonMethods.parseNullString(request.getHeader("Access-Token"));
//        String userId = jwtUtil.extractUserId(accessToken);
//
//        GenericResponse rsp = new GenericResponse();
//        JSONArray rspArray = categoriesService.getCategories(Integer.parseInt(userId),"tmp",search);
//        if(rspArray.isEmpty()){
//            rsp.setStatus(0);
//            rsp.setMessage("Error Fetching data");
//        }else{
//            rsp.setDataArray(rspArray);
//        }
//        return ResponseEntity.ok(rsp.rspToJson().toString());
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<String> getCategoryApi(@PathVariable int id){
//        GenericResponse rsp = new GenericResponse();
//        JSONObject rspArray = categoriesService.getCategory(id);
//        if(rspArray.isEmpty()){
//            rsp.setStatus(0);
//            rsp.setMessage("Error Fetching data");
//        }else{
//            rsp.setData(rspArray);
//        }
//        return ResponseEntity.ok(rsp.rspToJson().toString());
//    }
//
//    @PostMapping
//    public ResponseEntity<String> createCategoryApi(HttpServletRequest request, @RequestBody String title){
//        String accessToken = CommonMethods.parseNullString(request.getHeader("Access-Token"));
//        String userId = jwtUtil.extractUserId(accessToken);
//
//        boolean isInserted = categoriesService.createCategory(title,"tmp",userId);
//        if(isInserted){
//            return ResponseEntity.status(200).body("Category created successfully");
//        }else{
//            return ResponseEntity.status(200).body("Error creating category");
//        }
//    }
//
//    @DeleteMapping
//    public ResponseEntity<String> deleteCategoryApi(@RequestParam int id){
//        boolean isDeleted = categoriesService.deleteCategory(id);
//        if(isDeleted){
//            return ResponseEntity.status(200).body("Category deleted successfully");
//        }else{
//            return ResponseEntity.status(200).body("Error deleting category");
//        }
//    }
//}
