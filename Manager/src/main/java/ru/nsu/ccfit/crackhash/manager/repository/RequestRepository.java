package ru.nsu.ccfit.crackhash.manager.repository;

import generated.Request;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RequestRepository extends MongoRepository<Request, String> {
}
