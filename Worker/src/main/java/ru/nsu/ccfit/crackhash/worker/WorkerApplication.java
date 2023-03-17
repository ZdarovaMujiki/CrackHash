package ru.nsu.ccfit.crackhash.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WorkerApplication {
    public static void main(String[] args) {
        System.out.println("Worker started");
        SpringApplication.run(WorkerApplication.class, args);
    }
}
