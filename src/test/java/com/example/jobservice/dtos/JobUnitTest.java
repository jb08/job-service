package com.example.jobservice.dtos;


import static com.example.jobservice.utils.TestUtils.buildJob;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.ws.rs.WebApplicationException;

import org.junit.jupiter.api.Test;

import com.github.javafaker.Faker;

class JobUnitTest {

    private static Faker faker =  new Faker();

    @Test
    void validate_negativeTotal_Throws() {
        Job job = buildJob();
        job.setTotal(faker.number().numberBetween(-1000, 0));

        Exception e = assertThrows(WebApplicationException.class, job::validate);
        assertEquals(String.format("Job total must be non-negative: %s.", job.getTotal()), e.getMessage());
    }
}
