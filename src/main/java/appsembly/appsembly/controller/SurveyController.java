package appsembly.appsembly.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import appsembly.appsembly.service.SurveyService;

@RestController
public class SurveyController {

    private final SurveyService surveyService;

    public SurveyController(SurveyService surveyService) {
        this.surveyService = surveyService;
    }

    @PostMapping({ "/survey/save", "/admin/survey/save" })
    public Map<String, Object> saveSurvey(@RequestParam String title, @RequestParam String question,
            @RequestParam(required = false) List<String> respuestas,
            @RequestParam(required = false) String expirationDate,
            @RequestParam(required = false) String audienceMode,
            @RequestParam(required = false) String audienceBlocks,
            @RequestParam(required = false) String audienceTowers,
            @RequestParam(required = false) String votePrivacy,
            @RequestParam(required = false) String actor,
            @RequestParam(required = false) String status) {
        surveyService.saveSurvey(title, question, respuestas, expirationDate, audienceMode, audienceBlocks, audienceTowers, votePrivacy, actor, status);
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("message", "Encuesta guardada correctamente.");
            response.put("surveyTitle", title);
            response.put("surveyQuestion", question);
            response.put("surveyAnswers", respuestas == null ? List.of() : respuestas);
            response.put("expirationDate", expirationDate == null ? "" : expirationDate);
            response.put("audienceMode", audienceMode == null ? "ALL" : audienceMode);
            response.put("audienceBlocks", audienceBlocks == null ? "" : audienceBlocks);
            response.put("audienceTowers", audienceTowers == null ? "" : audienceTowers);
            response.put("votePrivacy", votePrivacy == null ? "ANONYMOUS" : votePrivacy);
            response.put("status", status == null ? "OPEN" : status);
            return response;
    }
}