package net.cms.app.service.impl;

import lombok.RequiredArgsConstructor;
import net.cms.app.service.MyUserDetailsService;
import net.cms.app.service.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MyUserDetailsServiceImpl implements MyUserDetailsService {

    private final UserService userService;
    @Override
    public UserDetailsService userDetailsService() {
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
                try{
                    return userService.findByEmail(username);
                }catch (Exception e){
                    e.printStackTrace();
                    throw e;
                }
            }
        };
    }
}
