package pl.dawid0604.pcforum.thread.service.commons.exception;

import java.io.Serial;

/**
 * <p>
 *     Exception which is thrown when for the given Idempotency key
 *     operation is still being processed. Only one operation should be
 *     processed at a time.
 * </p>
 */
public class IdempotencyProcessingException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * <p>
     *     Default exception message.
     * </p>
     */
    private static final String DEFAULT_MESSAGE;

    static {
        DEFAULT_MESSAGE = "Operation is still being processed. Please wait and retry";
    }

    /**
     * <p>
     *     Default constructor referencing to parent constructor.
     * </p>
     */
    public IdempotencyProcessingException() {
        super(DEFAULT_MESSAGE);
    }
}
