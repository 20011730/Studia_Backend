package Study.Assistant.Studia.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {
    
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    
    @Value("${openai.api.key:}")
    private String openAiApiKey;
    
    @Value("${claude.api.key:}")
    private String claudeApiKey;
    
    @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String openAiUrl;
    
    @Value("${claude.api.url:https://api.anthropic.com/v1/messages}")
    private String claudeUrl;
    
    @Value("${ai.model:openai}")
    private String preferredModel;
    
    public String generateSummary(String content) {
        String prompt = """
            다음 내용을 한국어로 체계적으로 요약해주세요.
            주요 아이디어, 핵심 개념, 중요한 세부사항에 초점을 맞춰주세요.
            요약은 체계적이고 이해하기 쉬워야 합니다.
            
            내용:
            %s
            """.formatted(content);
        
        return callAI(prompt, "summary");
    }
    
    public List<String> extractKeyPoints(String content) {
        String prompt = """
            다음 내용에서 핵심 포인트를 추출해주세요.
            각 핵심 포인트를 별도의 항목으로 나열해주세요.
            가장 중요한 개념과 사실에 초점을 맞춰주세요.
            
            내용:
            %s
            """.formatted(content);
        
        String response = callAI(prompt, "key-points");
        return parseListResponse(response);
    }
    
    public List<Map<String, Object>> generateQuizzes(String content, int count, String difficulty) {
        String prompt = """
            다음 내용을 바탕으로 %d개의 퀴즈 문제를 만들어주세요.
            난이도: %s
            각 문제에 대해 다음을 제공해주세요:
            1. 문제 텍스트
            2. 객관식 선택지 (해당하는 경우)
            3. 정답
            4. 간단한 설명
            5. 난이도 (EASY, MEDIUM, HARD 중 하나)
            
            JSON 배열 형식으로 응답해주세요.
            
            내용:
            %s
            """.formatted(count, difficulty, content);
        
        String response = callAI(prompt, "quiz-generation");
        return parseQuizResponse(response);
    }
    
    public String generateStudyPlan(List<Map<String, Object>> courses, List<Map<String, Object>> exams) {
        String prompt = """
            다음 수업 일정과 시험 일정을 고려하여 효율적인 학습 계획을 세워주세요:
            
            수업 일정: %s
            
            시험 일정: %s
            
            다음 사항을 포함해주세요:
            1. 일일 학습 권장 시간
            2. 과목별 우선순위
            3. 복습 주기
            4. 시험 대비 전략
            """.formatted(courses.toString(), exams.toString());
        
        return callAI(prompt, "study-plan");
    }
    
    private String callAI(String prompt, String purpose) {
        try {
            if ("openai".equalsIgnoreCase(preferredModel) && !openAiApiKey.isEmpty()) {
                return callOpenAI(prompt);
            } else if ("claude".equalsIgnoreCase(preferredModel) && !claudeApiKey.isEmpty()) {
                return callClaude(prompt);
            } else {
                log.warn("No AI API key configured. Using mock response for: {}", purpose);
                return getMockResponse(purpose);
            }
        } catch (Exception e) {
            log.error("Error calling AI API: ", e);
            return getMockResponse(purpose);
        }
    }
    
    private String callOpenAI(String prompt) {
        WebClient webClient = webClientBuilder
                .baseUrl(openAiUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + openAiApiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        
        Map<String, Object> requestBody = Map.of(
                "model", "gpt-3.5-turbo",
                "messages", List.of(
                        Map.of("role", "system", "content", "You are a helpful AI assistant for a study application. Always respond in Korean."),
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.7,
                "max_tokens", 1000
        );
        
        return webClient.post()
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .map(this::extractOpenAIResponse)
                .block();
    }
    
    private String callClaude(String prompt) {
        WebClient webClient = webClientBuilder
                .baseUrl(claudeUrl)
                .defaultHeader("x-api-key", claudeApiKey)
                .defaultHeader("anthropic-version", "2023-06-01")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        
        Map<String, Object> requestBody = Map.of(
                "model", "claude-3-haiku-20240307",
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                ),
                "max_tokens", 1000
        );
        
        return webClient.post()
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .map(this::extractClaudeResponse)
                .block();
    }
    
    private String extractOpenAIResponse(String response) {
        try {
            Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                return (String) message.get("content");
            }
        } catch (Exception e) {
            log.error("Error extracting OpenAI response: ", e);
        }
        return "";
    }
    
    private String extractClaudeResponse(String response) {
        try {
            Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
            List<Map<String, Object>> content = (List<Map<String, Object>>) responseMap.get("content");
            if (content != null && !content.isEmpty()) {
                return (String) content.get(0).get("text");
            }
        } catch (Exception e) {
            log.error("Error extracting Claude response: ", e);
        }
        return "";
    }
    
    private List<String> parseListResponse(String response) {
        List<String> result = new ArrayList<>();
        String[] lines = response.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            // Remove bullet points, numbers, etc.
            line = line.replaceAll("^[-•*\\d.]+\\s*", "");
            if (!line.isEmpty()) {
                result.add(line);
            }
        }
        return result;
    }
    
    private List<Map<String, Object>> parseQuizResponse(String response) {
        try {
            // AI가 JSON 형식으로 응답하도록 요청했으므로 파싱 시도
            return objectMapper.readValue(response, List.class);
        } catch (Exception e) {
            log.warn("Failed to parse quiz response as JSON, using fallback parser");
            return parsePlainTextQuiz(response);
        }
    }
    
    private List<Map<String, Object>> parsePlainTextQuiz(String response) {
        List<Map<String, Object>> quizzes = new ArrayList<>();
        // 간단한 텍스트 파싱 로직 - 실제 사용시 더 정교한 파싱 필요
        
        Map<String, Object> mockQuiz = new HashMap<>();
        mockQuiz.put("question", "예제 문제입니다.");
        mockQuiz.put("options", List.of("선택지 1", "선택지 2", "선택지 3", "선택지 4"));
        mockQuiz.put("correctAnswer", "선택지 1");
        mockQuiz.put("explanation", "이것이 정답인 이유입니다.");
        mockQuiz.put("difficulty", "MEDIUM");
        
        quizzes.add(mockQuiz);
        return quizzes;
    }
    
    private String getMockResponse(String purpose) {
        switch (purpose) {
            case "summary":
                return """
                    ## 요약
                    
                    이 내용은 주요 개념과 중요한 세부사항을 다루고 있습니다.
                    
                    ### 핵심 개념
                    1. 첫 번째 핵심 개념에 대한 설명
                    2. 두 번째 핵심 개념에 대한 설명
                    
                    ### 주요 내용
                    - 중요한 포인트 1
                    - 중요한 포인트 2
                    
                    ### 결론
                    전체적인 내용을 종합하면 다음과 같습니다.
                    """;
            
            case "key-points":
                return """
                    - 핵심 포인트 1: 중요한 개념에 대한 설명
                    - 핵심 포인트 2: 핵심 정보 강조
                    - 핵심 포인트 3: 기억해야 할 필수 세부사항
                    - 핵심 포인트 4: 중요한 발견 또는 결론
                    """;
            
            case "quiz-generation":
                // 향상된 Mock 퀴즈 데이터
                List<Map<String, Object>> mockQuizzes = new ArrayList<>();
                
                String[] questionTypes = {"CONCEPT", "APPLICATION", "ANALYSIS", "COMPARISON", "TRUE_FALSE"};
                String[] categories = {"핵심개념", "세부사항", "응용문제", "종합이해"};
                
                for (int i = 0; i < 5; i++) {
                    Map<String, Object> quiz = new HashMap<>();
                    String questionType = questionTypes[i % questionTypes.length];
                    String category = categories[i % categories.length];
                    
                    switch (questionType) {
                        case "CONCEPT":
                            quiz.put("question", "Spring Framework의 IoC(Inversion of Control)가 제공하는 주요 이점은 무엇입니까?");
                            quiz.put("options", List.of(
                                "객체 간 결합도를 낮추고 유연성을 높여 테스트와 유지보수가 용이해진다",
                                "실행 속도가 빨라지고 메모리 사용량이 줄어든다",
                                "데이터베이스 연결이 자동으로 관리된다",
                                "컴파일 시간이 단축된다"
                            ));
                            quiz.put("correctAnswer", "객체 간 결합도를 낮추고 유연성을 높여 테스트와 유지보수가 용이해진다");
                            quiz.put("explanation", "IoC는 객체의 생성과 의존관계 설정을 프레임워크가 담당하여 느슨한 결합을 실현합니다.");
                            break;
                            
                        case "APPLICATION":
                            quiz.put("question", "@Transactional 어노테이션을 사용할 때 주의해야 할 점은?");
                            quiz.put("options", List.of(
                                "public 메소드에만 적용되며, 같은 클래스 내부 호출시에는 작동하지 않는다",
                                "private 메소드에만 사용해야 한다",
                                "static 메소드에서만 작동한다",
                                "모든 메소드에 자동으로 적용된다"
                            ));
                            quiz.put("correctAnswer", "public 메소드에만 적용되며, 같은 클래스 내부 호출시에는 작동하지 않는다");
                            quiz.put("explanation", "Spring AOP는 프록시 기반으로 작동하므로 이러한 제약사항이 있습니다.");
                            break;
                            
                        case "ANALYSIS":
                            quiz.put("question", "데이터베이스 인덱스를 과도하게 생성했을 때 발생할 수 있는 문제는?");
                            quiz.put("options", List.of(
                                "INSERT, UPDATE, DELETE 성능이 저하되고 저장 공간이 증가한다",
                                "SELECT 쿼리가 느려진다",
                                "데이터베이스 연결이 불가능해진다",
                                "트랜잭션이 자동으로 롤백된다"
                            ));
                            quiz.put("correctAnswer", "INSERT, UPDATE, DELETE 성능이 저하되고 저장 공간이 증가한다");
                            quiz.put("explanation", "인덱스는 조회 성능을 향상시키지만, 데이터 변경 시 인덱스도 함께 업데이트해야 하므로 쓰기 성능이 저하됩니다.");
                            break;
                            
                        case "COMPARISON":
                            quiz.put("question", "ArrayList와 LinkedList의 차이점으로 올바른 것은?");
                            quiz.put("options", List.of(
                                "ArrayList는 인덱스 접근이 O(1), LinkedList는 삽입/삭제가 O(1)이다",
                                "둘 다 같은 성능을 보인다",
                                "LinkedList가 모든 면에서 더 빠르다",
                                "ArrayList는 삽입/삭제가 더 빠르다"
                            ));
                            quiz.put("correctAnswer", "ArrayList는 인덱스 접근이 O(1), LinkedList는 삽입/삭제가 O(1)이다");
                            quiz.put("explanation", "자료구조 선택은 사용 패턴에 따라 결정해야 합니다. 빈번한 접근이 필요하면 ArrayList, 잦은 삽입/삭제가 필요하면 LinkedList가 유리합니다.");
                            break;
                            
                        case "TRUE_FALSE":
                            quiz.put("question", "Spring Boot는 반드시 내장 톰캣 서버를 사용해야 한다. 이 설명이 맞습니까?");
                            quiz.put("options", List.of(
                                "거짓 - Spring Boot는 Jetty, Undertow 등 다른 서버도 사용 가능하다",
                                "참 - Spring Boot는 톰캣만 지원한다",
                                "거짓 - Spring Boot는 서버를 사용하지 않는다",
                                "참 - 다른 서버를 사용하려면 Spring Framework를 써야 한다"
                            ));
                            quiz.put("correctAnswer", "거짓 - Spring Boot는 Jetty, Undertow 등 다른 서버도 사용 가능하다");
                            quiz.put("explanation", "Spring Boot는 기본적으로 톰캣을 사용하지만, 의존성을 변경하여 다른 서버를 사용할 수 있습니다.");
                            break;
                    }
                    
                    quiz.put("category", category);
                    quiz.put("hint", "핵심 개념을 정확히 이해하고 실제 적용 사례를 생각해보세요.");
                    quiz.put("difficulty", i < 2 ? "EASY" : i < 4 ? "MEDIUM" : "HARD");
                    
                    mockQuizzes.add(quiz);
                }
                
                try {
                    return objectMapper.writeValueAsString(mockQuizzes);
                } catch (Exception e) {
                    return "[]";
                }

            
            case "study-plan":
                return """
                    ## 맞춤형 학습 계획
                    
                    ### 일일 학습 권장 시간
                    - 평일: 3-4시간
                    - 주말: 5-6시간
                    
                    ### 과목별 우선순위
                    1. 시험이 가까운 과목 우선
                    2. 난이도가 높은 과목에 더 많은 시간 할당
                    3. 기초 과목은 꾸준히 복습
                    
                    ### 복습 주기
                    - 새로운 내용: 24시간 이내 첫 복습
                    - 주간 복습: 매주 토요일
                    - 월간 복습: 매월 마지막 주
                    
                    ### 시험 대비 전략
                    - 시험 2주 전부터 집중 복습 시작
                    - 과거 문제 풀이
                    - 요약 노트 작성
                    """;
            
            default:
                return "Mock response for " + purpose;
        }
    }
}
