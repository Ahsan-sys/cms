package net.cms.app.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.cms.app.service.MyUserDetailsService;
import net.cms.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Slf4j
public class MyUserDetailsServiceImpl implements MyUserDetailsService {

    @Autowired
    private UserService userService;

    @Override
    public UserDetailsService userDetailsService() {
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
                try{
                    return userService.findByEmail(username);
                }catch (Exception e){
                    log.debug(e.getMessage() + " || Trace: "+e.getStackTrace()[0]+ " || "+e.getStackTrace()[1]);
                    throw e;
                }
            }
        };
    }
}
