package appsembly.appsembly.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DashboardStatsDTO {
    private String label;
    private String value;
    private String detail;
}
