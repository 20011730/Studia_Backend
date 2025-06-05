package Study.Assistant.Studia.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class QuizAttemptResponse {
    private Long id;
    private Long quizId;
    private String question;
    private String userAnswer;
    private String correctAnswer;
    private Boolean isCorrect;
    private Integer score;
    private String explanation;
    private LocalDateTime attemptedAt;
}
