package com.example.jobservice.utils;

import com.example.jobservice.dtos.Job;
import com.github.javafaker.Faker;

public class TestUtils {

    private static Faker faker =  new Faker();

    public static Job buildJob() {
        return Job.builder()
                .id(faker.number().randomDigit())
                .total(faker.number().numberBetween(0, 1000))
                .progress(faker.number().randomNumber())
                .build();
    }
}
