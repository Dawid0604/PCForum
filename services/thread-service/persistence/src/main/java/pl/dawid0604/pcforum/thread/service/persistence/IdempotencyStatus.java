package pl.dawid0604.pcforum.thread.service.persistence;

public enum IdempotencyStatus {
    PROCESSING,
    SUCCESS,
    FAILED
}
