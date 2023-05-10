package ru.nsu.ccfit.crackhash.manager;

import generated.Request;
import generated.Response;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import ru.nsu.ccfit.crackhash.manager.model.HashRequest;
import ru.nsu.ccfit.crackhash.manager.model.TaskData;
import ru.nsu.ccfit.crackhash.manager.repository.RequestRepository;
import ru.nsu.ccfit.crackhash.manager.repository.TaskRepository;

import java.util.List;
import java.util.Optional;

@RestController
public class ManagerController {
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private RequestRepository requestRepository;
    @Autowired
    private AmqpTemplate template;

    @PostMapping("/api/hash/crack")
    public String crack(@RequestBody HashRequest hashRequest) {
        var taskData = taskRepository.save(new TaskData());
        var taskId = taskData.getId();

        int partCount = Constants.WORKERS_AMOUNT;
        for (int i = 0; i < partCount; i++) {
            Request request = new Request();
            request.setTaskId(taskId);
            request.setHash(hashRequest.getHash());
            request.setMaxlength(hashRequest.getMaxLength());
            request.setPartCount(partCount);
            request.setPartNumber(i);

            try {
                template.convertAndSend("requestQueue", request, message -> {
                    message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    return message;
                });
            } catch (Exception e) {
                requestRepository.save(request);
            }
        }

        return taskId;
    }

    @Scheduled(fixedDelay = 5000)
    public void processRequests() throws AmqpException {
        List<Request> requests = requestRepository.findAll();
        for (Request request : requests) {
            System.out.println(request.getTaskId());
            template.convertAndSend("requestQueue", request);
            requestRepository.delete(request);
        }
    }

    @GetMapping("/api/hash/status")
    public TaskData status(@RequestParam String requestId) {
        return taskRepository.findById(requestId).orElse(null);
    }

    @RabbitListener(queues = "responseQueue")
    public void request(Response response) {
        Optional<TaskData> optionalTaskData = taskRepository.findById(response.getId());
        if (optionalTaskData.isPresent()) {
            TaskData taskData = optionalTaskData.get();

            taskData.setResponse(response.getPartNumber());
            taskData.setData(response.getData());
            taskRepository.save(taskData);
        }
    }
}
