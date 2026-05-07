package appsembly.appsembly.dto;

public record ParticipationSummaryDTO(
    String dimension,
    String label,
    long residents,
    long voters,
    int percentage
) {}