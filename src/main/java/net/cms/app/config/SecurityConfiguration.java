package net.cms.app.config;

import lombok.RequiredArgsConstructor;
import net.cms.app.filter.AuthorizationFilter;
import net.cms.app.filter.UserAuthenticationFilter;
import net.cms.app.service.MyUserDetailsService;
import net.cms.app.service.UserService;
import net.cms.app.service.impl.MyUserDetailsServiceImpl;
import net.cms.app.utility.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    @Autowired
    private AuthenticationConfiguration authenticationConfiguration;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        UserAuthenticationFilter userAuthenticationFilter = new UserAuthenticationFilter(authenticationManager(authenticationConfiguration),getJwtUtil(),getUserService());
        userAuthenticationFilter.setFilterProcessesUrl("/api/login");

        http.csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(request -> request
                .requestMatchers("/api/signup","/api/pswdBcrypt").permitAll()
                .requestMatchers("/api/admin/**","/api/cms/**").permitAll()
                .anyRequest().authenticated()
        )
        .sessionManagement(manager -> manager.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authenticationProvider(authenticationProvider())
        .addFilterBefore(authorizationFilter(), UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(userAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(myUserDetailsServiceImpl().userDetailsService());
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Bean
    public MyUserDetailsServiceImpl myUserDetailsServiceImpl(){
        return new MyUserDetailsServiceImpl();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception{
        return config.getAuthenticationManager();
    }

    @Bean
    public UserService getUserService(){
        return new UserService(passwordEncoder());
    }

    @Bean
    public JwtUtil getJwtUtil(){
        return new JwtUtil();
    }

    @Bean
    public AuthorizationFilter authorizationFilter(){
        return new AuthorizationFilter();
    }
}
