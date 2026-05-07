package appsembly.appsembly.config;

import java.io.IOException;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.fasterxml.jackson.databind.ObjectMapper;

import appsembly.appsembly.service.UserServiceImpl;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
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
            .requestMatchers("/logincheck", "/logout", "/registration", "/user/save", "/survey/save",
                "/admin/survey/save", "/ingresar_codigo", "/data/**")
            .permitAll()
                .anyRequest().permitAll())
                .sessionManagement(session -> session
                        .invalidSessionUrl("/login?invalid=true"))
                .formLogin(formLogin -> formLogin
                        .loginProcessingUrl("/logincheck")
                        .usernameParameter("email")
                        .passwordParameter("password")
                .successHandler((request, response, authentication) -> {
                    String redirectTo = "/inicio";
                    Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
                    for (GrantedAuthority authority : authorities) {
                    if ("ROLE_ADMIN".equals(authority.getAuthority())) {
                        redirectTo = "/admin/dashboard";
                        break;
                    }
                    }
                    writeJson(response, 200,
                        new ApiAuthResponse(true, "Inicio de sesión exitoso", redirectTo));
                })
                .failureHandler((request, response, exception) -> {
                    String message = "Usuario o contraseña inválido";
                    if (exception.getMessage().contains("UsernameNotFoundException")) {
                        message = "Usuario no encontrado";
                    } else if (exception.getMessage().contains("BadCredentialsException")) {
                        message = "Contraseña incorrecta";
                    } else if (exception.getMessage().contains("DisabledException")) {
                        message = "Usuario deshabilitado";
                    }
                    writeJson(response, 401, new ApiAuthResponse(false, message, null));
                }))
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler((request, response, authentication) -> writeJson(response, 200,
                            new ApiAuthResponse(true, "Sesión cerrada correctamente", "/")))
                        .permitAll())
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/ingresar_codigo", "/logincheck"))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> 
                            writeJson(response, 401, new ApiAuthResponse(false, "Autenticación requerida. Tu sesión puede haber expirado.", null)))
                        .accessDeniedHandler((request, response, accessDeniedException) -> 
                            writeJson(response, 403, new ApiAuthResponse(false, "Acceso denegado. Permiso insuficiente.", null))))
                .build();
    }

        private void writeJson(jakarta.servlet.http.HttpServletResponse response, int status, ApiAuthResponse payload)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        new ObjectMapper().writeValue(response.getWriter(), payload);
        }

        private record ApiAuthResponse(boolean success, String message, String redirectTo) {
        }
}
