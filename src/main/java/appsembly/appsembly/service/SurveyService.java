package appsembly.appsembly.service;

import java.util.List;

import appsembly.appsembly.dto.DashboardStatsDTO;
import appsembly.appsembly.dto.ParticipationChartDTO;
import appsembly.appsembly.dto.ParticipationSummaryDTO;
import appsembly.appsembly.dto.SurveyHistoryItemDTO;
import appsembly.appsembly.dto.SurveyResultDTO;
import appsembly.appsembly.dto.SurveyVoteDetailDTO;
import appsembly.appsembly.dto.AuditLogDTO;
import appsembly.appsembly.domain.Survey;

public interface SurveyService {
    SurveyResultDTO getCurrentSurvey();
    SurveyResultDTO getSurveyQuestion();
    ParticipationChartDTO getParticipationChart();
    ParticipationChartDTO getResultsDataset();
    List<DashboardStatsDTO> getDashboardStats();
    List<SurveyHistoryItemDTO> getSurveyHistory();
    void saveSurvey(String title, String question, List<String> respuestas, String expirationDate, String audienceMode, String audienceBlocks, String audienceTowers, String votePrivacy, String actor, String initialStatus);
    void validateVoteAccess(String voterCode);
    void submitVote(String voterCode, String selectedOption);
    Survey updateSurveyStatus(Long surveyId, String status, String actor);
    Survey editSurvey(Long surveyId, String title, String question, List<String> respuestas, String expirationDate, String audienceMode, String audienceBlocks, String audienceTowers, String votePrivacy, String actor);
    List<SurveyVoteDetailDTO> getSurveyVotes(Long surveyId);
    String exportSurveyResults(Long surveyId);
    byte[] exportSurveyResultsPDF(Long surveyId);
    List<AuditLogDTO> getSurveyAuditLog(Long surveyId);
    List<ParticipationSummaryDTO> getParticipationByBlock();
    List<ParticipationSummaryDTO> getParticipationByTower();
    List<ParticipationSummaryDTO> getParticipationByUnit();
    List<SurveyHistoryItemDTO> searchSurveys(String searchTerm);
    List<SurveyHistoryItemDTO> findSurveysByStatus(String status);
}