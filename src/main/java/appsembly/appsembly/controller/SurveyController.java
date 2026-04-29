package appsembly.appsembly.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SurveyController {

    @PostMapping({ "/survey/save", "/admin/survey/save" })
    public String saveSurvey(@RequestParam String title, @RequestParam String question,
            @RequestParam(required = false) List<String> respuestas, ModelMap model) {
        model.put("surveySaved", true);
        model.put("surveyTitle", title);
        model.put("surveyQuestion", question);
        model.put("surveyAnswers", respuestas);
        return "redirect:/admin/dashboard";
    }
}