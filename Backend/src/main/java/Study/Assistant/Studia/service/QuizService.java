package Study.Assistant.Studia.service;

import Study.Assistant.Studia.domain.entity.*;
import Study.Assistant.Studia.dto.request.QuizAttemptRequest;
import Study.Assistant.Studia.dto.response.*;
import Study.Assistant.Studia.repository.QuizAttemptRepository;
import Study.Assistant.Studia.repository.QuizRepository;
import Study.Assistant.Studia.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class QuizService {
    
    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final UserRepository userRepository;
    
    /**
     * 사용자의 퀴즈 목록 조회
     */
    public List<Study.Assistant.Studia.dto.response.QuizResponse> getUserQuizzes() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Quiz> quizzes = quizRepository.findByStudyMaterial_UserId(user.getId());
        
        // 퀴즈별로 그룹화하여 StudyMaterial별 퀴즈 그룹 생성
        Map<Long, List<Quiz>> quizzesByMaterial = quizzes.stream()
                .collect(Collectors.groupingBy(q -> q.getStudyMaterial().getId()));
        
        return quizzesByMaterial.entrySet().stream()
                .map(entry -> {
                    StudyMaterial material = quizzes.stream()
                            .filter(q -> q.getStudyMaterial().getId().equals(entry.getKey()))
                            .findFirst()
                            .map(Quiz::getStudyMaterial)
                            .orElse(null);
                    
                    if (material == null) return null;
                    
                    return Study.Assistant.Studia.dto.response.QuizResponse.builder()
                            .id(entry.getKey()) // Material ID as quiz group ID
                            .title(material.getTitle() + " Quiz")
                            .material(Study.Assistant.Studia.dto.response.MaterialSummaryResponse.builder()
                                    .id(material.getId())
                                    .title(material.getTitle())
                                    .originalFileName(material.getOriginalFileName())
                                    .build())
                            .questions(entry.getValue())
                            .attempts(quizAttemptRepository.countByUserIdAndQuiz_StudyMaterial_Id(
                                    user.getId(), material.getId()))
                            .build();
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    /**
     * 특정 퀴즈 상세 조회
     */
    public Study.Assistant.Studia.dto.response.QuizDetailResponse getQuizDetail(Long materialId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Quiz> quizzes = quizRepository.findByStudyMaterial_IdAndStudyMaterial_UserId(materialId, user.getId());
        
        if (quizzes.isEmpty()) {
            throw new RuntimeException("Quiz not found");
        }
        
        StudyMaterial material = quizzes.get(0).getStudyMaterial();
        
        List<Study.Assistant.Studia.dto.response.QuizDetailResponse.QuestionDetail> questions = quizzes.stream()
                .map(quiz -> Study.Assistant.Studia.dto.response.QuizDetailResponse.QuestionDetail.builder()
                        .id(quiz.getId())
                        .questionText(quiz.getQuestion())
                        .questionType(quiz.getQuestionType().toString())
                        .difficulty(quiz.getDifficulty().toString())
                        .options(quiz.getOptions())
                        .correctOption(quiz.getOptions().indexOf(quiz.getCorrectAnswer()))
                        .explanation(quiz.getExplanation())
                        .hint(quiz.getHint())
                        .category(quiz.getCategory())
                        .build())
                .collect(Collectors.toList());
        
        return Study.Assistant.Studia.dto.response.QuizDetailResponse.builder()
                .id(materialId)
                .title(material.getTitle() + " Quiz")
                .questions(questions)
                .totalQuestions(questions.size())
                .estimatedTime(questions.size() * 2) // 2 minutes per question estimate
                .build();
    }
    
    /**
     * 퀴즈 시도 제출 및 채점
     */
    public QuizAttemptResponse submitAttempt(Long materialId, QuizAttemptRequest request) {
        // 사용자 정보 가져오기
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // 해당 material의 모든 퀴즈 가져오기
        List<Quiz> quizzes = quizRepository.findByStudyMaterial_IdAndStudyMaterial_UserId(materialId, user.getId());
        
        if (quizzes.isEmpty()) {
            throw new RuntimeException("No quizzes found for this material");
        }
        
        int totalScore = 0;
        int correctCount = 0;
        List<QuizAttempt> attempts = new ArrayList<>();
        
        // 각 답변 처리
        for (QuizAttemptRequest.Answer answer : request.getAnswers()) {
            Quiz quiz = quizzes.stream()
                    .filter(q -> q.getId().equals(answer.getQuestionId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Quiz not found: " + answer.getQuestionId()));
            
            String userAnswer = answer.getSelectedOption() >= 0 && answer.getSelectedOption() < quiz.getOptions().size() 
                    ? quiz.getOptions().get(answer.getSelectedOption()) 
                    : "";
            
            boolean isCorrect = quiz.getCorrectAnswer().equals(userAnswer);
            int score = calculateScore(quiz.getDifficulty(), isCorrect);
            
            if (isCorrect) {
                correctCount++;
            }
            totalScore += score;
            
            // 시도 기록 저장
            QuizAttempt attempt = QuizAttempt.builder()
                    .quiz(quiz)
                    .user(user)
                    .userAnswer(userAnswer)
                    .isCorrect(isCorrect)
                    .score(score)
                    .attemptedAt(LocalDateTime.now())
                    .build();
            
            attempts.add(attempt);
        }
        
        // 모든 시도 저장
        quizAttemptRepository.saveAll(attempts);
        
        // 전체 결과 반환
        return QuizAttemptResponse.builder()
                .score(correctCount)
                .total(quizzes.size())
                .percentage((double) correctCount / quizzes.size() * 100)
                .build();
    }
    
    /**
     * 특정 학습 자료의 퀴즈 시도 기록 조회
     */
    public List<QuizAttemptResponse> getMaterialAttempts(Long materialId, boolean onlyWrong) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<QuizAttempt> attempts;
        if (onlyWrong) {
            attempts = quizAttemptRepository.findByUserIdAndQuiz_StudyMaterial_IdAndIsCorrectFalse(
                    user.getId(), materialId);
        } else {
            attempts = quizAttemptRepository.findByUserIdAndQuiz_StudyMaterial_Id(
                    user.getId(), materialId);
        }
        
        return attempts.stream()
                .map(this::convertToAttemptResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 퀴즈 복습 데이터 생성
     */
    public QuizReviewResponse getQuizReview(Long materialId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // 해당 자료의 모든 퀴즈 시도 가져오기
        List<QuizAttempt> attempts = quizAttemptRepository.findByUserIdAndQuiz_StudyMaterial_Id(
                user.getId(), materialId);
        
        // 퀴즈별 시도 횟수 및 정답률 계산
        Map<Long, List<QuizAttempt>> attemptsByQuiz = attempts.stream()
                .collect(Collectors.groupingBy(a -> a.getQuiz().getId()));
        
        int totalAttempts = attempts.size();
        int correctAttempts = (int) attempts.stream().filter(QuizAttempt::getIsCorrect).count();
        double overallAccuracy = totalAttempts > 0 ? 
                (double) correctAttempts / totalAttempts * 100 : 0;
        
        List<QuizReviewResponse.QuizStats> quizStatsList = attemptsByQuiz.entrySet().stream()
                .map(entry -> {
                    Long quizId = entry.getKey();
                    List<QuizAttempt> quizAttempts = entry.getValue();
                    Quiz quiz = quizAttempts.get(0).getQuiz();
                    
                    int quizTotalAttempts = quizAttempts.size();
                    int quizCorrectAttempts = (int) quizAttempts.stream()
                            .filter(QuizAttempt::getIsCorrect).count();
                    double accuracy = (double) quizCorrectAttempts / quizTotalAttempts * 100;
                    
                    return QuizReviewResponse.QuizStats.builder()
                            .quizId(quizId)
                            .question(quiz.getQuestion())
                            .difficulty(quiz.getDifficulty().toString())
                            .totalAttempts(quizTotalAttempts)
                            .correctAttempts(quizCorrectAttempts)
                            .accuracy(accuracy)
                            .lastAttemptedAt(quizAttempts.stream()
                                    .map(QuizAttempt::getAttemptedAt)
                                    .max(LocalDateTime::compareTo)
                                    .orElse(null))
                            .build();
                })
                .collect(Collectors.toList());
        
        // 취약 문제 (정답률 50% 미만) 식별
        List<Long> weakQuizIds = quizStatsList.stream()
                .filter(stats -> stats.getAccuracy() < 50)
                .map(QuizReviewResponse.QuizStats::getQuizId)
                .collect(Collectors.toList());
        
        return QuizReviewResponse.builder()
                .materialId(materialId)
                .totalAttempts(totalAttempts)
                .correctAttempts(correctAttempts)
                .overallAccuracy(overallAccuracy)
                .quizStatsList(quizStatsList)
                .weakQuizIds(weakQuizIds)
                .build();
    }
    
    /**
     * 오답노트 조회
     */
    public List<WrongAnswerNoteResponse> getWrongAnswerNotes(Long courseId, int limit) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<QuizAttempt> wrongAttempts;
        if (courseId != null) {
            wrongAttempts = quizAttemptRepository.findWrongAttemptsByCourse(
                    user.getId(), courseId, limit);
        } else {
            wrongAttempts = quizAttemptRepository.findRecentWrongAttempts(
                    user.getId(), limit);
        }
        
        return wrongAttempts.stream()
                .map(this::convertToWrongAnswerNote)
                .collect(Collectors.toList());
    }
    
    /**
     * 학습 통계 생성
     */
    public Map<String, Object> getQuizStatistics(String period) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        LocalDateTime startDate = calculateStartDate(period);
        
        // 기간 내 전체 통계
        List<QuizAttempt> periodAttempts = quizAttemptRepository
                .findByUserIdAndAttemptedAtAfter(user.getId(), startDate);
        
        int totalAttempts = periodAttempts.size();
        int correctCount = (int) periodAttempts.stream()
                .filter(QuizAttempt::getIsCorrect).count();
        double accuracy = totalAttempts > 0 ? 
                (double) correctCount / totalAttempts * 100 : 0;
        
        // 난이도별 통계
        Map<String, Double> accuracyByDifficulty = periodAttempts.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getQuiz().getDifficulty().toString(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    long correct = list.stream()
                                            .filter(QuizAttempt::getIsCorrect).count();
                                    return list.isEmpty() ? 0.0 : 
                                            (double) correct / list.size() * 100;
                                }
                        )
                ));
        
        // 일별 학습량
        Map<LocalDateTime, Long> dailyAttempts = periodAttempts.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getAttemptedAt().toLocalDate().atStartOfDay(),
                        Collectors.counting()
                ));
        
        return Map.of(
                "period", period,
                "startDate", startDate,
                "totalAttempts", totalAttempts,
                "correctCount", correctCount,
                "accuracy", accuracy,
                "accuracyByDifficulty", accuracyByDifficulty,
                "dailyAttempts", dailyAttempts
        );
    }
    
    private int calculateScore(Quiz.Difficulty difficulty, boolean isCorrect) {
        if (!isCorrect) return 0;
        
        return switch (difficulty) {
            case EASY -> 10;
            case MEDIUM -> 20;
            case HARD -> 30;
        };
    }
    
    private LocalDateTime calculateStartDate(String period) {
        LocalDateTime now = LocalDateTime.now();
        return switch (period.toUpperCase()) {
            case "DAY" -> now.minusDays(1);
            case "WEEK" -> now.minusWeeks(1);
            case "MONTH" -> now.minusMonths(1);
            case "YEAR" -> now.minusYears(1);
            default -> now.minusWeeks(1);
        };
    }
    
    private QuizAttemptResponse convertToAttemptResponse(QuizAttempt attempt) {
        return QuizAttemptResponse.builder()
                .id(attempt.getId())
                .quizId(attempt.getQuiz().getId())
                .question(attempt.getQuiz().getQuestion())
                .userAnswer(attempt.getUserAnswer())
                .correctAnswer(attempt.getQuiz().getCorrectAnswer())
                .isCorrect(attempt.getIsCorrect())
                .score(attempt.getScore())
                .explanation(attempt.getQuiz().getExplanation())
                .attemptedAt(attempt.getAttemptedAt())
                .build();
    }
    
    private WrongAnswerNoteResponse convertToWrongAnswerNote(QuizAttempt attempt) {
        Quiz quiz = attempt.getQuiz();
        StudyMaterial material = quiz.getStudyMaterial();
        
        return WrongAnswerNoteResponse.builder()
                .attemptId(attempt.getId())
                .quizId(quiz.getId())
                .materialId(material.getId())
                .materialTitle(material.getTitle())
                .question(quiz.getQuestion())
                .userAnswer(attempt.getUserAnswer())
                .correctAnswer(quiz.getCorrectAnswer())
                .explanation(quiz.getExplanation())
                .difficulty(quiz.getDifficulty().toString())
                .attemptedAt(attempt.getAttemptedAt())
                .attemptCount(quizAttemptRepository.countByUserIdAndQuizId(
                        attempt.getUser().getId(), quiz.getId()))
                .build();
    }
}
