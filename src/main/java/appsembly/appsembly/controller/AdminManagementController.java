package appsembly.appsembly.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import appsembly.appsembly.domain.Survey;
import appsembly.appsembly.domain.UserApp;
import appsembly.appsembly.dto.AuditLogDTO;
import appsembly.appsembly.dto.ParticipationSummaryDTO;
import appsembly.appsembly.dto.SurveyHistoryItemDTO;
import appsembly.appsembly.dto.SurveyVoteDetailDTO;
import appsembly.appsembly.service.DataSeedService;
import appsembly.appsembly.service.SurveyService;
import appsembly.appsembly.service.UserService;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminManagementController {
    private final UserService userService;
    private final SurveyService surveyService;
    private final DataSeedService dataSeedService;

    public AdminManagementController(UserService userService, SurveyService surveyService, DataSeedService dataSeedService) {
        this.userService = userService;
        this.surveyService = surveyService;
        this.dataSeedService = dataSeedService;
    }

    @GetMapping("/padron")
    public List<UserApp> getResidentPadron() {
        return userService.getResidentPadron();
    }

    @GetMapping("/padron/search")
    public List<UserApp> searchResidents(@RequestParam(required = true) String q) {
        return userService.searchResidents(q);
    }

    @GetMapping("/padron/block/{blockName}")
    public List<UserApp> getResidentsByBlock(@PathVariable String blockName) {
        return userService.findResidentsByBlock(blockName);
    }

    @GetMapping("/padron/tower/{towerName}")
    public List<UserApp> getResidentsByTower(@PathVariable String towerName) {
        return userService.findResidentsByTower(towerName);
    }

    @PutMapping("/padron/{userId}/housing")
    public Map<String, Object> updateHousing(@PathVariable Long userId,
            @RequestParam(required = false) String blockName,
            @RequestParam(required = false) String towerName,
            @RequestParam(required = false) String unitNumber) {
        UserApp userApp = userService.updateResidentHousing(userId, blockName, towerName, unitNumber);
        return Map.of(
                "success", true,
                "message", "Vivienda actualizada correctamente.",
                "user", userApp);
    }

    @PostMapping("/padron/{userId}/block")
    public Map<String, Object> blockResident(@PathVariable Long userId,
            @RequestParam(defaultValue = "true") boolean blocked) {
        UserApp userApp = userService.blockResident(userId, blocked);
        return Map.of(
                "success", true,
                "message", blocked ? "Residente bloqueado." : "Residente desbloqueado.",
                "user", userApp);
    }

    @PostMapping("/surveys/{surveyId}/status")
    public Map<String, Object> updateSurveyStatus(@PathVariable Long surveyId,
            @RequestParam String status,
            @RequestParam(required = false) String actor) {
        Survey survey = surveyService.updateSurveyStatus(surveyId, status, actor);
        return Map.of(
                "success", true,
                "message", "Estado de encuesta actualizado.",
                "surveyId", survey.getId(),
                "status", survey.getStatus().name());
    }

    @PutMapping("/surveys/{surveyId}")
    public Map<String, Object> editSurvey(@PathVariable Long surveyId,
            @RequestParam String title,
            @RequestParam String question,
            @RequestParam(required = false) List<String> respuestas,
            @RequestParam(required = false) String expirationDate,
            @RequestParam(required = false) String audienceMode,
            @RequestParam(required = false) String audienceBlocks,
            @RequestParam(required = false) String audienceTowers,
            @RequestParam(required = false) String votePrivacy,
            @RequestParam(required = false) String actor) {
        Survey survey = surveyService.editSurvey(surveyId, title, question, respuestas, expirationDate,
                audienceMode, audienceBlocks, audienceTowers, votePrivacy, actor);
        return Map.of(
                "success", true,
                "message", "Encuesta actualizada correctamente.",
                "surveyId", survey.getId(),
                "surveyTitle", survey.getTitle(),
                "surveyStatus", survey.getStatus().name());
    }

    @GetMapping("/surveys/{surveyId}/votes")
    public List<SurveyVoteDetailDTO> getSurveyVotes(@PathVariable Long surveyId) {
        return surveyService.getSurveyVotes(surveyId);
    }

    @GetMapping(value = "/surveys/{surveyId}/export", produces = "text/csv")
    public ResponseEntity<String> exportSurveyResults(@PathVariable Long surveyId) {
        String csv = surveyService.exportSurveyResults(surveyId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=survey-" + surveyId + "-results.csv")
                .contentType(new MediaType("text", "csv"))
                .body(csv);
    }

    @GetMapping(value = "/surveys/{surveyId}/export-pdf", produces = "application/pdf")
    public ResponseEntity<byte[]> exportSurveyResultsPDF(@PathVariable Long surveyId) {
        byte[] pdf = surveyService.exportSurveyResultsPDF(surveyId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=survey-" + surveyId + "-results.pdf")
                .contentType(new MediaType("application", "pdf"))
                .body(pdf);
    }

    @GetMapping("/surveys/{surveyId}/audit-log")
    public List<AuditLogDTO> getSurveyAuditLog(@PathVariable Long surveyId) {
        return surveyService.getSurveyAuditLog(surveyId);
    }

    @GetMapping("/participation")
    public List<ParticipationSummaryDTO> getParticipation(@RequestParam(defaultValue = "block") String dimension) {
        return switch (dimension.toLowerCase()) {
            case "tower" -> surveyService.getParticipationByTower();
            case "unit" -> surveyService.getParticipationByUnit();
            default -> surveyService.getParticipationByBlock();
        };
    }

    @GetMapping("/surveys/search")
    public List<SurveyHistoryItemDTO> searchSurveys(@RequestParam(required = true) String q) {
        return surveyService.searchSurveys(q);
    }

    @GetMapping("/surveys/status/{status}")
    public List<SurveyHistoryItemDTO> getSurveysByStatus(@PathVariable String status) {
        return surveyService.findSurveysByStatus(status);
    }

    @PostMapping("/data/seed")
    public Map<String, Object> seedData(@RequestParam(defaultValue = "admin") String actor) {
        DataSeedService.SeedSummary summary = dataSeedService.seedResidentData(actor);
        return Map.of(
                "success", true,
                "message", "Datos base cargados correctamente.",
                "actor", summary.actor(),
                "created", summary.created(),
                "existing", summary.existing(),
                "total", summary.total());
    }
}