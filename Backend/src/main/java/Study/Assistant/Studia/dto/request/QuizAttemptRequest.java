package Study.Assistant.Studia.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizAttemptRequest {
    @NotNull(message = "답변 목록이 필요합니다")
    private List<Answer> answers;
    
    private Long totalTimeSpent; // 전체 풀이 시간 (초)
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Answer {
        private Long questionId;
        private int selectedOption; // -1 if not answered
    }
}
