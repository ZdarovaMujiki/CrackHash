package ru.nsu.ccfit.crackhash.worker;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WorkerController {

    @GetMapping("/internal/api/worker/hash/crack/task")
    public String subj() {
        System.out.println("test");
        return "test";
    }
}
