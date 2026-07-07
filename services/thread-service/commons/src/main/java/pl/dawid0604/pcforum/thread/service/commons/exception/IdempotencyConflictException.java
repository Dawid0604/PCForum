package pl.dawid0604.pcforum.thread.service.commons.exception;

import java.io.Serial;

/**
 * <p>
 *     Exception which is thrown when the given Idempotency key was
 *     already user with different request parameters.
 * </p>
 */
public class IdempotencyConflictException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * <p>
     *     Default exception message.
     * </p>
     */
    private static final String DEFAULT_MESSAGE;

    static {
        DEFAULT_MESSAGE = "The idempotency key was already used with different request parameters";
    }

    /**
     * <p>
     *     Default constructor referencing to parent constructor.
     * </p>
     */
    public IdempotencyConflictException() {
        super(DEFAULT_MESSAGE);
    }
}
