package ru.nsu.ccfit.crackhash.manager;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
public class ManagerController {
    @GetMapping("/api/hash/crack")
    public String get() {
        WebClient client = WebClient.create("http://nginx:8001");

        Mono<ResponseEntity<String>> response = client.get()
                .uri("/internal/api/worker/hash/crack/task")
                .retrieve()
                .toEntity(String.class);

        var result = response.block().getBody();
        System.out.println(result);
        return result;
    }
}
