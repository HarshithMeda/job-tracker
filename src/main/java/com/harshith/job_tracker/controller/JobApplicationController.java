package com.harshith.job_tracker.controller;

import com.harshith.job_tracker.model.JobApplication;
import com.harshith.job_tracker.service.JobApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public List<JobApplication> list() {
        return service.findAll();
    }
}
