package com.harshith.job_tracker.service;

import com.harshith.job_tracker.exception.ResourceNotFoundException;
import com.harshith.job_tracker.model.ApplicationStatus;
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

    public JobApplication findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id " + id));
    }

    public JobApplication update(Long id, JobApplication updated) {
        JobApplication existing = findById(id);
        existing.setCompanyName(updated.getCompanyName());
        existing.setJobTitle(updated.getJobTitle());
        existing.setStatus(updated.getStatus());
        existing.setAppliedDate(updated.getAppliedDate());
        existing.setJobUrl(updated.getJobUrl());
        existing.setNotes(updated.getNotes());
        return repository.save(existing);
    }

    public JobApplication updateStatus(Long id, ApplicationStatus status) {
        JobApplication existing = findById(id);
        existing.setStatus(status);
        return repository.save(existing);
    }

    public void delete(Long id) {
        JobApplication existing = findById(id);
        repository.delete(existing);
    }
}
