package com.harshith.job_tracker.service;

import com.harshith.job_tracker.dto.PageResponse;
import com.harshith.job_tracker.exception.ResourceNotFoundException;
import com.harshith.job_tracker.model.ApplicationStatus;
import com.harshith.job_tracker.model.JobApplication;
import com.harshith.job_tracker.repository.JobApplicationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class JobApplicationService {

    private static final Set<String> SORTABLE_FIELDS =
            Set.of("id", "companyName", "jobTitle", "status", "appliedDate");

    private final JobApplicationRepository repository;

    public JobApplicationService(JobApplicationRepository repository) {
        this.repository = repository;
    }

    public JobApplication create(JobApplication application) {
        application.setId(null); // a new application always gets a new id
        return repository.save(application);
    }

    public PageResponse<JobApplication> search(ApplicationStatus status, String company,
                                               int page, int size, String sortBy, String direction) {
        String sortField = SORTABLE_FIELDS.contains(sortBy) ? sortBy : "appliedDate";
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);

        Pageable pageable = PageRequest.of(safePage, safeSize,
                Sort.by(sortDirection, sortField).and(Sort.by("id")));

        Specification<JobApplication> spec = (root, query, cb) -> cb.conjunction();

        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (company != null && !company.isBlank()) {
            String pattern = "%" + company.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("companyName")), pattern));
        }

        Page<JobApplication> result = repository.findAll(spec, pageable);
        return new PageResponse<>(result.getContent(), result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
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
