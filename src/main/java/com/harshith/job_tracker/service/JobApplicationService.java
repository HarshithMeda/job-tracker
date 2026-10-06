package com.harshith.job_tracker.service;

import com.harshith.job_tracker.model.JobApplication;
import com.harshith.job_tracker.repository.JobApplicationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobApplicationService {

    private final JobApplicationRepository repository;

    public JobApplicationService(JobApplicationRepository repository) {
        this.repository = repository;
    }

    public JobApplication create(JobApplication application) {
        application.setId(null); // a new application always gets a new id
        return repository.save(application);
    }

    public List<JobApplication> findAll() {
        return repository.findAll();
    }
}
