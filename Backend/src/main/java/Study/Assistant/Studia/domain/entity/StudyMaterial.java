package Study.Assistant.Studia.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "study_materials")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class StudyMaterial {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false)
    private String originalFileName;
    
    @Column(nullable = false)
    private String storedFileName;
    
    private String fileType;
    
    private Long fileSize;
    
    @Column(columnDefinition = "TEXT")
    private String rawContent;
    
    @Column(columnDefinition = "TEXT")
    private String summary;
    
    @Column(columnDefinition = "TEXT")
    private String keyPoints;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;
    
    @Column
    private String className;
    
    @OneToMany(mappedBy = "studyMaterial", cascade = CascadeType.ALL)
    private List<Quiz> quizzes = new ArrayList<>();
    
    @Enumerated(EnumType.STRING)
    private ProcessingStatus status = ProcessingStatus.UPLOADED;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    private LocalDateTime processedAt;
    
    public enum ProcessingStatus {
        UPLOADED, PROCESSING, COMPLETED, FAILED
    }
}
