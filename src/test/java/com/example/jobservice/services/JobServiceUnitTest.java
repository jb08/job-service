package com.example.jobservice.services;

import static com.example.jobservice.utils.TestUtils.buildJob;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.jobservice.dtos.Job;
import com.example.jobservice.persistence.JobDao;
import com.github.javafaker.Faker;
import com.google.common.cache.LoadingCache;

@ExtendWith(MockitoExtension.class)
class JobServiceUnitTest {

    private static Faker faker =  new Faker();

    @Mock private JobDao jobDao;
    @Mock private LoadingCache<Integer, Job> jobCache;
    @InjectMocks private JobService jobService;

    @Test
    void updateJob_CacheHit_Valid() throws ExecutionException {
        Job oldJob = buildJob();
        Job job = spy(Job.builder()
                .id(oldJob.getId())
                .total(oldJob.getTotal())
                .progress(oldJob.getProgress() + faker.number().randomDigit())
                .build());

        when(jobCache.get(job.getId())).thenReturn(job);
        jobService.updateJob(job);

        verify(job, times(1)).setUpdatedAt(any());
        verify(jobCache, times(1)).put(job.getId(), job);
    }

    @Test
    void createJob_PersistedAndCacheRefreshed_Valid() {
        Job job = buildJob();
        int id = faker.number().randomDigit();
        when(jobDao.create(job)).thenReturn(id);
        assertEquals(id, jobService.createJob(job));
        verify(jobCache, times(1)).refresh(id);
    }

}
