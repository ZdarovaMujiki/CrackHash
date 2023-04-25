package ru.nsu.ccfit.crackhash.worker;

import generated.Request;
import org.apache.commons.codec.digest.DigestUtils;
import org.paukov.combinatorics.ICombinatoricsVector;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.paukov.combinatorics.CombinatoricsFactory.createPermutationWithRepetitionGenerator;
import static org.paukov.combinatorics.CombinatoricsFactory.createVector;

@RestController
public class WorkerController {
    private final String[] alphabet = "abcdefghijklmnopqrstuvwxyz".split("");
    private final ICombinatoricsVector<String> vector = createVector(alphabet);

    @PostMapping("/internal/api/worker/hash/crack/task")
    public String task(@RequestBody Request request) {
        int partCount = request.getPartCount();
        int partNumber = request.getPartNumber();
        String targetHash = request.getHash();
        int length = request.getMaxlength();

        long totalWordsAmount = (long) Math.pow(alphabet.length, length);
        long wordsAmount = totalWordsAmount / partCount;
        long wordsRemain = totalWordsAmount % partCount;

        long start = wordsAmount * partNumber;
        long end = start + wordsAmount;

        if (partNumber < wordsRemain) {
            start += partNumber;
            end += partNumber + 1;
        } else {
            start += wordsRemain;
            end += wordsRemain;
        }

        var generator = createPermutationWithRepetitionGenerator(vector, length);
        long index = 0;
        for (var letters : generator) {
            if (index >= start) {
                String word = String.join("", letters.getVector());
                String hash = DigestUtils.md5Hex(word);

                if (hash.equals(targetHash)) return word;
            }

            index++;
            if (index == end) break;
        }

        return null;
    }
}
