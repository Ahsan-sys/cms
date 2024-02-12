package net.cms.app.controller;

import jakarta.websocket.server.PathParam;
import lombok.AllArgsConstructor;
import net.cms.app.response.GenericResponse;
import net.cms.app.service.ProfileService;
import net.cms.app.utility.CommonMethods;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path="/api/admin/profile")
@AllArgsConstructor
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @GetMapping
    public ResponseEntity<String> getProfilesApi(){
        GenericResponse rsp = new GenericResponse();
        JSONArray rspArray = profileService.getAllProfiles();
        rsp.setDataArray(rspArray);
        return ResponseEntity.ok(rsp.rspToJson().toString());

    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getProfileApi(@PathVariable int id){
        GenericResponse rsp = new GenericResponse();
        JSONObject obj = profileService.getProfileWithId(id);
        if(obj==null){
            rsp.setStatus(0);
            rsp.setMessage("Error getting data for user");
        }else{
            rsp.setData(obj);
        }
        return ResponseEntity.ok(rsp.rspToJson().toString());

    }
    @PostMapping
    public ResponseEntity<String> createProfileApi(@RequestBody String obj){
        GenericResponse rsp = new GenericResponse();
        JSONObject profileObj = new JSONObject(obj);

        String role = CommonMethods.parseNullString(profileObj.getString("role"));

        if(!profileObj.has("authorities")) {
            rsp.setStatus(0);
            rsp.setMessage("Authorities are missing");
        }else if(role.isEmpty()){
            rsp.setStatus(0);
            rsp.setMessage("Role is required");
        }else if(role.equalsIgnoreCase("super_admin")){
            rsp.setStatus(0);
            rsp.setMessage("This role can not be created");
        }else{

            if(profileService.createRole(profileObj)){
                rsp.setStatus(1);
                rsp.setMessage("Role created successfully");
            }else{
                rsp.setStatus(0);
                rsp.setMessage("Error creating role");
            }
        }
        return ResponseEntity.ok(rsp.rspToJson().toString());
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateProfileApi(@RequestBody String obj,@PathVariable int id){
        GenericResponse rsp = new GenericResponse();
        JSONObject profileObj = new JSONObject(obj);
        String role = CommonMethods.parseNullString(profileObj.getString("role"));

        if(!profileObj.has("authorities")) {
            rsp.setStatus(0);
            rsp.setMessage("Authorities are missing");
        }else if(CommonMethods.parseNullString(role).isEmpty()){
            rsp.setStatus(0);
            rsp.setMessage("Role is required");
        }else if(CommonMethods.parseNullInt(id)==0){
            rsp.setStatus(0);
            rsp.setMessage("Profile id is required");
        }else if(role.equalsIgnoreCase("super_admin")){
            rsp.setStatus(0);
            rsp.setMessage("Can not update role super admin");
        }else if(profileService.getProfileWithId(id).getString("role").equalsIgnoreCase("super_admin")){
            rsp.setStatus(0);
            rsp.setMessage("Can not update role super admin");
        }else{
            if(profileService.updateRole(profileObj,id)){
                rsp.setStatus(1);
                rsp.setMessage("Role updated successfully");
            }else{
                rsp.setStatus(0);
                rsp.setMessage("Error updating role");
            }
        }
        return ResponseEntity.ok(rsp.rspToJson().toString());
    }

    @DeleteMapping
    public ResponseEntity<String> deleteProfileApi(@RequestBody int id){
        GenericResponse rsp = new GenericResponse();

        if(CommonMethods.parseNullInt(id)==0){
            rsp.setStatus(0);
            rsp.setMessage("Role is required");
        }else if(profileService.getProfileWithId(id).getString("role").equalsIgnoreCase("super_admin")){
            rsp.setStatus(0);
            rsp.setMessage("Can not delete role super admin");
        }else{
            if(!profileService.profileHasUsers(id)){
                if(profileService.deleteRole(id)){
                    rsp.setStatus(1);
                    rsp.setMessage("Role is deleted successfully");
                }else{
                    rsp.setStatus(0);
                    rsp.setMessage("Error deleting role");
                }
            }else{
                rsp.setStatus(0);
                rsp.setMessage("Users have this profile assigned. Can not delete it.");
            }
        }
        return ResponseEntity.ok(rsp.rspToJson().toString());
    }
}
