package com.example.jobservice.dtos;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.example.jobservice.exceptions.BadRequest;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Job {

    int id;
    long total;
    long progress;
    @JsonProperty("updated_at")
    Timestamp updatedAt;

    /**
     * Create a job. Note, progress is set to zero on job creation, and id is system-generated.
     *
     * @param total the Job total
     */
    public Job(long total) {
        this.total = total;
    }

    public void validate() {
        if (total < 0) throw new BadRequest(String.format("Job total must be non-negative: %s.", total));
    }
}

