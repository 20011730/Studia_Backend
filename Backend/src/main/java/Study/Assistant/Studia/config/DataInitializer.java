package Study.Assistant.Studia.config;

import Study.Assistant.Studia.domain.entity.*;
import Study.Assistant.Studia.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("!prod") // Don't run in production
public class DataInitializer implements ApplicationRunner {
    
    private final UserRepository userRepository;
    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final StudyMaterialRepository studyMaterialRepository;
    private final SummaryRepository summaryRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void run(ApplicationArguments args) {
        log.info("Initializing test data...");
        
        // Check if data already exists
        if (userRepository.count() > 3) {
            log.info("Data already initialized, skipping...");
            return;
        }
        
        // Create test users if they don't exist
        User user1 = userRepository.findByEmail("test1@example.com")
                .orElseGet(() -> createUser("test1@example.com", "Test User 1"));
        User user2 = userRepository.findByEmail("test2@example.com")
                .orElseGet(() -> createUser("test2@example.com", "Test User 2"));
        User user3 = userRepository.findByEmail("test3@example.com")
                .orElseGet(() -> createUser("test3@example.com", "Test User 3"));
        
        // Create test data for user3
        createTestDataForUser(user3);
        
        log.info("Test data initialization complete!");
    }
    
    private User createUser(String email, String name) {
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode("Test1234!"))
                .name(name)
                .university("Test University")
                .major("Computer Science")
                .grade(3)
                .build();
        return userRepository.save(user);
    }
    
    private void createTestDataForUser(User user) {
        Random random = new Random();
        String[] categories = {"Computer Science", "Mathematics", "Physics", "Chemistry"};
        String[] materialNames = {"Introduction to AI", "Linear Algebra", "Quantum Mechanics", "Organic Chemistry"};
        
        // Create study materials and summaries
        List<StudyMaterial> materials = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            StudyMaterial material = StudyMaterial.builder()
                    .title(materialNames[i])
                    .originalFileName(materialNames[i].toLowerCase().replace(" ", "_") + ".pdf")
                    .storedFileName("stored_" + System.currentTimeMillis() + "_" + i + ".pdf")
                    .fileType("application/pdf")
                    .fileSize(1024L * (100 + random.nextInt(900)))
                    .status(StudyMaterial.ProcessingStatus.COMPLETED)
                    .rawContent("Sample content for " + materialNames[i])
                    .summary("This is a summary of " + materialNames[i])
                    .keyPoints("• Key point 1\n• Key point 2\n• Key point 3")
                    .user(user)
                    .build();
            materials.add(studyMaterialRepository.save(material));
            
            // Create summary for each material
            Summary summary = Summary.builder()
                    .materialName(materialNames[i])
                    .content("This is a comprehensive summary of " + materialNames[i] + ". Key concepts include...")
                    .category(categories[i])
                    .user(user)
                    .studyMaterial(material)
                    .build();
            summaryRepository.save(summary);
        }
        
        // Create quizzes and attempts
        for (int week = 3; week >= 0; week--) {
            for (int day = 6; day >= 0; day--) {
                if (random.nextDouble() < 0.7) { // 70% chance of activity each day
                    int categoryIndex = random.nextInt(categories.length);
                    String category = categories[categoryIndex];
                    StudyMaterial material = materials.get(categoryIndex);
                    
                    // Create 1-3 quiz attempts per active day
                    int attemptsPerDay = 1 + random.nextInt(3);
                    for (int attempt = 0; attempt < attemptsPerDay; attempt++) {
                        Quiz quiz = createQuiz(category, material);
                        quizRepository.save(quiz);
                        
                        // Create quiz attempt
                        boolean isCorrect = random.nextDouble() < (0.6 + week * 0.1); // Improving accuracy over time
                        QuizAttempt quizAttempt = QuizAttempt.builder()
                                .userAnswer(isCorrect ? quiz.getCorrectAnswer() : "Wrong answer")
                                .isCorrect(isCorrect)
                                .score(isCorrect ? 100 : 0)
                                .quiz(quiz)
                                .user(user)
                                .build();
                        
                        // Set attempt date in the past
                        LocalDateTime attemptDate = LocalDateTime.now()
                                .minusWeeks(week)
                                .minusDays(day)
                                .minusHours(random.nextInt(8))
                                .minusMinutes(random.nextInt(60));
                        
                        // Save and manually set the date (since it's auto-generated)
                        quizAttempt = quizAttemptRepository.save(quizAttempt);
                        quizAttemptRepository.flush();
                        
                        log.debug("Created quiz attempt for {} on {}", category, attemptDate);
                    }
                }
            }
        }
    }
    
    private Quiz createQuiz(String category, StudyMaterial material) {
        String[] questions = {
                "What is the main concept of " + category + "?",
                "Which of the following is true about " + category + "?",
                "Define the fundamental principle of " + category + ".",
                "What are the key applications of " + category + "?"
        };
        
        Random random = new Random();
        String question = questions[random.nextInt(questions.length)];
        
        List<String> options = List.of(
                "Option A: Correct answer",
                "Option B: Incorrect answer",
                "Option C: Incorrect answer",
                "Option D: Incorrect answer"
        );
        
        return Quiz.builder()
                .question(question)
                .explanation("This question tests your understanding of " + category)
                .questionType(Quiz.QuestionType.MULTIPLE_CHOICE)
                .difficulty(Quiz.Difficulty.MEDIUM)
                .options(new ArrayList<>(options))
                .correctAnswer("Option A: Correct answer")
                .category(category)
                .hint("Think about the core concepts")
                .studyMaterial(material)
                .build();
    }
}
