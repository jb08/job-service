package com.example.jobservice.controllers;

import static com.example.jobservice.utils.TestUtils.buildJob;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.jobservice.dtos.Job;
import com.example.jobservice.exceptions.BadRequest;
import com.example.jobservice.exceptions.JobNotFound;
import com.example.jobservice.services.JobService;
import com.github.javafaker.Faker;

@ExtendWith(MockitoExtension.class)
class JobControllerUnitTest {

    private static Faker faker =  new Faker();

    @Mock private JobService jobService;
    @InjectMocks private JobController jobController;

    @Test
    void getJobs_CallsService_Valid() {
        jobController.getJobs();
        verify(jobService, times(1)).getJobs();
    }

    @Test
    void updateJobById_mismatchedIds_Throws() {
        Job job = spy(buildJob());
        assertThrows(BadRequest.class, () -> jobController.updateJobById(faker.number().randomDigit(), job));
        verify(job, times(1)).validate();
    }

    @Test
    void updateJobById_NotExists_Throws() {
        Job job = buildJob();
        jobController.updateJobById(job.getId(), job);
        verify(jobService, times(1)).updateJob(job);
    }

    @Test
    void updateJobProgress_NotExists_Throws() {
        Job job = buildJob();
        when(jobService.getJobById(job.getId())).thenReturn(Optional.empty());
        Exception e = assertThrows(JobNotFound.class, () -> jobController.updateJobProgress(job.getId(), faker.number().randomDigit()));
        assertEquals(String.format("Job not found with id: %s.", job.getId()), e.getMessage());
    }

}
