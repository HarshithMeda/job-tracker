package com.harshith.job_tracker.controller;

import com.harshith.job_tracker.dto.PageResponse;
import com.harshith.job_tracker.dto.StatusUpdateRequest;
import com.harshith.job_tracker.model.ApplicationStatus;
import com.harshith.job_tracker.model.JobApplication;
import com.harshith.job_tracker.service.JobApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/applications")
public class JobApplicationController {

    private final JobApplicationService service;

    public JobApplicationController(JobApplicationService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JobApplication create(@Valid @RequestBody JobApplication application) {
        return service.create(application);
    }

    @GetMapping
    public PageResponse<JobApplication> list(
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(required = false) String company,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "appliedDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        return service.search(status, company, page, size, sortBy, direction);
    }

    @GetMapping("/{id}")
    public JobApplication getOne(@PathVariable Long id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    public JobApplication update(@PathVariable Long id, @Valid @RequestBody JobApplication application) {
        return service.update(id, application);
    }

    @PatchMapping("/{id}/status")
    public JobApplication updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
        return service.updateStatus(id, request.status());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
