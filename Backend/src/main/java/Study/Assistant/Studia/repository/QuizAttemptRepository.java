package Study.Assistant.Studia.repository;

import Study.Assistant.Studia.domain.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    
    // 사용자의 특정 자료에 대한 모든 퀴즈 시도 조회
    @Query("SELECT qa FROM QuizAttempt qa " +
           "JOIN qa.quiz q " +
           "WHERE qa.user.id = :userId " +
           "AND q.studyMaterial.id = :materialId " +
           "ORDER BY qa.attemptedAt DESC")
    List<QuizAttempt> findByUserIdAndQuiz_StudyMaterial_Id(
            @Param("userId") Long userId, 
            @Param("materialId") Long materialId);
    
    // 사용자의 특정 자료에 대한 오답만 조회
    @Query("SELECT qa FROM QuizAttempt qa " +
           "JOIN qa.quiz q " +
           "WHERE qa.user.id = :userId " +
           "AND q.studyMaterial.id = :materialId " +
           "AND qa.isCorrect = false " +
           "ORDER BY qa.attemptedAt DESC")
    List<QuizAttempt> findByUserIdAndQuiz_StudyMaterial_IdAndIsCorrectFalse(
            @Param("userId") Long userId, 
            @Param("materialId") Long materialId);
    
    // 최근 오답 조회
    @Query("SELECT qa FROM QuizAttempt qa " +
           "WHERE qa.user.id = :userId " +
           "AND qa.isCorrect = false " +
           "ORDER BY qa.attemptedAt DESC " +
           "LIMIT :limit")
    List<QuizAttempt> findRecentWrongAttempts(
            @Param("userId") Long userId, 
            @Param("limit") int limit);
    
    // 특정 과목의 오답 조회
    @Query("SELECT qa FROM QuizAttempt qa " +
           "JOIN qa.quiz q " +
           "JOIN q.studyMaterial sm " +
           "WHERE qa.user.id = :userId " +
           "AND sm.course.id = :courseId " +
           "AND qa.isCorrect = false " +
           "ORDER BY qa.attemptedAt DESC " +
           "LIMIT :limit")
    List<QuizAttempt> findWrongAttemptsByCourse(
            @Param("userId") Long userId, 
            @Param("courseId") Long courseId, 
            @Param("limit") int limit);
    
    // 특정 기간 이후의 시도 조회
    List<QuizAttempt> findByUserIdAndAttemptedAtAfter(Long userId, LocalDateTime date);
    
    // 사용자가 특정 퀴즈를 시도한 횟수
    Integer countByUserIdAndQuizId(Long userId, Long quizId);
}
