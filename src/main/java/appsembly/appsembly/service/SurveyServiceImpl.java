package appsembly.appsembly.service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import appsembly.appsembly.Repository.SurveyRepository;
import appsembly.appsembly.Repository.SurveyVoteRepository;
import appsembly.appsembly.Repository.UserAppRepository;
import appsembly.appsembly.domain.Survey;
import appsembly.appsembly.domain.SurveyAuditAction;
import appsembly.appsembly.domain.SurveyAuditLog;
import appsembly.appsembly.domain.SurveyAudienceMode;
import appsembly.appsembly.domain.SurveyOption;
import appsembly.appsembly.domain.SurveyStatus;
import appsembly.appsembly.domain.SurveyVote;
import appsembly.appsembly.domain.SurveyVotePrivacy;
import appsembly.appsembly.domain.UserApp;
import appsembly.appsembly.dto.AuditLogDTO;
import appsembly.appsembly.dto.DashboardStatsDTO;
import appsembly.appsembly.dto.ParticipationChartDTO;
import appsembly.appsembly.dto.ParticipationSummaryDTO;
import appsembly.appsembly.dto.SurveyHistoryItemDTO;
import appsembly.appsembly.dto.SurveyOptionDTO;
import appsembly.appsembly.dto.SurveyResultDTO;
import appsembly.appsembly.dto.SurveyVoteDetailDTO;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SurveyServiceImpl implements SurveyService {
    private final SurveyRepository surveyRepository;
    private final SurveyVoteRepository surveyVoteRepository;
    private final UserAppRepository userAppRepository;
    private final ApplicationMetrics applicationMetrics;

    public SurveyServiceImpl(SurveyRepository surveyRepository, SurveyVoteRepository surveyVoteRepository,
            UserAppRepository userAppRepository, ApplicationMetrics applicationMetrics) {
        this.surveyRepository = surveyRepository;
        this.surveyVoteRepository = surveyVoteRepository;
        this.userAppRepository = userAppRepository;
        this.applicationMetrics = applicationMetrics;
    }

    @Override
    @Transactional(readOnly = true)
    public SurveyResultDTO getCurrentSurvey() {
        return getLatestOpenSurvey()
                .map(this::toSurveyResultWithVotes)
                .orElseGet(this::emptySurveyResult);
    }

    @Override
    @Transactional(readOnly = true)
    public SurveyResultDTO getSurveyQuestion() {
        return getCurrentSurvey();
    }

    @Override
    @Transactional(readOnly = true)
    public ParticipationChartDTO getParticipationChart() {
        Optional<Survey> currentSurvey = getLatestOpenSurvey();
        if (currentSurvey.isEmpty()) {
            return new ParticipationChartDTO(List.of("Participaron", "Pendientes"), List.of(0, 0));
        }

        long participated = surveyVoteRepository.countBySurveyId(currentSurvey.get().getId());
        long eligibleResidents = countEligibleResidents(currentSurvey.get());
        long pending = Math.max(eligibleResidents - participated, 0);
        return new ParticipationChartDTO(List.of("Participaron", "Pendientes"), List.of((int) participated, (int) pending));
    }

    @Override
    @Transactional(readOnly = true)
    public ParticipationChartDTO getResultsDataset() {
        Survey survey = getLatestOpenSurvey().orElse(null);
        if (survey == null) {
            return new ParticipationChartDTO(List.of(), List.of());
        }

        List<String> labels = survey.getOptions().stream().map(SurveyOption::getLabel).collect(Collectors.toList());
        List<Integer> values = survey.getOptions().stream().map(SurveyOption::getVotes).collect(Collectors.toList());
        return new ParticipationChartDTO(labels, values);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DashboardStatsDTO> getDashboardStats() {
        long totalSurveys = surveyRepository.findAll().size();
        long openSurveys = surveyRepository.findAll().stream().filter(survey -> survey.getStatus() == SurveyStatus.OPEN).count();
        long activeUsers = userAppRepository.count();
        long currentVotes = getLatestOpenSurvey().map(survey -> surveyVoteRepository.countBySurveyId(survey.getId())).orElse(0L);
        double responseRate = activeUsers > 0 ? (currentVotes * 100.0) / activeUsers : 0.0;

        return List.of(
                new DashboardStatsDTO("Asistencia media", formatPercentage(responseRate), "Promedio sobre usuarios registrados"),
                new DashboardStatsDTO("Residentes activos", String.valueOf(activeUsers), "Cuentas habilitadas"),
                new DashboardStatsDTO("Encuestas abiertas", String.valueOf(openSurveys), "Vigentes en la base"),
                new DashboardStatsDTO("Encuestas totales", String.valueOf(totalSurveys), "Incluye borradores, cerradas y archivadas"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SurveyHistoryItemDTO> getSurveyHistory() {
        return surveyRepository.findAll().stream()
                .sorted(Comparator.comparing(Survey::getCreatedAt).reversed())
                .map(this::toHistoryItem)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void saveSurvey(String title, String question, List<String> respuestas, String expirationDate,
            String audienceMode, String audienceBlocks, String audienceTowers, String votePrivacy, String actor,
            String initialStatus) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("El título de la encuesta es obligatorio.");
        }
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("La pregunta de la encuesta es obligatoria.");
        }

        List<String> normalizedAnswers = Optional.ofNullable(respuestas).orElse(List.of()).stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(answer -> !answer.isEmpty())
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));

        if (normalizedAnswers.size() < 2) {
            throw new IllegalArgumentException("Debes proporcionar al menos dos respuestas distintas.");
        }

        LocalDate expiration = parseExpirationDate(expirationDate);
        SurveyStatus status = parseSurveyStatus(initialStatus).orElse(SurveyStatus.OPEN);
        SurveyAudienceMode mode = parseSurveyAudienceMode(audienceMode).orElse(SurveyAudienceMode.ALL);
        SurveyVotePrivacy privacy = parseSurveyVotePrivacy(votePrivacy).orElse(SurveyVotePrivacy.ANONYMOUS);
        String actorName = normalizeActor(actor);
        log.info("Creating survey '{}' with status={} actor={}", title.trim(), status, actorName);

        if (status == SurveyStatus.OPEN) {
            surveyRepository.findTopByStatusOrderByCreatedAtDesc(SurveyStatus.OPEN).ifPresent(existing -> {
                existing.setStatus(SurveyStatus.CLOSED);
                existing.setClosedBy(actorName);
                existing.setUpdatedBy(actorName);
                existing.setUpdatedAt(LocalDateTime.now());
                existing.addAuditLog(createAudit(existing, SurveyAuditAction.CLOSED, actorName,
                        "Se cerró al crear una nueva encuesta activa."));
                surveyRepository.save(existing);
            });
        }

        Survey survey = Survey.builder()
                .title(title.trim())
                .question(question.trim())
                .expirationDate(expiration)
                .status(status)
                .audienceMode(mode)
                .audienceBlocks(normalize(audienceBlocks))
                .audienceTowers(normalize(audienceTowers))
                .votePrivacy(privacy)
                .createdBy(actorName)
                .updatedBy(actorName)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        normalizedAnswers.forEach(answer -> survey.addOption(SurveyOption.builder().label(answer).votes(0).build()));
        survey.addAuditLog(createAudit(survey, SurveyAuditAction.CREATED, actorName, "Encuesta creada con estado " + status.name()));
        surveyRepository.save(survey);
    }

    @Override
    @Transactional(readOnly = true)
    public void validateVoteAccess(String voterCode) {
        Survey survey = getLatestOpenSurvey()
                .orElseThrow(() -> new IllegalStateException("No hay una encuesta activa para votar."));

        UserApp resident = resolveResident(voterCode);
        assertResidentCanVote(resident, survey);
    }

    @Override
    @Transactional
    public void submitVote(String voterCode, String selectedOption) {
        if (voterCode == null || voterCode.isBlank()) {
            throw new IllegalArgumentException("Debes ingresar un código válido antes de votar.");
        }
        if (selectedOption == null || selectedOption.isBlank()) {
            throw new IllegalArgumentException("Debes seleccionar una opción antes de confirmar el voto.");
        }

        Survey survey = getLatestOpenSurvey()
                .orElseThrow(() -> new IllegalStateException("No hay una encuesta activa para votar."));

        UserApp resident = resolveResident(voterCode);
        assertResidentCanVote(resident, survey);

        String normalizedCode = normalize(voterCode);
        String normalizedOption = selectedOption.trim();
        log.info("Submitting vote for survey {} and voter {}", getLatestOpenSurvey().map(Survey::getId).orElse(null), normalizedCode);

        if (surveyVoteRepository.existsBySurveyIdAndVoterCode(survey.getId(), normalizedCode)) {
            throw new IllegalStateException("Este código ya registró un voto en la encuesta actual.");
        }

        SurveyOption option = survey.getOptions().stream()
                .filter(entry -> entry.getLabel().equalsIgnoreCase(normalizedOption))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("La opción seleccionada no existe en la encuesta activa."));

        option.setVotes(option.getVotes() + 1);
        survey.setUpdatedAt(LocalDateTime.now());
        survey.setUpdatedBy(resident.getEmail());

        surveyVoteRepository.save(SurveyVote.builder()
                .survey(survey)
                .voterCode(normalizedCode)
                .selectedOption(option.getLabel())
                .voterPersonalCode(resident.getPersonalCode())
                .voterFirstName(resident.getFirstName())
                .voterLastName(resident.getLastName())
                .voterBlockName(resident.getBlockName())
                .voterTowerName(resident.getTowerName())
                .voterUnitNumber(resident.getUnitNumber())
                .voterEmail(resident.getEmail())
                .createdAt(LocalDateTime.now())
                .build());
        surveyRepository.save(survey);
            applicationMetrics.recordVote();
    }

    @Override
    @Transactional
    public Survey updateSurveyStatus(Long surveyId, String status, String actor) {
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la encuesta indicada."));

        SurveyStatus nextStatus = parseSurveyStatus(status)
                .orElseThrow(() -> new IllegalArgumentException("Debes indicar un estado válido para la encuesta."));
        String actorName = normalizeActor(actor);
        log.info("Updating survey {} from {} to {} by {}", surveyId, survey.getStatus(), nextStatus, actorName);

        // Validar transiciones permitidas
        validateStatusTransition(survey.getStatus(), nextStatus);

        if (nextStatus == SurveyStatus.OPEN) {
            // Cerrar automáticamente la encuesta OPEN anterior
            surveyRepository.findTopByStatusOrderByCreatedAtDesc(SurveyStatus.OPEN)
                    .filter(existing -> !existing.getId().equals(survey.getId()))
                    .ifPresent(existing -> {
                        existing.setStatus(SurveyStatus.CLOSED);
                        existing.setClosedBy(actorName);
                        existing.setUpdatedBy(actorName);
                        existing.setUpdatedAt(LocalDateTime.now());
                        existing.addAuditLog(createAudit(existing, SurveyAuditAction.CLOSED, actorName,
                                "Se cerró automáticamente para abrir una nueva encuesta activa."));
                        surveyRepository.save(existing);
                    });
            survey.setActivatedBy(actorName);
            survey.addAuditLog(createAudit(survey, SurveyAuditAction.ACTIVATED, actorName, "Encuesta activada manualmente."));
        } else if (nextStatus == SurveyStatus.CLOSED) {
            survey.setClosedBy(actorName);
            survey.addAuditLog(createAudit(survey, SurveyAuditAction.CLOSED, actorName, "Encuesta cerrada manualmente."));
        } else if (nextStatus == SurveyStatus.ARCHIVED) {
            survey.setArchivedBy(actorName);
            survey.addAuditLog(createAudit(survey, SurveyAuditAction.ARCHIVED, actorName, "Encuesta archivada manualmente."));
        }

        survey.setStatus(nextStatus);
        survey.setUpdatedBy(actorName);
        survey.setUpdatedAt(LocalDateTime.now());
        return surveyRepository.save(survey);
    }

    private void validateStatusTransition(SurveyStatus currentStatus, SurveyStatus nextStatus) {
        // Transiciones válidas:
        // DRAFT -> OPEN, DRAFT (sin cambio)
        // OPEN -> CLOSED
        // CLOSED -> ARCHIVED
        // ARCHIVED -> ARCHIVED (sin cambio)

        if (currentStatus == nextStatus) {
            return; // Sin cambio, permitido
        }

        boolean validTransition = switch (currentStatus) {
            case DRAFT -> nextStatus == SurveyStatus.OPEN;
            case OPEN -> nextStatus == SurveyStatus.CLOSED;
            case CLOSED -> nextStatus == SurveyStatus.ARCHIVED;
            case ARCHIVED -> false; // No permitir cambios desde ARCHIVED
        };

        if (!validTransition) {
            throw new IllegalStateException("Transición no permitida: " + currentStatus.name() + 
                    " → " + nextStatus.name());
        }
    }

    @Override
    @Transactional
    public Survey editSurvey(Long surveyId, String title, String question, List<String> respuestas,
            String expirationDate, String audienceMode, String audienceBlocks, String audienceTowers,
            String votePrivacy, String actor) {
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la encuesta indicada."));

        // Solo se pueden editar encuestas en estado DRAFT
        if (survey.getStatus() != SurveyStatus.DRAFT) {
            throw new IllegalStateException("Solo se pueden editar encuestas en estado DRAFT. " +
                    "Estado actual: " + survey.getStatus().name());
        }
        log.info("Editing survey {} by {}", surveyId, normalizeActor(actor));

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("El título de la encuesta es obligatorio.");
        }
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("La pregunta de la encuesta es obligatoria.");
        }

        List<String> normalizedAnswers = Optional.ofNullable(respuestas).orElse(List.of()).stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(answer -> !answer.isEmpty())
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));

        if (normalizedAnswers.size() < 2) {
            throw new IllegalArgumentException("Debes proporcionar al menos dos respuestas distintas.");
        }

        String actorName = normalizeActor(actor);
        LocalDate expiration = parseExpirationDate(expirationDate);
        SurveyAudienceMode mode = parseSurveyAudienceMode(audienceMode).orElse(SurveyAudienceMode.ALL);
        SurveyVotePrivacy privacy = parseSurveyVotePrivacy(votePrivacy).orElse(SurveyVotePrivacy.ANONYMOUS);

        // Actualizar campos editables
        survey.setTitle(title.trim());
        survey.setQuestion(question.trim());
        survey.setExpirationDate(expiration);
        survey.setAudienceMode(mode);
        survey.setAudienceBlocks(normalize(audienceBlocks));
        survey.setAudienceTowers(normalize(audienceTowers));
        survey.setVotePrivacy(privacy);
        survey.setUpdatedBy(actorName);
        survey.setUpdatedAt(LocalDateTime.now());

        // Actualizar opciones de respuesta
        survey.getOptions().clear();
        normalizedAnswers.forEach(answer -> survey.addOption(SurveyOption.builder().label(answer).votes(0).build()));

        // Registrar la edición en auditoría
        survey.addAuditLog(createAudit(survey, SurveyAuditAction.EDITED, actorName,
                "Encuesta editada en estado DRAFT: título, pregunta, respuestas y parámetros actualizados."));

        return surveyRepository.save(survey);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SurveyVoteDetailDTO> getSurveyVotes(Long surveyId) {
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la encuesta indicada."));

        boolean anonymous = survey.getVotePrivacy() == SurveyVotePrivacy.ANONYMOUS;
        return surveyVoteRepository.findAllBySurveyId(surveyId).stream()
                .map(vote -> anonymous
                        ? new SurveyVoteDetailDTO(mask(vote.getVoterCode()), null, null, null, null, null, null,
                                vote.getSelectedOption(), vote.getCreatedAt())
                        : new SurveyVoteDetailDTO(vote.getVoterCode(), vote.getVoterPersonalCode(),
                                vote.getVoterFirstName(), vote.getVoterLastName(), vote.getVoterBlockName(),
                                vote.getVoterTowerName(), vote.getVoterUnitNumber(), vote.getSelectedOption(),
                                vote.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public String exportSurveyResults(Long surveyId) {
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la encuesta indicada."));

        StringBuilder export = new StringBuilder();
        export.append("survey_id,title,question,status,vote_privacy,option,total_votes\n");
        for (SurveyOption option : survey.getOptions()) {
            export.append(csv(survey.getId().toString())).append(',')
                    .append(csv(survey.getTitle())).append(',')
                    .append(csv(survey.getQuestion())).append(',')
                    .append(csv(survey.getStatus().name())).append(',')
                    .append(csv(survey.getVotePrivacy() == null ? SurveyVotePrivacy.ANONYMOUS.name() : survey.getVotePrivacy().name())).append(',')
                    .append(csv(option.getLabel())).append(',')
                    .append(option.getVotes())
                    .append('\n');
        }

        export.append('\n').append("vote_code,personal_code,first_name,last_name,block_name,tower_name,unit_number,selected_option,created_at\n");
        boolean anonymous = survey.getVotePrivacy() == SurveyVotePrivacy.ANONYMOUS;
        surveyVoteRepository.findAllBySurveyId(surveyId).forEach(vote -> export.append(csv(anonymous ? mask(vote.getVoterCode()) : vote.getVoterCode())).append(',')
                .append(csv(anonymous ? "" : Objects.toString(vote.getVoterPersonalCode(), ""))).append(',')
                .append(csv(anonymous ? "" : vote.getVoterFirstName())).append(',')
                .append(csv(anonymous ? "" : vote.getVoterLastName())).append(',')
                .append(csv(anonymous ? "" : vote.getVoterBlockName())).append(',')
                .append(csv(anonymous ? "" : vote.getVoterTowerName())).append(',')
                .append(csv(anonymous ? "" : vote.getVoterUnitNumber())).append(',')
                .append(csv(vote.getSelectedOption())).append(',')
                .append(csv(vote.getCreatedAt().toString())).append('\n'));

        return export.toString();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportSurveyResultsPDF(Long surveyId) {
        try {
            Survey survey = surveyRepository.findById(surveyId)
                    .orElseThrow(() -> new IllegalArgumentException("No se encontró la encuesta indicada."));

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, output);
            document.open();

            // Encabezado
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("Reporte de Encuesta", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            // Información de la encuesta
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            document.add(new Paragraph("Encuesta: " + survey.getTitle(), headerFont));
            document.add(new Paragraph("Pregunta: " + survey.getQuestion()));
            document.add(new Paragraph("Estado: " + survey.getStatus().name()));
            document.add(new Paragraph("Privacidad de votos: " + (survey.getVotePrivacy() == null ? "ANÓNIMO" : survey.getVotePrivacy().name())));
            document.add(new Paragraph("Fecha de creación: " + survey.getCreatedAt()));
            if (survey.getExpirationDate() != null) {
                document.add(new Paragraph("Fecha de vencimiento: " + survey.getExpirationDate()));
            }
            document.add(new Paragraph(" "));

            // Tabla de resultados
            document.add(new Paragraph("Resultados", headerFont));
            PdfPTable resultsTable = new PdfPTable(2);
            resultsTable.setWidthPercentage(100);

            // Encabezados de tabla
            PdfPCell cellOption = new PdfPCell(new Phrase("Opción"));
            cellOption.setBackgroundColor(BaseColor.LIGHT_GRAY);
            resultsTable.addCell(cellOption);

            PdfPCell cellVotes = new PdfPCell(new Phrase("Votos"));
            cellVotes.setBackgroundColor(BaseColor.LIGHT_GRAY);
            resultsTable.addCell(cellVotes);

            // Datos de opciones
            long totalVotes = 0;
            for (SurveyOption option : survey.getOptions()) {
                resultsTable.addCell(option.getLabel());
                resultsTable.addCell(String.valueOf(option.getVotes()));
                totalVotes += option.getVotes();
            }

            document.add(resultsTable);
            document.add(new Paragraph(" "));

            // Resumen
            document.add(new Paragraph("Total de votos registrados: " + totalVotes));
            long eligibleResidents = countEligibleResidents(survey);
            document.add(new Paragraph("Residentes elegibles: " + eligibleResidents));
            double participationRate = eligibleResidents > 0 ? (totalVotes * 100.0) / eligibleResidents : 0.0;
            document.add(new Paragraph(String.format("Tasa de participación: %.2f%%", participationRate)));

            document.close();
            return output.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Error al generar PDF: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogDTO> getSurveyAuditLog(Long surveyId) {
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la encuesta indicada."));

        return survey.getAuditLogs().stream()
                .sorted(Comparator.comparing(SurveyAuditLog::getActionAt).reversed())
                .map(log -> AuditLogDTO.builder()
                        .id(log.getId())
                        .surveyId(survey.getId())
                        .action(log.getAction().name())
                        .actor(log.getActor())
                        .description(log.getNotes())
                        .timestamp(log.getActionAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationSummaryDTO> getParticipationByBlock() {
        return buildParticipationSummary(UserApp::getBlockName, "block");
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationSummaryDTO> getParticipationByTower() {
        return buildParticipationSummary(UserApp::getTowerName, "tower");
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationSummaryDTO> getParticipationByUnit() {
        return buildParticipationSummary(user -> {
            if (user.getBlockName() == null && user.getTowerName() == null && user.getUnitNumber() == null) {
                return null;
            }
            return String.join(" / ", List.of(
                    Objects.toString(user.getBlockName(), "Sin bloque"),
                    Objects.toString(user.getTowerName(), "Sin torre"),
                    Objects.toString(user.getUnitNumber(), "Sin unidad")));
        }, "unit");
    }

    @Override
    @Transactional(readOnly = true)
    public List<SurveyHistoryItemDTO> searchSurveys(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return surveyRepository.findAllOrderByCreatedDesc().stream()
                    .map(this::toHistoryItem)
                    .collect(Collectors.toList());
        }
        return surveyRepository.searchSurveys(searchTerm.trim()).stream()
                .map(this::toHistoryItem)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SurveyHistoryItemDTO> findSurveysByStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return getSurveyHistory();
        }
        try {
            SurveyStatus surveyStatus = SurveyStatus.valueOf(status.trim().toUpperCase());
            return surveyRepository.findByStatus(surveyStatus).stream()
                    .map(this::toHistoryItem)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Estado de encuesta inválido: " + status);
        }
    }

    private Optional<Survey> getLatestOpenSurvey() {
        return surveyRepository.findTopByStatusOrderByCreatedAtDesc(SurveyStatus.OPEN);
    }

    private SurveyResultDTO toSurveyResultWithVotes(Survey survey) {
        int totalVotes = survey.getOptions().stream().mapToInt(SurveyOption::getVotes).sum();
        List<SurveyOptionDTO> options = survey.getOptions().stream()
                .map(option -> new SurveyOptionDTO(option.getLabel(), calculatePercentage(option.getVotes(), totalVotes), option.getVotes()))
                .collect(Collectors.toList());
        return new SurveyResultDTO(survey.getId(), survey.getTitle(), survey.getQuestion(), options);
    }

    private int calculatePercentage(int votes, int totalVotes) {
        if (totalVotes <= 0) {
            return 0;
        }
        return (int) Math.round((votes * 100.0) / totalVotes);
    }

    private SurveyHistoryItemDTO toHistoryItem(Survey survey) {
        long participants = surveyVoteRepository.countBySurveyId(survey.getId());
        String status = switch (survey.getStatus()) {
            case DRAFT -> "Borrador";
            case OPEN -> survey.getExpirationDate().isBefore(LocalDate.now()) ? "Vencida" : "Activa";
            case CLOSED -> "Cerrada";
            case ARCHIVED -> "Archivada";
        };
        String votes = participants == 0 ? "Pendiente"
                : formatPercentage((participants * 100.0) / Math.max(userAppRepository.count(), 1));
        return new SurveyHistoryItemDTO(survey.getId(), survey.getTitle(), survey.getCreatedAt().toLocalDate().toString(), status, votes);
    }

    private SurveyResultDTO emptySurveyResult() {
        return new SurveyResultDTO(null, "Sin encuesta activa", "Aún no se ha creado una votación para mostrar.", List.of());
    }

    private LocalDate parseExpirationDate(String expirationDate) {
        if (expirationDate == null || expirationDate.isBlank()) {
            throw new IllegalArgumentException("La fecha de expiración es obligatoria.");
        }

        try {
            return LocalDate.parse(expirationDate.trim());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("La fecha de expiración no tiene un formato válido.");
        }
    }

    private Optional<SurveyStatus> parseSurveyStatus(String status) {
        if (status == null || status.isBlank()) {
            return Optional.empty();
        }

        try {
            return Optional.of(SurveyStatus.valueOf(status.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("El estado de la encuesta no es válido.");
        }
    }

    private Optional<SurveyAudienceMode> parseSurveyAudienceMode(String mode) {
        if (mode == null || mode.isBlank()) {
            return Optional.empty();
        }

        String normalized = mode.trim().toUpperCase(Locale.ROOT);
        // Accept common synonyms used in API/tests
        switch (normalized) {
            case "ALL":
                return Optional.of(SurveyAudienceMode.ALL);
            case "BLOCKS":
            case "BLOCK":
                return Optional.of(SurveyAudienceMode.BLOCK);
            case "TOWERS":
            case "TOWER":
                return Optional.of(SurveyAudienceMode.TOWER);
            case "BLOCKS_AND_TOWERS":
            case "BLOCK_AND_TOWER":
            case "BLOCK_AND_TOWERS":
            case "BLOCKS_AND_TOWER":
                return Optional.of(SurveyAudienceMode.BLOCK_AND_TOWER);
            default:
                try {
                    return Optional.of(SurveyAudienceMode.valueOf(normalized));
                } catch (IllegalArgumentException ex) {
                    throw new IllegalArgumentException("El alcance de audiencia no es válido.");
                }
        }
    }

    private Optional<SurveyVotePrivacy> parseSurveyVotePrivacy(String privacy) {
        if (privacy == null || privacy.isBlank()) {
            return Optional.empty();
        }

        try {
            return Optional.of(SurveyVotePrivacy.valueOf(privacy.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("La privacidad del voto no es válida.");
        }
    }

    private String normalizeActor(String actor) {
        return actor == null || actor.isBlank() ? "system" : actor.trim();
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private SurveyAuditLog createAudit(Survey survey, SurveyAuditAction action, String actor, String notes) {
        return SurveyAuditLog.builder()
                .survey(survey)
                .action(action)
                .actor(normalizeActor(actor))
                .actionAt(LocalDateTime.now())
                .notes(notes)
                .build();
    }

    private void assertResidentCanVote(UserApp resident, Survey survey) {
        if (Boolean.TRUE.equals(resident.getBlocked())) {
            throw new IllegalStateException("El residente está bloqueado y no puede votar.");
        }

        if (!isEligible(resident, survey)) {
            throw new IllegalStateException("Este residente no pertenece a la audiencia autorizada para la encuesta actual.");
        }
    }

    private UserApp resolveResident(String voterCode) {
        String normalizedCode = normalize(voterCode);
        if (normalizedCode == null) {
            throw new IllegalArgumentException("Debes ingresar un código válido antes de votar.");
        }

        Optional<UserApp> resident = Optional.empty();
        try {
            resident = userAppRepository.findByPersonalCode(Integer.valueOf(normalizedCode));
        } catch (NumberFormatException ignored) {
            // Fallback to email lookup below.
        }

        if (resident.isEmpty()) {
            resident = Optional.ofNullable(userAppRepository.findByEmail(normalizedCode));
        }

        return resident.orElseThrow(() -> new IllegalArgumentException("No se encontró un residente asociado al código proporcionado."));
    }

    private boolean isEligible(UserApp resident, Survey survey) {
        SurveyAudienceMode mode = survey.getAudienceMode() == null ? SurveyAudienceMode.ALL : survey.getAudienceMode();
        List<String> allowedBlocks = parseCsvList(survey.getAudienceBlocks());
        List<String> allowedTowers = parseCsvList(survey.getAudienceTowers());

        if (mode == SurveyAudienceMode.ALL) {
            return true;
        }

        boolean blockMatch = resident.getBlockName() != null && allowedBlocks.stream().anyMatch(value -> value.equalsIgnoreCase(resident.getBlockName()));
        boolean towerMatch = resident.getTowerName() != null && allowedTowers.stream().anyMatch(value -> value.equalsIgnoreCase(resident.getTowerName()));

        if (mode == SurveyAudienceMode.BLOCK) {
            return allowedBlocks.isEmpty() || blockMatch;
        }

        if (mode == SurveyAudienceMode.TOWER) {
            return allowedTowers.isEmpty() || towerMatch;
        }

        if (mode == SurveyAudienceMode.BLOCK_AND_TOWER) {
            return (allowedBlocks.isEmpty() && allowedTowers.isEmpty()) || blockMatch || towerMatch;
        }

        return true;
    }

    private List<String> parseCsvList(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }

        return List.of(csv.split(","))
                .stream()
                .map(String::trim)
                .filter(entry -> !entry.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    private long countEligibleResidents(Survey survey) {
        return userAppRepository.findAll().stream()
                .filter(user -> !Boolean.TRUE.equals(user.getBlocked()))
                .filter(user -> isEligible(user, survey))
                .count();
    }

    private List<ParticipationSummaryDTO> buildParticipationSummary(Function<UserApp, String> groupExtractor, String dimension) {
        Survey survey = getLatestOpenSurvey().orElseGet(() -> surveyRepository.findAll().stream()
                .sorted(Comparator.comparing(Survey::getCreatedAt).reversed())
                .findFirst()
                .orElse(null));

        if (survey == null) {
            return List.of();
        }

        List<UserApp> residents = userAppRepository.findAll().stream()
                .filter(user -> !Boolean.TRUE.equals(user.getBlocked()))
                .toList();
        List<SurveyVote> votes = surveyVoteRepository.findAllBySurveyId(survey.getId());

        return residents.stream()
                .map(groupExtractor)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .map(label -> {
                    long totalResidents = residents.stream().filter(user -> label.equals(groupExtractor.apply(user))).count();
                    long totalVotes = votes.stream().filter(vote -> label.equals(groupLabel(vote, dimension))).count();
                    int percentage = totalResidents > 0 ? (int) Math.round((totalVotes * 100.0) / totalResidents) : 0;
                    return new ParticipationSummaryDTO(dimension, label, totalResidents, totalVotes, percentage);
                })
                .collect(Collectors.toList());
    }

    private String groupLabel(SurveyVote vote, String dimension) {
        return switch (dimension) {
            case "block" -> vote.getVoterBlockName();
            case "tower" -> vote.getVoterTowerName();
            case "unit" -> String.join(" / ", List.of(
                    Objects.toString(vote.getVoterBlockName(), "Sin bloque"),
                    Objects.toString(vote.getVoterTowerName(), "Sin torre"),
                    Objects.toString(vote.getVoterUnitNumber(), "Sin unidad")));
            default -> null;
        };
    }

    private String mask(String value) {
        if (value == null || value.isBlank()) {
            return "anonimo";
        }

        if (value.length() <= 2) {
            return "**";
        }

        return value.charAt(0) + "***" + value.charAt(value.length() - 1);
    }

    private String csv(String value) {
        String safe = value == null ? "" : value.replace("\"", "\"\"");
        return '"' + safe + '"';
    }

    private String formatPercentage(double value) {
        return Math.round(value) + "%";
    }
}