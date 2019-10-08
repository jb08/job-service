package com.example.jobservice.controllers;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import com.example.jobservice.dtos.Job;
import com.example.jobservice.exceptions.BadRequest;
import com.example.jobservice.exceptions.JobNotFound;
import com.example.jobservice.services.JobService;

@Path("/jobs")
@Api("/jobs")
@Produces(MediaType.APPLICATION_JSON)
public class JobController {

    private JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GET
    @Path("/")
    @ApiOperation(value = "Get all Jobs")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully retrieved Jobs") })
    public List<Job> getJobs() {
        return jobService.getJobs();
    }

    @GET
    @Path("/{id}")
    @ApiOperation(value = "Get one Job")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully retrieved a Job") })
    public Response getJobById(@PathParam("id") int id) {
        Optional<Job> optJob = jobService.getJobById(id);
        if (optJob.isPresent()) {
            return Response.ok(optJob.get()).build();
        } else {
            throw new JobNotFound(id);
        }
    }

    @PUT
    @Path("/{id}")
    @ApiOperation(value = "Update a Job")
    @ApiResponses(value = { @ApiResponse(code = 204, message = "Successfully updated a Job") })
    public Response updateJobById(@PathParam("id") int id, Job job) {
        job.validate();
        if (id != job.getId()) {
            throw new BadRequest("Id and Job id do not match.");
        }
        jobService.updateJob(job);
        return Response.noContent().build();
    }

    @POST
    @Path("/")
    @ApiOperation(value = "Create a Job")
    @ApiResponses(value = { @ApiResponse(code = 201, message = "Successfully created a Job") })
    public Response postJob(Job job) {
        job.validate();
        job = new Job(job.getTotal());
        int id = jobService.createJob(job);
        return Response
                .created(URI.create(String.format("/jobs/%s", id)))
                .build();
    }

    @PUT
    @Path("/{id}/progress/{add}")
    @ApiOperation(value = "Add to a Job's progress")
    @ApiResponses(value = { @ApiResponse(code = 204, message = "Successfully updated a Job") })
    public Response updateJobProgress(@PathParam("id") int id, @PathParam("add") long add) {
        Optional<Job> optJob = jobService.getJobById(id);
        if (!optJob.isPresent()) {
            throw new JobNotFound(id);
        } else {
            Job job = optJob.get();
            job.setProgress(job.getProgress() + add);
            jobService.updateJob(job);
            return Response.noContent().build();
        }
    }

}
