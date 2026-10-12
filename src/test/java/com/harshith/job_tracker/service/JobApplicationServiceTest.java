package com.harshith.job_tracker.service;

import com.harshith.job_tracker.exception.ResourceNotFoundException;
import com.harshith.job_tracker.model.AppUser;
import com.harshith.job_tracker.model.ApplicationStatus;
import com.harshith.job_tracker.model.JobApplication;
import com.harshith.job_tracker.repository.JobApplicationRepository;
import com.harshith.job_tracker.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobApplicationServiceTest {

    @Mock
    private JobApplicationRepository repository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JobApplicationService service;

    private JobApplication sampleApplication() {
        JobApplication app = new JobApplication();
        app.setId(5L);
        app.setCompanyName("Google");
        app.setJobTitle("Software Engineer");
        app.setStatus(ApplicationStatus.APPLIED);
        app.setAppliedDate(LocalDate.of(2026, 10, 4));
        return app;
    }

    @Test
    void findById_returnsApplication_whenOwnerMatches() {
        JobApplication app = sampleApplication();
        when(repository.findByIdAndOwner_Id(5L, 1L)).thenReturn(Optional.of(app));

        JobApplication result = service.findById(1L, 5L);

        assertSame(app, result);
    }

    @Test
    void findById_throwsNotFound_whenMissingOrOwnedBySomeoneElse() {
        when(repository.findByIdAndOwner_Id(5L, 2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(2L, 5L));
    }

    @Test
    void create_setsOwnerAndIgnoresClientSuppliedId() {
        AppUser owner = new AppUser();
        JobApplication app = sampleApplication(); // arrives with id 5, which must be ignored
        when(userRepository.getReferenceById(1L)).thenReturn(owner);
        when(repository.save(any(JobApplication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JobApplication result = service.create(1L, app);

        assertNull(result.getId());
        assertSame(owner, result.getOwner());
    }

    @Test
    void updateStatus_changesOnlyTheStatus() {
        JobApplication app = sampleApplication();
        when(repository.findByIdAndOwner_Id(5L, 1L)).thenReturn(Optional.of(app));
        when(repository.save(any(JobApplication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JobApplication result = service.updateStatus(1L, 5L, ApplicationStatus.INTERVIEW);

        assertEquals(ApplicationStatus.INTERVIEW, result.getStatus());
        assertEquals("Google", result.getCompanyName());
    }

    @Test
    void delete_removesApplication_whenOwnerMatches() {
        JobApplication app = sampleApplication();
        when(repository.findByIdAndOwner_Id(5L, 1L)).thenReturn(Optional.of(app));

        service.delete(1L, 5L);

        verify(repository).delete(app);
    }

    @Test
    void delete_doesNothing_whenApplicationBelongsToSomeoneElse() {
        when(repository.findByIdAndOwner_Id(5L, 2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.delete(2L, 5L));

        verify(repository, never()).delete(any(JobApplication.class));
    }
}
