package com.harshith.job_tracker.controller;

import com.harshith.job_tracker.dto.PageResponse;
import com.harshith.job_tracker.dto.StatusUpdateRequest;
import com.harshith.job_tracker.model.ApplicationStatus;
import com.harshith.job_tracker.model.JobApplication;
import com.harshith.job_tracker.service.JobApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/applications")
public class JobApplicationController {

    private final JobApplicationService service;

    public JobApplicationController(JobApplicationService service) {
        this.service = service;
    }

    private Long userId(Jwt jwt) {
        return ((Number) jwt.getClaim("uid")).longValue();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JobApplication create(@AuthenticationPrincipal Jwt jwt,
                                 @Valid @RequestBody JobApplication application) {
        return service.create(userId(jwt), application);
    }

    @GetMapping
    public PageResponse<JobApplication> list(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(required = false) String company,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "appliedDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        return service.search(userId(jwt), status, company, page, size, sortBy, direction);
    }

    @GetMapping("/{id}")
    public JobApplication getOne(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return service.findById(userId(jwt), id);
    }

    @PutMapping("/{id}")
    public JobApplication update(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                 @Valid @RequestBody JobApplication application) {
        return service.update(userId(jwt), id, application);
    }

    @PatchMapping("/{id}/status")
    public JobApplication updateStatus(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                       @Valid @RequestBody StatusUpdateRequest request) {
        return service.updateStatus(userId(jwt), id, request.status());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        service.delete(userId(jwt), id);
    }
}
