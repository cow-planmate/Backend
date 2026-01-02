package com.example.planmate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.example.planmate", "sharedsync", "com.sharedsync.shared"})
public class PlanMateApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlanMateApplication.class, args);
    }
}
