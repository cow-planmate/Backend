package com.example.planmate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
        scanBasePackages = {
                "com.example.planmate",
                "com.sharedsync.framework"
        }
)
public class PlanMateApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlanMateApplication.class, args);
    }
}
