package net.cms.app.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.cms.app.response.GenericResponse;
import net.cms.app.service.ProfileService;
import net.cms.app.service.UserService;
import net.cms.app.utility.CommonMethods;
import net.cms.app.utility.JwtUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path="/api/admin/user")
@AllArgsConstructor
public class UserController {

    @Autowired
    private final UserService userService;

    @Autowired
    private final ProfileService profileService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<String> getAllUsersApi(HttpServletRequest request){
        String accessToken = CommonMethods.parseNullString(request.getHeader("Access-Token"));
        String userId = jwtUtil.extractUserId(accessToken);

        GenericResponse rsp = new GenericResponse();
        JSONArray rspArray = userService.getAllUsers(userId);
        rsp.setDataArray(rspArray);
        return ResponseEntity.status(200).body(rsp.rspToJson().toString());
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getUserApi(HttpServletRequest request, @PathVariable int id){
        String accessToken = CommonMethods.parseNullString(request.getHeader("Access-Token"));
        String userId = jwtUtil.extractUserId(accessToken);

        GenericResponse rsp = new GenericResponse();
        JSONObject obj = userService.getUserWithId(id);
        if(obj==null){
            rsp.setStatus(0);
            rsp.setMessage("Error getting data for user");
        }else{
            rsp.setData(obj);
        }
        return ResponseEntity.status(200).body(rsp.rspToJson().toString());
    }

    @PostMapping
    public ResponseEntity<String> createUserApi(HttpServletRequest request,@RequestBody String obj){
        String userId = jwtUtil.extractUserId(CommonMethods.parseNullString(request.getHeader("Access-Token")));

        JSONObject userObj = new JSONObject(obj);
        GenericResponse rsp = new GenericResponse();
        String emptyField = null;

        if(!userObj.has("email") || CommonMethods.parseNullString(userObj.getString("email")).isEmpty()){
            emptyField = "Email";
        }else if(!userObj.has("name") || CommonMethods.parseNullString(userObj.getString("name")).isEmpty()){
            emptyField = "Name";
        }else if(!userObj.has("password") || CommonMethods.parseNullString(userObj.getString("password")).isEmpty()){
            emptyField = "Password";
        }else if(!userObj.has("profile_id") || CommonMethods.parseNullInt(userObj.getInt("profile_id"))==0){
            emptyField = "Profile";
        }

        if(!CommonMethods.parseNullString(emptyField).isEmpty()){
            rsp.setStatus(0);
            rsp.setMessage(emptyField+" is required");
        }else {
            JSONObject profileObj = profileService.getProfileWithId(userObj.getInt("profile_id"));
            if(profileObj==null || profileObj.isEmpty()){
                rsp.setStatus(0);
                rsp.setMessage("Profile id is invalid");
            }else if(profileObj.getString("role").equalsIgnoreCase("super_admin")){
                rsp.setStatus(0);
                rsp.setMessage("Super admin profile can not be assigned");
            }else if(userService.findByEmail(userObj.getString("email"))==null) {
                userObj.put("created_by",userId);
                if (userService.createUser(userObj)) {
                    rsp.setData(userService.findByIdOrEmail(null,userObj.getString("email")).getJSONObject("data"));
                    rsp.setStatus(1);
                    rsp.setMessage("User created successfully");
                } else {
                    rsp.setStatus(0);
                    rsp.setMessage("Error creating user");
                }
            }else{
                rsp.setStatus(0);
                rsp.setMessage("User with this email already present");
            }
        }
        return ResponseEntity.status(200).body(rsp.rspToJson().toString());
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateUserApi(HttpServletRequest request,@RequestBody String obj,@PathVariable int id){
        String userId = jwtUtil.extractUserId(CommonMethods.parseNullString(request.getHeader("Access-Token")));

        JSONObject userObj = new JSONObject(obj);
        GenericResponse rsp = new GenericResponse();
        String emptyField = null;
        if(!userObj.has("email") || CommonMethods.parseNullString(userObj.getString("email")).isEmpty()){
            emptyField = "Email";
        }else if(!userObj.has("name") || CommonMethods.parseNullString(userObj.getString("name")).isEmpty()){
            emptyField = "Name";
        }else if(CommonMethods.parseNullInt(id)==0){
            emptyField = "User id";
        }else if(!userObj.has("profile_id") || CommonMethods.parseNullInt(userObj.getInt("profile_id"))==0){
            emptyField = "Profile";
        }

        if(!CommonMethods.parseNullString(emptyField).isEmpty()){
            rsp.setStatus(0);
            rsp.setMessage(emptyField+" is required");
        }else {
            userObj.put("updated_by",userId);
            JSONObject profileObj = profileService.getProfileWithId(userObj.getInt("profile_id"));
            if(!profileService.isProfileValid(userObj.getInt("profile_id")) || profileObj.isEmpty()){
                rsp.setStatus(0);
                rsp.setMessage("Profile id is invalid");
            }else if(profileObj.getString("role").equalsIgnoreCase("super_admin")){
                rsp.setStatus(0);
                rsp.setMessage("Super admin profile can not be assigned");
            }else if(userService.updateUser(userObj,id)){
                rsp.setData(userService.findByIdOrEmail(String.valueOf(id),null).getJSONObject("data"));
                rsp.setStatus(1);
                rsp.setMessage("User updated successfully");
            }else{
                rsp.setStatus(0);
                rsp.setMessage("Error updating user");
            }

        }
        return ResponseEntity.status(200).body(rsp.rspToJson().toString());
    }

    @DeleteMapping
    public ResponseEntity<String> deleteUserApi(@RequestParam("id") String id){
        GenericResponse rsp = new GenericResponse();
        if(CommonMethods.parseNullInt(id)==0){
            rsp.setStatus(0);
            rsp.setMessage("User id is missing");
        }else{

            if(userService.findByIdOrEmail(id,null).getJSONObject("data").getString("role").equalsIgnoreCase("super_admin")){
                rsp.setStatus(0);
                rsp.setMessage("Super admin user can not be deleted");
            }else{
                if(userService.deleteUser(id)){
                    rsp.setStatus(1);
                    rsp.setMessage("User deleted successfully");
                }else{
                    rsp.setStatus(0);
                    rsp.setMessage("Error deleting user");
                }
            }
        }
        return ResponseEntity.status(200).body(rsp.rspToJson().toString());
    }
}
