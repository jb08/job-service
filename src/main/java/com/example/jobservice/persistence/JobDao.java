package com.example.jobservice.persistence;

import java.util.List;
import java.util.Optional;

import org.jdbi.v3.core.Jdbi;

import com.example.jobservice.dtos.Job;

public class JobDao {

    Jdbi jdbi;

    public JobDao(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public int create(Job job) {
        return jdbi.withHandle(handle -> {
                return handle.createQuery("INSERT INTO demo.jobs (total, progress) " +
                        "VALUES (:total, :progress) " +
                        "RETURNING id")
                        .bind("total", job.getTotal())
                        .bind("progress", job.getProgress())
                        .mapTo(Integer.class)
                        .one();
        });
    }

    public List<Job> getJobs() {
        return jdbi.withHandle(handle -> {
            return handle.createQuery("SELECT id, total, progress, updated_at FROM demo.jobs")
                    .mapToBean(Job.class)
                    .list();
        });
    }

    public Optional<Job> getJob(long id) {
        return jdbi.withHandle(handle -> {
            return handle.select("SELECT id, total, progress, updated_at FROM demo.jobs WHERE id IN (:id)")
                    .bind("id", id)
                    .mapToBean(Job.class)
                    .findFirst();
        });
    }

    public void updateJob(Job job) {
        jdbi.useHandle(handle -> {
            handle.createUpdate("UPDATE demo.jobs " +
                    "SET total = :total, " +
                    "progress = :progress, " +
                    "updated_at = now() " +
                    "WHERE id IN (:id)")
                    .bind("total", job.getTotal())
                    .bind("progress", job.getProgress())
                    .bind("id", job.getId())
                    .execute();
        });
    }

    public void deleteJobById(int id) {
        jdbi.useHandle(handle -> {
            handle.execute("DELETE FROM demo.jobs " +
                    "WHERE id IN (?)", id);
        });
    }
}
