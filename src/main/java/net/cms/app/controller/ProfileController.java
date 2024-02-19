package net.cms.app.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.websocket.server.PathParam;
import lombok.AllArgsConstructor;
import net.cms.app.response.GenericResponse;
import net.cms.app.service.ProfileService;
import net.cms.app.utility.CommonMethods;
import net.cms.app.utility.JwtUtil;
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

    @Autowired
    private JwtUtil jwtUtil;

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

    @GetMapping
    public ResponseEntity<String> getProfileAuthorizedUrls(@RequestParam String roleId){
        GenericResponse rsp = new GenericResponse();
        rsp.setDataArray(profileService.getAllAuhtorizedUrls(roleId));
        return ResponseEntity.ok(rsp.rspToJson().toString());

    }
    @PostMapping
    public ResponseEntity<String> createProfileApi(HttpServletRequest request, @RequestBody String obj){
        String accessToken = CommonMethods.parseNullString(request.getHeader("Access-Token"));
        String userId = jwtUtil.extractUserId(accessToken);

        GenericResponse rsp = new GenericResponse();
        JSONObject profileObj = new JSONObject(obj);
        if(profileObj.isEmpty()){
            rsp.setStatus(0);
            rsp.setMessage("Required data is missing");
        }else {
            String role = "";
            String name = "";
            if (profileObj.has("role")) {
                role = CommonMethods.parseNullString(profileObj.getString("role")).replaceAll(" ", "_");
            }
            if (profileObj.has("name")) {
                name = CommonMethods.parseNullString(profileObj.getString("name"));
            }

            if (role.isEmpty()) {
                rsp.setStatus(0);
                rsp.setMessage("Role is required");
            } else if (name.isEmpty()) {
                rsp.setStatus(0);
                rsp.setMessage("Role Name is required");
            } else if(!profileService.isRoleAndNameExist(role,name)){
                rsp.setStatus(0);
                rsp.setMessage("Role or name already exists");
            } else if (role.equalsIgnoreCase("super_admin")) {
                rsp.setStatus(0);
                rsp.setMessage("super_admin role can not be created");
            } else if (name.equalsIgnoreCase("Super Admin")) {
                rsp.setStatus(0);
                rsp.setMessage("Super Admin role can not be created");
            }else if (!profileObj.has("authorities") || profileObj.getJSONArray("authorities").length() <= 0) {
                rsp.setStatus(0);
                rsp.setMessage("Authorities are missing");
            } else {
                profileObj.put("created_by", userId);
                if (profileService.createRole(profileObj)) {
                    rsp.setStatus(1);
                    rsp.setMessage("Role created successfully");
                } else {
                    rsp.setStatus(0);
                    rsp.setMessage("Error creating role");
                }
            }
        }
        return ResponseEntity.ok(rsp.rspToJson().toString());
    }

    @PutMapping
    public ResponseEntity<String> updateProfileApi(HttpServletRequest request, @RequestBody String obj){
        String accessToken = CommonMethods.parseNullString(request.getHeader("Access-Token"));
        String userId = jwtUtil.extractUserId(accessToken);

        GenericResponse rsp = new GenericResponse();
        JSONObject profileObj = new JSONObject(obj);
        if(profileObj.isEmpty()){
            rsp.setStatus(0);
            rsp.setMessage("Required data is missing");
        }else {
            if(!profileObj.has("authorities") || profileObj.getJSONArray("authorities").length() <= 0) {
                rsp.setStatus(0);
                rsp.setMessage("Authorities are missing");
            }else if(!profileObj.has("profile_id") || CommonMethods.parseNullString(profileObj.getString("profile_id")).isEmpty()){
                rsp.setStatus(0);
                rsp.setMessage("Profile id is missing");
            }else if(profileService.getProfileWithId(Integer.parseInt(profileObj.getString("profile_id"))).getString("role").equalsIgnoreCase("super_admin")){
                rsp.setStatus(0);
                rsp.setMessage("Can not update role super admin");
            }else{
                profileObj.put("updated_by",userId);
                if(profileService.updateRole(profileObj)){
                    rsp.setMessage("Role updated successfully");
                }else{
                    rsp.setStatus(0);
                    rsp.setMessage("Error updating role");
                }
            }
        }
        return ResponseEntity.ok(rsp.rspToJson().toString());
    }

    @DeleteMapping
    public ResponseEntity<String> deleteProfileApi(@RequestParam int id){
        GenericResponse rsp = new GenericResponse();

        if(CommonMethods.parseNullInt(id)==0){
            rsp.setStatus(0);
            rsp.setMessage("Profile id is required");
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
