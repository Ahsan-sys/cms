package net.cms.app.controller;


import lombok.AllArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/user")
public class UserController {

    @Autowired
    private PasswordEncoder passwordEncoder;
//    @PostMapping
//    public ResponseEntity<JSONObject> createUser(@RequestBody JSONObject obj){
//        String query = "insert into users (name,password,email,phone_number,profile_id) values (?,?,?,?,?)";
//
//    }
}
