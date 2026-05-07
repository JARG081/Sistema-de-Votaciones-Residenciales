package appsembly.appsembly.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import appsembly.appsembly.dto.DashboardStatsDTO;
import appsembly.appsembly.dto.ParticipationChartDTO;
import appsembly.appsembly.dto.SurveyHistoryItemDTO;
import appsembly.appsembly.dto.SurveyResultDTO;
import appsembly.appsembly.dto.SystemMetricsDTO;
import appsembly.appsembly.service.ApplicationMetrics;
import appsembly.appsembly.service.SurveyService;

@RestController
@RequestMapping("/data")
@CrossOrigin(origins = "*")
public class DataRestController {

    private final SurveyService surveyService;
    private final ApplicationMetrics applicationMetrics;

    public DataRestController(SurveyService surveyService, ApplicationMetrics applicationMetrics) {
        this.surveyService = surveyService;
        this.applicationMetrics = applicationMetrics;
    }

    @GetMapping("/dashboard-stats")
    public List<DashboardStatsDTO> getDashboardStats() {
        return surveyService.getDashboardStats();
    }

    @GetMapping("/participation-chart")
    public ParticipationChartDTO getParticipationChart() {
        return surveyService.getParticipationChart();
    }

    @GetMapping("/current-survey")
    public SurveyResultDTO getCurrentSurvey() {
        return surveyService.getCurrentSurvey();
    }

    @GetMapping("/survey-history")
    public List<SurveyHistoryItemDTO> getSurveyHistory() {
        return surveyService.getSurveyHistory();
    }

    @GetMapping("/survey-question")
    public SurveyResultDTO getSurveyQuestion() {
        return surveyService.getSurveyQuestion();
    }

    @GetMapping("/results-dataset")
    public ParticipationChartDTO getResultsDataset() {
        return surveyService.getResultsDataset();
    }

    @GetMapping("/metrics")
    public SystemMetricsDTO getMetrics() {
        return applicationMetrics.snapshot();
    }
}
