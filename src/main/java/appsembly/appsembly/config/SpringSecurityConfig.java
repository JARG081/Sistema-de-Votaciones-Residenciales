package appsembly.appsembly.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import appsembly.appsembly.service.UserServiceImpl;

@Configuration
public class SpringSecurityConfig {
    @Autowired
    public UserServiceImpl userServiceImpl;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userServiceImpl).passwordEncoder(new BCryptPasswordEncoder());
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(authHttp -> authHttp
                .requestMatchers("/index").permitAll()
                .requestMatchers("/inicio").authenticated()
                .requestMatchers("/admin/dashboard").hasRole("ADMIN")
                .anyRequest().permitAll())
                .formLogin(formLogin -> formLogin
                        .loginPage("/index")
                        .loginProcessingUrl("/logincheck")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/inicio", true))
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/index")
                        .permitAll())
                .csrf(csrf -> csrf.disable())
                .build();
    }
}
