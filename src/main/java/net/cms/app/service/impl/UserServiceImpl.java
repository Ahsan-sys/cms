package net.cms.app.service.impl;

import lombok.RequiredArgsConstructor;
import net.cms.app.service.UserService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    @Override
    public UserDetailsService userDetailsService() {
        return null;
    }
}
