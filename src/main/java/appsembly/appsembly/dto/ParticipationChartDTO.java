package appsembly.appsembly.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ParticipationChartDTO {
    private List<String> labels;
    private List<Integer> values;
}
