package pl.dawid0604.pcforum.thread.service.commons.exception;

import java.io.Serial;

/**
 * <p>
 *     Exception which is thrown when given resource not found.
 * </p>
 */
public class ResourceNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructor referencing to parent constructor with
     * {@code String message} argument.
     * @param message exception message
     */
    public ResourceNotFoundException(final String message) {
        super(message);
    }
}
