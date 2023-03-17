package ru.nsu.ccfit.crackhash.manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ManagerApplication {

    public static void main(String[] args) {
        System.out.println("Manager started");
        SpringApplication.run(ManagerApplication.class, args);
    }
}