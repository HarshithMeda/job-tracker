package com.harshith.job_tracker.repository;

import com.harshith.job_tracker.model.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface JobApplicationRepository
        extends JpaRepository<JobApplication, Long>, JpaSpecificationExecutor<JobApplication> {
}
