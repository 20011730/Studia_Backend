package Study.Assistant.Studia.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizResponse {
    private Long id;
    private String title;
    private MaterialSummaryResponse material;
    private int questionCount; // questions 필드 대신 개수만
    private int attempts;
}
