package com.example.firstbotapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FirstBotAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(FirstBotAppApplication.class, args);
    }

}
