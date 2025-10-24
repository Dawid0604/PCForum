package pl.dawid0604.pcforum.thread.service.commons.exception;

public class IdempotencyProcessingException extends RuntimeException {

    public IdempotencyProcessingException() {
        super("Operation is still being processed. Please wait and retry");
    }
}
