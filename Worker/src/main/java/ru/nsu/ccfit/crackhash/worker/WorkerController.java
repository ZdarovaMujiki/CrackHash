package ru.nsu.ccfit.crackhash.worker;

import generated.Request;
import generated.Response;
import org.apache.commons.codec.digest.DigestUtils;
import org.paukov.combinatorics.ICombinatoricsVector;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.paukov.combinatorics.CombinatoricsFactory.createPermutationWithRepetitionGenerator;
import static org.paukov.combinatorics.CombinatoricsFactory.createVector;

@RestController
public class WorkerController {
    private final String[] alphabet = "abcdefghijklmnopqrstuvwxyz".split("");
    private final ICombinatoricsVector<String> vector = createVector(alphabet);
    private final WebClient client = WebClient.create(System.getenv("MANAGER_URL"));

    @PostMapping("/internal/api/worker/hash/crack/task")
    public void task(@RequestBody Request request) {
        int partCount = request.getPartCount();
        int partNumber = request.getPartNumber();
        String targetHash = request.getHash();
        int length = request.getMaxlength();

        Response response = new Response();
        response.setId(request.getId());

        var generator = createPermutationWithRepetitionGenerator(vector, length);
        var iterator = generator.iterator();
        ICombinatoricsVector<String> letters = iterator.next();
        for (int i = 0; i < partNumber; i++) {
            letters = iterator.next();
        }

        while (true) {
            String word = String.join("", letters.getVector());
            String hash = DigestUtils.md5Hex(word);
            if (hash.equals(targetHash)) {
                response.setData(word);
                break;
            }

            if (!iterator.hasNext()) {
                break;
            }

            for (int i = 0; i < partCount && iterator.hasNext(); i++) {
                letters = iterator.next();
            }
        }

        client.post()
                .uri("/internal/api/manager/hash/crack/request")
                .contentType(MediaType.TEXT_XML)
                .body(Mono.just(response), Response.class)
                .retrieve()
                .bodyToFlux(String.class)
                .subscribe();
    }
}
