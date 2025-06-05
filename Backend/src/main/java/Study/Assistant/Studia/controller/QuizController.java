package Study.Assistant.Studia.controller;

import Study.Assistant.Studia.dto.request.QuizAttemptRequest;
import Study.Assistant.Studia.dto.response.QuizAttemptResponse;
import Study.Assistant.Studia.dto.response.QuizReviewResponse;
import Study.Assistant.Studia.dto.response.WrongAnswerNoteResponse;
import Study.Assistant.Studia.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {
    
    private final QuizService quizService;
    
    /**
     * 퀴즈 목록 조회
     */
    @GetMapping
    public ResponseEntity<List<Study.Assistant.Studia.dto.response.QuizResponse>> getQuizzes() {
        List<Study.Assistant.Studia.dto.response.QuizResponse> quizzes = quizService.getUserQuizzes();
        return ResponseEntity.ok(quizzes);
    }
    
    /**
     * 특정 퀴즈 상세 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<Study.Assistant.Studia.dto.response.QuizDetailResponse> getQuiz(@PathVariable Long id) {
        Study.Assistant.Studia.dto.response.QuizDetailResponse quiz = quizService.getQuizDetail(id);
        return ResponseEntity.ok(quiz);
    }
    
    /**
     * 퀴즈 시도 제출
     */
    @PostMapping("/{quizId}/attempts")
    public ResponseEntity<QuizAttemptResponse> submitQuizAttempt(
            @PathVariable Long quizId,
            @RequestBody QuizAttemptRequest request) {
        
        QuizAttemptResponse response = quizService.submitAttempt(quizId, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 특정 학습 자료의 퀴즈 시도 기록 조회
     */
    @GetMapping("/materials/{materialId}/attempts")
    public ResponseEntity<List<QuizAttemptResponse>> getMaterialQuizAttempts(
            @PathVariable Long materialId,
            @RequestParam(value = "onlyWrong", defaultValue = "false") boolean onlyWrong) {
        
        List<QuizAttemptResponse> attempts = quizService.getMaterialAttempts(materialId, onlyWrong);
        return ResponseEntity.ok(attempts);
    }
    
    /**
     * 퀴즈 복습 데이터 조회 (정답률, 취약 문제 등)
     */
    @GetMapping("/materials/{materialId}/review")
    public ResponseEntity<QuizReviewResponse> getQuizReview(@PathVariable Long materialId) {
        QuizReviewResponse review = quizService.getQuizReview(materialId);
        return ResponseEntity.ok(review);
    }
    
    /**
     * 오답노트 조회
     */
    @GetMapping("/wrong-answers")
    public ResponseEntity<List<WrongAnswerNoteResponse>> getWrongAnswerNotes(
            @RequestParam(value = "courseId", required = false) Long courseId,
            @RequestParam(value = "limit", defaultValue = "20") int limit) {
        
        List<WrongAnswerNoteResponse> notes = quizService.getWrongAnswerNotes(courseId, limit);
        return ResponseEntity.ok(notes);
    }
    
    /**
     * 학습 통계 조회
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getQuizStatistics(
            @RequestParam(value = "period", defaultValue = "WEEK") String period) {
        
        return ResponseEntity.ok(quizService.getQuizStatistics(period));
    }
}
