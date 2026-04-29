package appsembly.appsembly.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;

import appsembly.appsembly.service.UserService;

@Controller
@RequestMapping("user")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping
    public String indexUser() {
        return "user/index";
    }

    @GetMapping("/save")
    public String showSaveForm() {
        return "user/save";
    }

    @PostMapping("/save")
    public String saveUser(@RequestParam String email, @RequestParam String password,
            @RequestParam String role, ModelMap model) {
        try {
            userService.register(null, null, email, password, password, role, null, true);
            model.put("successful", "usuario creado correctamente!");
        } catch (Exception e) {
            model.put("error", e.getMessage());
        }
        return "user/save";
    }
}
