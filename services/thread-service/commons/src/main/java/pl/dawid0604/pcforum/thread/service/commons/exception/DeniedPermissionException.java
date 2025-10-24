package pl.dawid0604.pcforum.thread.service.commons.exception;

public class DeniedPermissionException extends RuntimeException {

    public DeniedPermissionException(final String message) {
        super(message);
    }
}
