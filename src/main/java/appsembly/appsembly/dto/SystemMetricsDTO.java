package appsembly.appsembly.dto;

public record SystemMetricsDTO(long voteCount, long badRequestCount, long conflictCount, long unexpectedErrorCount) {
}