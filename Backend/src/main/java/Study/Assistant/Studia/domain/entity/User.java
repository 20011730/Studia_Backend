package Study.Assistant.Studia.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String university;
    
    private String major;
    
    private Integer grade;
    
    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.STUDENT;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Course> courses = new ArrayList<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<StudyMaterial> studyMaterials = new ArrayList<>();
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    public enum UserRole {
        STUDENT, ADMIN
    }
}
