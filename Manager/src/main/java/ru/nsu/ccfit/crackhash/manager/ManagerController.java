package ru.nsu.ccfit.crackhash.manager;

import generated.Request;
import generated.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.nsu.ccfit.crackhash.manager.model.HashRequest;
import ru.nsu.ccfit.crackhash.manager.model.TaskData;
import ru.nsu.ccfit.crackhash.manager.repository.TaskRepository;

import java.time.Duration;
import java.util.Optional;

@RestController
public class ManagerController {
    private final WebClient client = WebClient.create(Constants.PROXY_URL);

    @Autowired
    private TaskRepository taskRepository;

    @PostMapping("/api/hash/crack")
    public String crack(@RequestBody HashRequest hashRequest) {
        var taskData = taskRepository.save(new TaskData());
        var id = taskData.getId();

        int partCount = Constants.WORKERS_AMOUNT;
        for (int i = 0; i < partCount; i++) {
            Request request = new Request();
            request.setId(id);
            request.setHash(hashRequest.getHash());
            request.setMaxlength(hashRequest.getMaxLength());
            request.setPartCount(partCount);
            request.setPartNumber(i);

            client.post()
                    .uri("/internal/api/worker/hash/crack/task")
                    .contentType(MediaType.TEXT_XML)
                    .body(Mono.just(request), Request.class)
                    .retrieve()
                    .bodyToFlux(String.class)
                    .timeout(Duration.ofMillis(Constants.TIMEOUT_MS))
                    .publishOn(Schedulers.boundedElastic())
                    .doOnError(throwable -> {
                        taskData.setStatus(TaskData.Status.ERROR);
                        taskRepository.save(taskData);
                    })
                    .onErrorComplete()
                    .subscribe();
        }

        return id;
    }

    @GetMapping("/api/hash/status")
    public TaskData status(@RequestParam String requestId) {
        return taskRepository.findById(requestId).orElse(null);
    }

    @PostMapping("/internal/api/manager/hash/crack/request")
    public void request(@RequestBody Response response) {
        Optional<TaskData> optionalTaskData = taskRepository.findById(response.getId());
        if (optionalTaskData.isPresent()) {
            TaskData taskData = optionalTaskData.get();

            taskData.incrementResponseAmount();
            taskData.setData(response.getData());
            taskRepository.save(taskData);
        }
    }
}
