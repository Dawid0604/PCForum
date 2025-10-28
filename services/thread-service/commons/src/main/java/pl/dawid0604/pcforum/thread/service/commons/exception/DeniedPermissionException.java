package pl.dawid0604.pcforum.thread.service.commons.exception;

import java.io.Serial;

/**
 * <p>
 *     Exception which is thrown when requirement of {@code RequirePermission}
 *     annotation is not met.
 * </p>
 */
public class DeniedPermissionException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructor referencing to parent constructor with
     * {@code String message} argument.
     * @param message exception message
     */
    public DeniedPermissionException(final String message) {
        super(message);
    }
}
