package com.harshith.job_tracker.service;

import com.harshith.job_tracker.dto.PageResponse;
import com.harshith.job_tracker.exception.ResourceNotFoundException;
import com.harshith.job_tracker.model.ApplicationStatus;
import com.harshith.job_tracker.model.JobApplication;
import com.harshith.job_tracker.repository.JobApplicationRepository;
import com.harshith.job_tracker.repository.UserRepository;
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
    private final UserRepository userRepository;

    public JobApplicationService(JobApplicationRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    public JobApplication create(Long ownerId, JobApplication application) {
        application.setId(null); // a new application always gets a new id
        application.setOwner(userRepository.getReferenceById(ownerId));
        return repository.save(application);
    }

    public PageResponse<JobApplication> search(Long ownerId, ApplicationStatus status, String company,
                                               int page, int size, String sortBy, String direction) {
        String sortField = SORTABLE_FIELDS.contains(sortBy) ? sortBy : "appliedDate";
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);

        Pageable pageable = PageRequest.of(safePage, safeSize,
                Sort.by(sortDirection, sortField).and(Sort.by("id")));

        Specification<JobApplication> spec =
                (root, query, cb) -> cb.equal(root.get("owner").get("id"), ownerId);

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

    public JobApplication findById(Long ownerId, Long id) {
        return repository.findByIdAndOwner_Id(id, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id " + id));
    }

    public JobApplication update(Long ownerId, Long id, JobApplication updated) {
        JobApplication existing = findById(ownerId, id);
        existing.setCompanyName(updated.getCompanyName());
        existing.setJobTitle(updated.getJobTitle());
        existing.setStatus(updated.getStatus());
        existing.setAppliedDate(updated.getAppliedDate());
        existing.setJobUrl(updated.getJobUrl());
        existing.setNotes(updated.getNotes());
        return repository.save(existing);
    }

    public JobApplication updateStatus(Long ownerId, Long id, ApplicationStatus status) {
        JobApplication existing = findById(ownerId, id);
        existing.setStatus(status);
        return repository.save(existing);
    }

    public void delete(Long ownerId, Long id) {
        JobApplication existing = findById(ownerId, id);
        repository.delete(existing);
    }
}
