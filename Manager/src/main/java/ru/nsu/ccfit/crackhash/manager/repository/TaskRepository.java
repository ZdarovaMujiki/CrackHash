package ru.nsu.ccfit.crackhash.manager.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.nsu.ccfit.crackhash.manager.model.TaskData;

public interface TaskRepository extends MongoRepository<TaskData, String> {
}
