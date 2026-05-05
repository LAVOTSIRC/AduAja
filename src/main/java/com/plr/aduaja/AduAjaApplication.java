package com.plr.aduaja;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AduAjaApplication {

    public static void main(String[] args) {
        SpringApplication.run(AduAjaApplication.class, args);
    }

}
