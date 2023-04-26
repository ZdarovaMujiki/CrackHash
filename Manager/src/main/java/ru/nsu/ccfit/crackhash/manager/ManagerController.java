package ru.nsu.ccfit.crackhash.manager;

import generated.Request;
import generated.Response;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.nsu.ccfit.crackhash.manager.model.HashRequest;
import ru.nsu.ccfit.crackhash.manager.model.TaskData;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@RestController
public class ManagerController {
    private final WebClient client = WebClient.create(Constants.PROXY_URL);
    private final ConcurrentMap<String, TaskData> tasks = new ConcurrentHashMap<>();

    @PostMapping("/api/hash/crack")
    public String crack(@RequestBody HashRequest hashRequest) {
        String uuid = UUID.randomUUID().toString();
        tasks.put(uuid, new TaskData());

        int partCount = Constants.WORKERS_AMOUNT;
        for (int i = 0; i < partCount; i++) {
            Request request = new Request();
            request.setId(uuid);
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
                    .doOnError(throwable -> tasks.get(uuid).setStatus(TaskData.Status.ERROR))
                    .onErrorComplete()
                    .subscribe();
        }

        return uuid;
    }

    @GetMapping("/api/hash/status")
    public TaskData status(@RequestParam String requestId) {
        return tasks.get(requestId);
    }

    @PostMapping("/internal/api/manager/hash/crack/request")
    public void request(@RequestBody Response response) {
        TaskData taskData = tasks.get(response.getId());

        taskData.incrementResponseAmount();
        taskData.setData(response.getData());
    }
}
