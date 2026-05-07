package appsembly.appsembly.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SurveyOptionDTO {
    private String label;
    private int percentage;
    private int votes;
}
