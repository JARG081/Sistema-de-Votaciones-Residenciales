package appsembly.appsembly.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import appsembly.appsembly.service.UserService;

@RestController
@RequestMapping("/registration")
public class RegisterController {

    @Autowired
    private UserService userService;

    @PostMapping
    public Map<String, Object> register(@RequestParam String firstName, @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam String password, @RequestParam String passwordConfirm, @RequestParam String role,
            @RequestParam(required = false) Integer personalCode) {
        try {
            userService.register(firstName, lastName, email, password, passwordConfirm, role, personalCode, true);
            return Map.of("success", true, "message", "Usuario registrado correctamente.");
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @GetMapping
    public Map<String, Object> registrationInfo() {
        return Map.of("success", true, "message", "Usa POST /registration para registrar usuarios.");
    }

}
