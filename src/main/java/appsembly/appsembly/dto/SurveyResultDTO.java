package appsembly.appsembly.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SurveyResultDTO {
    private Long surveyId;
    private String title;
    private String question;
    private List<SurveyOptionDTO> options;
}
