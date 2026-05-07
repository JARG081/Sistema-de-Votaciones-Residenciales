package appsembly.appsembly.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import appsembly.appsembly.service.SurveyService;

@RestController
public class AppsemblyController {

    @Autowired
    private SurveyService surveyService;

    @PostMapping("/ingresar_codigo")
    public Map<String, Object> ingresarCodigo(@RequestParam(required = false) String codigo) {
        String normalizedCode = codigo == null ? "" : codigo.trim();
        if (normalizedCode.isBlank()) {
            return Map.of("success", false, "message", "Debes ingresar un código válido.");
        }

        surveyService.validateVoteAccess(normalizedCode);

        return Map.of("success", true, "message", "Código validado correctamente.", "redirectTo", "/pregunta", "residentCode", normalizedCode);
    }
}
