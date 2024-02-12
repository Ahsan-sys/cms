package net.cms.app.controller;

import lombok.AllArgsConstructor;
import net.cms.app.response.GenericResponse;
import net.cms.app.service.UserService;
import net.cms.app.utility.CommonMethods;
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

    @GetMapping
    public ResponseEntity<String> getAllUsersApi(){
        GenericResponse rsp = new GenericResponse();
        JSONArray rspArray = userService.getAllUsers();
        rsp.setDataArray(rspArray);
        return ResponseEntity.ok(rsp.rspToJson().toString());
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getUserApi(@PathVariable int id){
        GenericResponse rsp = new GenericResponse();
        JSONObject obj = userService.getUserWithId(id);
        if(obj==null){
            rsp.setStatus(0);
            rsp.setMessage("Error getting data for user");
        }else{
            rsp.setData(obj);
        }
        return ResponseEntity.ok(rsp.rspToJson().toString());
    }

    @PostMapping
    public ResponseEntity<String> createUserApi(@RequestBody String obj){
        JSONObject userObj = new JSONObject(obj);
        GenericResponse rsp = new GenericResponse();
        String emptyField = null;

        if(CommonMethods.parseNullString(userObj.getString("email")).isEmpty()){
            emptyField = "Email";
        }else if(CommonMethods.parseNullString(userObj.getString("phone_number")).isEmpty()){
            emptyField = "Phone Number";
        }else if(CommonMethods.parseNullString(userObj.getString("name")).isEmpty()){
            emptyField = "Name";
        }else if(CommonMethods.parseNullString(userObj.getString("password")).isEmpty()){
            emptyField = "Password";
        }else if(CommonMethods.parseNullInt(userObj.getInt("profile_id"))==0){
            emptyField = "Profile";
        }

        if(!CommonMethods.parseNullString(emptyField).isEmpty()){
            rsp.setStatus(0);
            rsp.setMessage(emptyField+" is required");
        }else {
            if(userService.findByEmail(userObj.getString("email"))==null) {
                if (userService.createUser(userObj)) {
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
    public ResponseEntity<String> updateUserApi(@RequestBody String obj,@PathVariable int id){
        JSONObject userObj = new JSONObject(obj);
        GenericResponse rsp = new GenericResponse();
        String emptyField = null;
        if(CommonMethods.parseNullString(userObj.getString("email")).isEmpty()){
            emptyField = "Email";
        }else if(CommonMethods.parseNullString(userObj.getString("phone_number")).isEmpty()){
            emptyField = "Phone Number";
        }else if(CommonMethods.parseNullString(userObj.getString("name")).isEmpty()){
            emptyField = "Name";
        }else if(CommonMethods.parseNullInt(id)==0){
            emptyField = "User id";
        }else if(CommonMethods.parseNullInt(userObj.getInt("profile_id"))==0){
            emptyField = "Profile";
        }

        if(CommonMethods.parseNullString(emptyField).isEmpty()){
            rsp.setStatus(0);
            rsp.setMessage(emptyField+" is required");
        }else {
            if(userService.updateUser(userObj,id)){
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
            if(userService.deleteUser(id)){
                rsp.setStatus(1);
                rsp.setMessage("User deleted successfully");
            }else{
                rsp.setStatus(0);
                rsp.setMessage("Error deleting user");
            }
        }
        return ResponseEntity.status(200).body(rsp.rspToJson().toString());
    }
}
