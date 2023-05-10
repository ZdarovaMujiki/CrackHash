package ru.nsu.ccfit.crackhash.worker;

import generated.Request;
import generated.Response;
import org.apache.commons.codec.digest.DigestUtils;
import org.paukov.combinatorics.ICombinatoricsVector;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.paukov.combinatorics.CombinatoricsFactory.createPermutationWithRepetitionGenerator;
import static org.paukov.combinatorics.CombinatoricsFactory.createVector;

@RestController
public class WorkerController {
    private final String[] alphabet = "abcdefghijklmnopqrstuvwxyz0123456789".split("");
    private final ICombinatoricsVector<String> vector = createVector(alphabet);

    @Autowired
    private AmqpTemplate template;

    @RabbitListener(queues = "requestQueue")
    public void task(@RequestBody Request request) {
        int partCount = request.getPartCount();
        int partNumber = request.getPartNumber();
        String targetHash = request.getHash();
        int length = request.getMaxlength();

        Response response = new Response();
        response.setId(request.getTaskId());

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

        template.convertAndSend("responseQueue", response, message -> {
            message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            return message;
        });
    }
}
