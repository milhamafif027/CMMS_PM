package com.example.serverapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ServerappApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServerappApplication.class, args);
        System.out.println("\n Server BE is running... \n");
    }
    
}
