package Study.Assistant.Studia.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyPlanResponse {
    private Long id;
    private String title;
    private String type;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean allDay;
    private String color;
    private boolean repeat;
    private String repeatType;
    private LocalDate repeatUntil;
    private List<Integer> repeatDays;
    private String className;
    private String description;
    private String groupId;
    private String repeatGroupId;
    private String createdAt;
    private String updatedAt;
}
