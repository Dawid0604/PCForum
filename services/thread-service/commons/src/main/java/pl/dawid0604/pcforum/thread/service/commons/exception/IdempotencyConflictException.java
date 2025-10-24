package pl.dawid0604.pcforum.thread.service.commons.exception;

public class IdempotencyConflictException extends RuntimeException {

    public IdempotencyConflictException() {
        super("The idempotency key was already used with different request parameters");
    }
}
