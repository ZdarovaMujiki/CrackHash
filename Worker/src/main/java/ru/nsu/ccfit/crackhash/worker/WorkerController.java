package ru.nsu.ccfit.crackhash.worker;

import generated.Request;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WorkerController {

    @PostMapping("/internal/api/worker/hash/crack/task")
    public Integer task(@RequestBody Request request) {
        System.out.println(request.getMaxlength());
        System.out.println(request.getPartCount());
        System.out.println(request.getPartNumber());
        return request.getPartNumber();
    }
}
