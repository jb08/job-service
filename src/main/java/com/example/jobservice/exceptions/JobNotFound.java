package com.example.jobservice.exceptions;

import javax.ws.rs.WebApplicationException;

public class JobNotFound extends WebApplicationException {

    public JobNotFound(long id) {
       super(String.format("Job not found with id: %s.", id), 404);
    }
}

