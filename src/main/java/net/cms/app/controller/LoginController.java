package net.cms.app.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.cms.app.response.GenericResponse;
import net.cms.app.service.UserService;
import net.cms.app.utility.CommonMethods;
import net.cms.app.utility.JwtUtil;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path="/api")
@AllArgsConstructor
public class LoginController {

    @Autowired
    private final UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/signup")
    public ResponseEntity<String> signupApi(@RequestBody String obj){
        JSONObject userObj = new JSONObject(obj);
        GenericResponse rsp = new GenericResponse();
        String emptyField = null;

        if(!userObj.isEmpty()) {
            if (!userObj.has("email") || CommonMethods.parseNullString(userObj.getString("email")).isEmpty()) {
                emptyField = "Email";
            } else if (!userObj.has("phone_number") || CommonMethods.parseNullString(userObj.getString("phone_number")).isEmpty()) {
                emptyField = "Phone Number";
            } else if (!userObj.has("name") || CommonMethods.parseNullString(userObj.getString("name")).isEmpty()) {
                emptyField = "Name";
            } else if (!userObj.has("password") || CommonMethods.parseNullString(userObj.getString("password")).isEmpty()) {
                emptyField = "Password";
            }

            if (userObj.has("profile_id")) {
                userObj.remove("profile_id");
            }else if (userObj.has("updated_by")) {
                userObj.remove("updated_by");
            }else if (userObj.has("created_by")) {
                userObj.remove("created_by");
            }

            if (!CommonMethods.parseNullString(emptyField).isEmpty()) {
                rsp.setStatus(0);
                rsp.setMessage(emptyField + " is required");
            } else {
                if (userService.findByEmail(userObj.getString("email")) == null) {
                    if (userService.createUser(userObj)) {
                        rsp.setStatus(1);
                        rsp.setMessage("User created successfully");
                    } else {
                        rsp.setStatus(0);
                        rsp.setMessage("Error creating user");
                    }
                }else{
                    rsp.setStatus(0);
                    rsp.setMessage("Email is not unique");
                }
            }
        }else{
            rsp.setStatus(0);
            rsp.setMessage("Required data missing");
        }
        return ResponseEntity.status(200).body(rsp.rspToJson().toString());
    }

    @PostMapping("/pswdBcrypt")
    public ResponseEntity<String> getBcryptPswrd(@RequestBody String pswrd){
        return ResponseEntity.status(200).body(userService.bcryptPassword(pswrd));
    }

    @PutMapping("/cms/updateUser/{id}")
    public ResponseEntity<String> updateUserApi(HttpServletRequest request, @RequestBody String obj,@PathVariable String id){
        String accessToken = CommonMethods.parseNullString(request.getHeader("Access-Token"));
        String userId = jwtUtil.extractUserId(accessToken);
        GenericResponse rsp = new GenericResponse();

        if(userId.equals(id)) {
            JSONObject userObj = new JSONObject(obj);
            String emptyField = null;
            if (!userObj.has("email") || CommonMethods.parseNullString(userObj.getString("email")).isEmpty()) {
                emptyField = "Email";
            } else if (!userObj.has("phone_number") || CommonMethods.parseNullString(userObj.getString("phone_number")).isEmpty()) {
                emptyField = "Phone Number";
            } else if (!userObj.has("name") || CommonMethods.parseNullString(userObj.getString("name")).isEmpty()) {
                emptyField = "Name";
            } else if (CommonMethods.parseNullInt(id) == 0) {
                emptyField = "User id";
            }

            if (userObj.has("profile_id")) {
                userObj.remove("profile_id");
            }else if (userObj.has("updated_by")) {
                userObj.remove("updated_by");
            }else if (userObj.has("created_by")) {
                userObj.remove("created_by");
            }

            if (!CommonMethods.parseNullString(emptyField).isEmpty()) {
                rsp.setStatus(0);
                rsp.setMessage(emptyField + " is required");
            } else {
                if (userService.updateUser(userObj, Integer.parseInt(id))) {
                    rsp.setStatus(1);
                    rsp.setMessage("User updated successfully");
                } else {
                    rsp.setStatus(0);
                    rsp.setMessage("Error updating user");
                }
            }
        }else{
            rsp.setStatus(0);
            rsp.setMessage("Invalid user id");
        }
        return ResponseEntity.status(200).body(rsp.rspToJson().toString());
    }

    @DeleteMapping("/cms/logout")
    public ResponseEntity<String> logOutApi(HttpServletRequest request, @RequestParam("id") String userId){
        String accessToken = CommonMethods.parseNullString(request.getHeader("Access-Token"));
        String id = jwtUtil.extractUserId(accessToken);
        GenericResponse rsp = new GenericResponse();

        if(id.equals(userId)) {
            userService.deleteUserSession(userId);
            rsp.setMessage("User logged out successfully");
        }else{
            rsp.setMessage("Invalid user id");
            rsp.setStatus(0);
        }
        return ResponseEntity.status(200).body(rsp.rspToJson().toString());
    }

    @PostMapping("/cms/refresh_token")
    public ResponseEntity<String> refreshTokenApi(HttpServletRequest request){
        String accessToken = CommonMethods.parseNullString(request.getHeader("Access-Token"));
        String userId = jwtUtil.extractUserId(accessToken);
        String role = jwtUtil.extractUserRole(accessToken);
        String email = jwtUtil.extractUserName(accessToken);


        String newAccessToken = jwtUtil.generateAccessToken(userId,email,role);
        String newRefreshToken = jwtUtil.generateRefreshToken(userId);

        JSONObject rsp = new JSONObject();
        rsp.put("Access-Token",newAccessToken);
        rsp.put("Refresh-Token",newRefreshToken);
        userService.setToken(Integer.parseInt(userId),newAccessToken,newRefreshToken);

        return ResponseEntity.status(200).body(rsp.toString());
    }
}
