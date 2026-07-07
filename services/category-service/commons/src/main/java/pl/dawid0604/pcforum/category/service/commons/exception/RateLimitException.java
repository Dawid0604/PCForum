package pl.dawid0604.pcforum.category.service.commons.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;

import java.io.Serial;

/**
 * <p>
 *     Custom Rate Limiter exception.
 * </p>
 */
@Getter
@RequiredArgsConstructor
public class RateLimitException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Limit header to append into {@code ProblemDetails}
     * in the {@code RestControllerAdvice} bean.
     */
    private final Pair<String, String> limitHeader;

    /**
     * Limit remaining header to append into {@code ProblemDetails}
     * in the {@code RestControllerAdvice} bean.
     */
    private final Pair<String, String> limitRemainingHeader;

    /**
     * Limit reset header to append into {@code ProblemDetails}
     * in the {@code RestControllerAdvice} bean.
     */
    private final Pair<String, String> limitResetHeader;

    /**
     * Retry after header to append into {@code ProblemDetails}
     * in the {@code RestControllerAdvice} bean.
     */
    private final Pair<String, String> retryAfterHeader;
}
