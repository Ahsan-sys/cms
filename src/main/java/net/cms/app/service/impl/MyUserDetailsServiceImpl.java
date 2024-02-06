package net.cms.app.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cms.app.service.MyUserDetailsService;
import net.cms.app.service.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
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
                    log.debug(e.getMessage() + " || Trace: "+e.getStackTrace()[0]+ " || "+e.getStackTrace()[1]);
                    throw e;
                }
            }
        };
    }
}
