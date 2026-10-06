package com.harshith.job_tracker.dto;

import com.harshith.job_tracker.model.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(@NotNull ApplicationStatus status) {
}
