package appsembly.appsembly;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import appsembly.appsembly.Repository.SurveyRepository;
import appsembly.appsembly.Repository.SurveyVoteRepository;
import appsembly.appsembly.Repository.UserAppRepository;
import appsembly.appsembly.domain.Roles;
import appsembly.appsembly.domain.Survey;
import appsembly.appsembly.domain.SurveyStatus;
import appsembly.appsembly.domain.UserApp;
import appsembly.appsembly.service.SurveyService;
import appsembly.appsembly.service.UserService;

@SpringBootTest
@ActiveProfiles("test")
public class SurveyFlowIntegrationTests {

    @Autowired
    private UserService userService;

    @Autowired
    private SurveyService surveyService;

    @Autowired
    private UserAppRepository userAppRepository;

    @Autowired
    private SurveyRepository surveyRepository;

    @Autowired
    private SurveyVoteRepository surveyVoteRepository;

    @Test
    public void testCompleteResidentAndSurveyFlow() throws Exception {
        // 1. ALTA DE RESIDENTE
        userService.register("Juan", "Perez", "juan@test.com", "password123", "password123", "USER", 1001, true);
        UserApp resident = userAppRepository.findByEmail("juan@test.com");
        assertNotNull(resident, "El residente debe ser registrado");
        assertEquals(Roles.USER, resident.getRole());

        // 2. ASIGNACIÓN DE VIVIENDA Y VALIDACIÓN DE DUPLICADOS
        UserApp updated = userService.updateResidentHousing(resident.getId(), "Bloque A", "Torre 1", "101");
        assertEquals("Bloque A", updated.getBlockName());
        assertEquals("Torre 1", updated.getTowerName());
        assertEquals("101", updated.getUnitNumber());

        // Intentar asignar la misma vivienda a otro residente (debe fallar)
        userService.register("Maria", "Gonzalez", "maria@test.com", "password123", "password123", "USER", 1002, true);
        UserApp resident2 = userAppRepository.findByEmail("maria@test.com");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateResidentHousing(resident2.getId(), "Bloque A", "Torre 1", "101");
        });
        assertTrue(exception.getMessage().contains("Ya existe un residente"));

        // 3. CREAR ENCUESTA POR BLOQUE (AUDIENCE MODE)
        surveyService.saveSurvey("Encuesta de Bloque A", "¿Qué mejora deseas?",
                List.of("Reparación de fachada", "Renovación de áreas comunes", "Otro"),
                "2026-05-31",
                "BLOCKS", "Bloque A", null, "ANONYMOUS", "admin", "DRAFT");

        Survey survey = surveyRepository.findAll().stream().filter(s -> "Encuesta de Bloque A".equals(s.getTitle())).findFirst().orElse(null);
        assertNotNull(survey, "La encuesta debe ser creada");
        assertEquals(SurveyStatus.DRAFT, survey.getStatus());

        // Transicionar a OPEN
        Survey openSurvey = surveyService.updateSurveyStatus(survey.getId(), "OPEN", "admin");
        assertEquals(SurveyStatus.OPEN, openSurvey.getStatus());

        // 4. VOTO ANÓNIMO
        surveyService.validateVoteAccess("juan@test.com");
        surveyService.submitVote("juan@test.com", "Reparación de fachada");
        long voteCount = surveyVoteRepository.countBySurveyId(survey.getId());
        assertEquals(1, voteCount, "Debe haber 1 voto");

        // Intentar votar dos veces (debe fallar)
        IllegalStateException duplicateVoteError = assertThrows(IllegalStateException.class, () -> {
            surveyService.submitVote("juan@test.com", "Renovación de áreas comunes");
        });
        assertTrue(duplicateVoteError.getMessage().contains("registró un voto"));

        // 5. CIERRE DE ENCUESTA
        Survey closedSurvey = surveyService.updateSurveyStatus(survey.getId(), "CLOSED", "admin");
        assertEquals(SurveyStatus.CLOSED, closedSurvey.getStatus());

        // 6. ARCHIVO DE ENCUESTA
        Survey archivedSurvey = surveyService.updateSurveyStatus(survey.getId(), "ARCHIVED", "admin");
        assertEquals(SurveyStatus.ARCHIVED, archivedSurvey.getStatus());

        // 7. EXPORTACIÓN CSV
        String csvExport = surveyService.exportSurveyResults(survey.getId());
        assertTrue(csvExport.contains("survey_id"), "CSV debe contener encabezados");
        assertTrue(csvExport.contains("title"), "CSV debe contener datos de la encuesta");

        // 8. EXPORTACIÓN PDF
        byte[] pdfExport = surveyService.exportSurveyResultsPDF(survey.getId());
        assertTrue(pdfExport.length > 0, "PDF debe ser generado");

        // 9. AUDIT LOG
        var auditLog = surveyService.getSurveyAuditLog(survey.getId());
        assertTrue(auditLog.size() >= 4, "Debe haber al menos 4 acciones: CREATED, ACTIVATED, CLOSED, ARCHIVED");
    }

    @Test
    public void testEditSurveyInDraftState() throws Exception {
        // Crear encuesta en DRAFT
        surveyService.saveSurvey("Encuesta Original", "Pregunta original",
                List.of("Sí", "No"),
                "2026-05-31",
                "ALL", null, null, "ANONYMOUS", "admin", "DRAFT");

        Survey survey = surveyRepository.findAll().stream().filter(s -> "Encuesta Original".equals(s.getTitle())).findFirst().orElse(null);
        assertNotNull(survey);

        // Editar encuesta
        Survey editedSurvey = surveyService.editSurvey(survey.getId(), "Encuesta Editada", "Pregunta editada",
                List.of("Opción 1", "Opción 2", "Opción 3"),
                "2026-06-15", "ALL", null, null, "PUBLIC", "admin");

        assertEquals("Encuesta Editada", editedSurvey.getTitle());
        assertEquals("Pregunta editada", editedSurvey.getQuestion());
        assertEquals(3, editedSurvey.getOptions().size());

        // Intentar editar después de activar (debe fallar)
        surveyService.updateSurveyStatus(survey.getId(), "OPEN", "admin");
        IllegalStateException editError = assertThrows(IllegalStateException.class, () -> {
            surveyService.editSurvey(survey.getId(), "Nuevo Título", "Nueva Pregunta",
                    List.of("A", "B"), null, "ALL", null, null, "ANONYMOUS", "admin");
        });
        assertTrue(editError.getMessage().contains("DRAFT"));
    }

    @Test
    public void testStatusTransitionRules() throws Exception {
        // Crear encuesta en DRAFT
        surveyService.saveSurvey("Test Transiciones", "¿Pregunta?",
                List.of("Opción 1", "Opción 2"),
                "2026-05-31", "ALL", null, null, "ANONYMOUS", "admin", "DRAFT");

        Survey survey = surveyRepository.findAll().stream().filter(s -> "Test Transiciones".equals(s.getTitle())).findFirst().orElse(null);

        // Transición válida: DRAFT -> OPEN
        Survey openSurvey = surveyService.updateSurveyStatus(survey.getId(), "OPEN", "admin");
        assertEquals(SurveyStatus.OPEN, openSurvey.getStatus());

        // Transición válida: OPEN -> CLOSED
        Survey closedSurvey = surveyService.updateSurveyStatus(survey.getId(), "CLOSED", "admin");
        assertEquals(SurveyStatus.CLOSED, closedSurvey.getStatus());

        // Transición válida: CLOSED -> ARCHIVED
        Survey archivedSurvey = surveyService.updateSurveyStatus(survey.getId(), "ARCHIVED", "admin");
        assertEquals(SurveyStatus.ARCHIVED, archivedSurvey.getStatus());

        // Transición inválida: ARCHIVED -> CLOSED (debe fallar)
        IllegalStateException invalidTransition = assertThrows(IllegalStateException.class, () -> {
            surveyService.updateSurveyStatus(survey.getId(), "CLOSED", "admin");
        });
        assertTrue(invalidTransition.getMessage().contains("Transición no permitida"));
    }

    @Test
    public void testHousingValidation() throws Exception {
        userService.register("Carlos", "Lopez", "carlos@test.com", "password123", "password123", "USER", 1003, true);
        UserApp resident = userAppRepository.findByEmail("carlos@test.com");

        // Validación: proporcionar solo bloque sin torre y unidad (debe fallar)
        IllegalArgumentException partialError = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateResidentHousing(resident.getId(), "Bloque B", null, null);
        });
        assertTrue(partialError.getMessage().contains("juntos"));

        // Validación: proporcionar todos los campos (válido)
        UserApp validHousing = userService.updateResidentHousing(resident.getId(), "Bloque B", "Torre 2", "201");
        assertNotNull(validHousing.getBlockName());
        assertEquals("Bloque B", validHousing.getBlockName());

        // Validación: limpiar vivienda (proporcionar nulos)
        UserApp cleanedHousing = userService.updateResidentHousing(resident.getId(), null, null, null);
        assertNull(cleanedHousing.getBlockName());
    }
}
