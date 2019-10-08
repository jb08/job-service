package com.example.jobservice.exceptions;

import javax.ws.rs.WebApplicationException;

public class BadRequest extends WebApplicationException {

    public BadRequest(String message) {
       super(message, 400);
    }
}

