package com.example.InformasiMesin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.example") // ⬅️ Scan semua paket com.example
@EnableJpaRepositories(basePackages = "com.example.serverapp.repository")
@EntityScan(basePackages = "com.example.serverapp.model")
public class InformasiMesinApplication {
    public static void main(String[] args) {
        SpringApplication.run(InformasiMesinApplication.class, args);
        System.out.println("\n Server FE is running... \n");
    }
}
