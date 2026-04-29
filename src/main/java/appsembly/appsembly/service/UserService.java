package appsembly.appsembly.service;

import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    void register(String firstName, String lastName, String email, String password, String passwordConfirm,
            String role, Integer personalCode, Boolean newUser) throws Exception;
}
