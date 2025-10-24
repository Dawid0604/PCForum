package pl.dawid0604.pcforum.thread.service.commons.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(final String message) {
        super(message);
    }
}
