package Study.Assistant.Studia.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizResponse {
    private Long id;
    private String title;
    private MaterialSummaryResponse material;
    private List<Study.Assistant.Studia.domain.entity.Quiz> questions;
    private int attempts;
    
    // 개별 퀴즈 문제용 (기존 필드 유지)
    private String question;
    private String questionType;
    private String difficulty;
    private List<String> options;
    private String correctAnswer;
    private String explanation;
    private String category;
    private String hint;
}
