package appsembly.appsembly.dto;

import java.time.LocalDateTime;

public record SurveyVoteDetailDTO(
    String voterCode,
    Integer voterPersonalCode,
    String voterFirstName,
    String voterLastName,
    String voterBlockName,
    String voterTowerName,
    String voterUnitNumber,
    String selectedOption,
    LocalDateTime createdAt
) {}