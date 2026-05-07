package appsembly.appsembly.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import appsembly.appsembly.service.SurveyService;

@RestController
public class VoteController {

    private final SurveyService surveyService;

    public VoteController(SurveyService surveyService) {
        this.surveyService = surveyService;
    }

    @PostMapping("/vote/submit")
    public Map<String, Object> submitVote(@RequestParam String voterCode, @RequestParam String selectedOption) {
        surveyService.submitVote(voterCode, selectedOption);
        return Map.of("success", true, "message", "Voto registrado correctamente.");
    }
}