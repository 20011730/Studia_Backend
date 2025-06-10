package Study.Assistant.Studia.controller;

import Study.Assistant.Studia.dto.request.StudyPlanRequest;
import Study.Assistant.Studia.dto.response.StudyPlanResponse;
import Study.Assistant.Studia.service.StudyPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/study-plans")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8000"})
public class StudyPlanController {
    
    private final StudyPlanService studyPlanService;
    
    /**
     * Create a new study plan
     */
    @PostMapping
    public ResponseEntity<StudyPlanResponse> createStudyPlan(@RequestBody StudyPlanRequest request) {
        log.info("Creating study plan: {}", request);
        StudyPlanResponse response = studyPlanService.createStudyPlan(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Get all study plans for the current user
     */
    @GetMapping
    public ResponseEntity<List<StudyPlanResponse>> getAllStudyPlans() {
        log.info("Getting all study plans");
        List<StudyPlanResponse> plans = studyPlanService.getAllStudyPlans();
        return ResponseEntity.ok(plans);
    }
    
    /**
     * Get study plans by date range
     */
    @GetMapping("/range")
    public ResponseEntity<List<StudyPlanResponse>> getStudyPlansByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Getting study plans from {} to {}", startDate, endDate);
        List<StudyPlanResponse> plans = studyPlanService.getStudyPlansByDateRange(startDate, endDate);
        return ResponseEntity.ok(plans);
    }
    
    /**
     * Get a specific study plan
     */
    @GetMapping("/{id}")
    public ResponseEntity<StudyPlanResponse> getStudyPlan(@PathVariable Long id) {
        log.info("Getting study plan with id: {}", id);
        StudyPlanResponse plan = studyPlanService.getStudyPlan(id);
        return ResponseEntity.ok(plan);
    }
    
    /**
     * Update a study plan
     */
    @PutMapping("/{id}")
    public ResponseEntity<StudyPlanResponse> updateStudyPlan(
            @PathVariable Long id,
            @RequestBody StudyPlanRequest request) {
        log.info("Updating study plan {} with data: {}", id, request);
        StudyPlanResponse response = studyPlanService.updateStudyPlan(id, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Delete a study plan
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudyPlan(@PathVariable Long id) {
        log.info("Deleting study plan with id: {}", id);
        studyPlanService.deleteStudyPlan(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Delete recurring study plans
     */
    @DeleteMapping("/{id}/recurring")
    public ResponseEntity<Void> deleteRecurringStudyPlans(
            @PathVariable Long id,
            @RequestParam(required = false) String groupId) {
        log.info("Deleting recurring study plans with id: {} and groupId: {}", id, groupId);
        studyPlanService.deleteRecurringStudyPlans(id, groupId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Update recurring study plans
     */
    @PutMapping("/{id}/recurring")
    public ResponseEntity<List<StudyPlanResponse>> updateRecurringStudyPlans(
            @PathVariable Long id,
            @RequestParam(required = false) String groupId,
            @RequestBody StudyPlanRequest request) {
        log.info("Updating recurring study plans with id: {} and groupId: {}", id, groupId);
        List<StudyPlanResponse> responses = studyPlanService.updateRecurringStudyPlans(id, groupId, request);
        return ResponseEntity.ok(responses);
    }
}
