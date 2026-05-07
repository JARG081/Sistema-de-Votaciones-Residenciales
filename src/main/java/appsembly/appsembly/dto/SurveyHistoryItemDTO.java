package appsembly.appsembly.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SurveyHistoryItemDTO {
    private Long surveyId;
    private String title;
    private String date;
    private String status;
    private String votes;
}
