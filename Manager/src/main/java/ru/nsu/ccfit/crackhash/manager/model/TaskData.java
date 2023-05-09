package ru.nsu.ccfit.crackhash.manager.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import ru.nsu.ccfit.crackhash.manager.Constants;

@Getter
@Setter
public class TaskData {
    @Id
    @JsonIgnore
    private String id;
    @JsonIgnore
    private int responseAmount;
    private Status status = Status.IN_PROGRESS;
    private String data;

    public void incrementResponseAmount() {
        responseAmount++;
        if (responseAmount == Constants.WORKERS_AMOUNT) {
            status = Status.READY;
        }
    }

    public void setData(String data) {
        if (data == null) {
            return;
        }
        this.data = data;
        status = Status.READY;
    }

    @ToString
    public
    enum Status {
        READY,
        IN_PROGRESS,
        ERROR
    }
}
