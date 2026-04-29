package appsembly.appsembly.controller;

import java.util.Collection;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AppsemblyController {
    @GetMapping({ "/", "/index" })
    public String index(@RequestParam(required = false) String error, ModelMap model) {
        if (error != null) {
            model.addAttribute("error", "usuario o contraseña invalido");
        }
        return "index";
    }

    @GetMapping("/inicio")
    public String inicio(Authentication authentication) {
        if (authentication != null) {
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            for (GrantedAuthority authority : authorities) {
                if (authority.getAuthority().equals("ROLE_ADMIN")) {
                    return "redirect:/admin/dashboard";
                }
            }
        }
        return "inicio";
    }
    @GetMapping("/adminpanel")
    public String adminPanel() {
        return "adminpanel";
    }
    
    @GetMapping("/pregunta")
    public String pregunta() {
        return "pregunta";
    }
    
    @GetMapping("/resultados")
    public String resultados() {
        return "resultados";
    }
    
    @GetMapping("/historial")
    public String historial() {
        return "historial";
    }
}
