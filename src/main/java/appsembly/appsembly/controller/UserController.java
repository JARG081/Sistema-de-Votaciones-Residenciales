package appsembly.appsembly.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import appsembly.appsembly.service.UserService;

@RestController
@RequestMapping("user")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping
    public Map<String, Object> indexUser() {
        return Map.of("success", true, "message", "Módulo de usuario activo en modo API.");
    }

    @GetMapping("/save")
    public Map<String, Object> showSaveForm() {
        return Map.of("success", true, "message", "Usa POST /user/save para crear usuarios.");
    }

    @PostMapping("/save")
    public Map<String, Object> saveUser(@RequestParam String email, @RequestParam String password,
            @RequestParam String role) {
        try {
            userService.register(null, null, email, password, password, role, null, true);
            return Map.of("success", true, "message", "Usuario creado correctamente.");
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }
}
