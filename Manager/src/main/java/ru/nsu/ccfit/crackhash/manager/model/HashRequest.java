package ru.nsu.ccfit.crackhash.manager.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HashRequest {
    private String hash;
    private int maxLength;
}
