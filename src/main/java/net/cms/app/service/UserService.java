package net.cms.app.service;

import lombok.RequiredArgsConstructor;
import net.cms.app.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService{
    @Autowired
    private JdbcTemplate jdbc;

    public User findByEmail(String email) {
        User user = new User();
        String query = "select u.*,p.role from users u left join profiles p on p.id=u.profile_id where email = ?";
        Boolean isUserFound = false;
        try{
            jdbc.execute(query,(PreparedStatementCallback<Void>) ps->{
                try{
                    ps.setString(1,email);
                    ResultSet rs = ps.executeQuery();
                    if(rs.next()){
                        user.setUserId(rs.getString("id"));
                        user.setEmail(rs.getString("email"));
                        user.setName(rs.getString("name"));
                        user.setRole(rs.getString("role"));
                        user.setPassword(rs.getString("password"));
                        user.setPhoneNumber(rs.getString("phone_number"));
                    }
                }catch(Exception e){
                    e.printStackTrace();
                    throw e;
                }
                return null;
            });
        }catch (Exception e){
            throw e;
        }

        if(user.getEmail() != null){
            return user;
        }else{
            throw new UsernameNotFoundException("User '"+email+"' Not Found");
        }
    }
}
