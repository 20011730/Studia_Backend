package Study.Assistant.Studia.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class QuizAttemptRequest {
    @NotBlank(message = "답변을 입력해주세요")
    private String userAnswer;
    
    private Long timeSpent; // 문제 풀이 시간 (초)
}
