package ru.nsu.ccfit.crackhash.manager;

public class Constants {
    public static final int WORKERS_AMOUNT = Integer.parseInt(System.getenv("WORKERS_AMOUNT"));
    public static final String PROXY_URL = System.getenv("PROXY_URL");
    public static final int TIMEOUT_MS = 150000;
}
