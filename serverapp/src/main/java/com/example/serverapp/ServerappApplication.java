package com.example.serverapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ServerappApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServerappApplication.class, args);
        System.out.println("\n Server is running... \n");
    }
}
