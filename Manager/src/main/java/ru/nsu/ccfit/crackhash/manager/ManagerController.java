package ru.nsu.ccfit.crackhash.manager;

import generated.Request;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.nsu.ccfit.crackhash.manager.DTO.HashRequest;

import java.time.Duration;
import java.util.UUID;

@RestController
public class ManagerController {

    WebClient client = WebClient.create("http://nginx:8001");

    @PostMapping("/api/hash/crack")
    public String crack(@RequestBody HashRequest hashRequest) {
        String uuid = UUID.randomUUID().toString();

        int partCount = Integer.parseInt(System.getenv("WORKERS_AMOUNT"));

        for (int i = 0; i < partCount; i++) {
            Request request = new Request();
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
                    .timeout(Duration.ofMillis(60000))
                    .subscribe(System.out::println);
        }

        return uuid;
    }
}
