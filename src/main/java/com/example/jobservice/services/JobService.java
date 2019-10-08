package com.example.jobservice.services;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.jobservice.dtos.Job;
import com.example.jobservice.exceptions.JobNotFound;
import com.example.jobservice.persistence.JobDao;
import com.google.common.cache.LoadingCache;

public class JobService {

    private JobDao jobDao;
    private LoadingCache<Integer, Job> jobCache;
    ScheduledExecutorService scheduledExecutorService;

    private static final Logger LOGGER = LoggerFactory.getLogger(JobService.class);

    /**
     * Create a JobService.
     *
     * @param jobDao the DAO dependency
     * @param jobCache the cache dependency
     */
    public JobService(JobDao jobDao, LoadingCache<Integer, Job> jobCache) {
        this.jobDao = jobDao;
        this.jobCache = jobCache;

        // preload cache
        Map<Integer, Job> jobs = jobDao.getJobs()
                .stream()
                .collect(Collectors.toMap(Job::getId, job -> job));

        jobCache.putAll(jobs);

        // configure job cleanup cron
        scheduledExecutorService = Executors.newScheduledThreadPool(5);
        ScheduledFuture scheduledFuture = scheduledExecutorService
                .scheduleWithFixedDelay(this::removeOldJobs, 0, 5, TimeUnit.SECONDS);
    }

    /**
     * Get all Jobs.
     *
     * @return the List of Users
     */
    public List<Job> getJobs() {
        Map<Integer, Job> cachedJobs = jobCache.asMap();

        Map<Integer, Job> persistedJobs = jobDao.getJobs()
                .stream()
                .collect(Collectors.toMap(Job::getId, job -> job));

        // merge cached and persisted jobs; cached version takes precedence
        persistedJobs.putAll(cachedJobs);
        return new ArrayList<>(persistedJobs.values());
    }

    /**
     * Retrieve the Optional Job with the given id.
     *
     * @param id the id of the Job to look for
     * @return the Optional Job
     */
    public Optional<Job> getJobById(int id) {
        try {
            return Optional.of(jobCache.get(id));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Update the given Job.
     *
     * @param job the Job as desired
     */
    public void updateJob(Job job) {
        Optional<Job> optJob = getJobById(job.getId());
        if (optJob.isPresent()) {
            job.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
            jobCache.put(job.getId(), job);
        } else {
            throw new JobNotFound(job.getId());
        }
    }

    /**
     * Create the given Job.
     *
     * @param job the Job as desired
     * @return the id corresponding to the Job
     */
    public int createJob(Job job) {
        int id = jobDao.create(job);
        jobCache.refresh(id);
        return id;
    }

    private void removeOldJobs() {
        LOGGER.debug("removeOldJobs() started.");

        Timestamp cutoffTime = Timestamp.valueOf(LocalDateTime.now().minusMinutes(1));
        List<Integer> expiredJobIds = new ArrayList<>();

        for (Job job : jobCache.asMap().values()) {
            Timestamp updatedAt = job.getUpdatedAt();
            boolean expired = updatedAt.before(cutoffTime);
            LOGGER.debug(String.format("Job updated at: %s | expired time: %s | expired: %s",
                    updatedAt, cutoffTime, expired));
            if (expired) {
               expiredJobIds.add(job.getId());
            }
        }

        jobCache.invalidateAll(expiredJobIds);
    }
}
