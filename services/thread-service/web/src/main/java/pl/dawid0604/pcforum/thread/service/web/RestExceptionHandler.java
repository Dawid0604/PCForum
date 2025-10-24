package pl.dawid0604.pcforum.thread.service.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.lang.NonNull;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import pl.dawid0604.pcforum.thread.service.commons.exception.DeniedPermissionException;
import pl.dawid0604.pcforum.thread.service.commons.exception.IdempotencyConflictException;
import pl.dawid0604.pcforum.thread.service.commons.exception.IdempotencyProcessingException;
import pl.dawid0604.pcforum.thread.service.commons.exception.RateLimitException;

import java.net.URI;
import java.time.Instant;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@RestControllerAdvice
@SuppressWarnings("PMD.TooManyMethods")
class RestExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * <p>
     *     Problem type URI for resource not found errors.
     * </p>
     */
    private static final URI RESOURCE_NOT_FOUND_URI;

    /**
     * <p>
     *     Problem type URI for validation errors.
     * </p>
     */
    private static final URI VALIDATION_URI;

    /**
     * <p>
     *     Problem type URI for internal server errors.
     * </p>
     */
    private static final URI ISE_URI;

    /**
     * <p>
     *     Problem type URI for unauthorized errors.
     * </p>
     */
    private static final URI UNAUTHORIZED_URI;

    /**
     * <p>
     *     Problem type URI for forbidden permission errors.
     * </p>
     */
    private static final URI FORBIDDEN_URI;

    /**
     * <p>
     *     Problem type URI for rate limit errors.
     * </p>
     */
    private static final URI RATE_LIMIT_URI;

    /**
     * <p>
     *     Problem type URI for idempotency errors.
     * </p>
     */
    private static final URI IDEMPOTENCY_URI;

    /**
     * <p>
     *     Current service name.
     * </p>
     */
    private final String serviceName;

    /**
     * <p>
     *     Constructs exception handler with service identification.
     * </p>
     *
     * @param incomingServiceName current service name
     */
    RestExceptionHandler(
            @Value("${spring.application.name}")
            final String incomingServiceName) {

        super();
        this.serviceName = incomingServiceName;
    }

    static {
        RESOURCE_NOT_FOUND_URI = URI.create("urn:problem-type:resource-not-found");
        VALIDATION_URI = URI.create("urn:problem-type:validation-error");
        ISE_URI = URI.create("urn:problem-type:server-error");
        UNAUTHORIZED_URI = URI.create("urn:problem-type:unauthorized-error");
        RATE_LIMIT_URI = URI.create("urn:problem-type:rate-limit-error");
        FORBIDDEN_URI = URI.create("urn:problem-type:forbidden-error");
        IDEMPOTENCY_URI = URI.create("urn:problem-type:idempotency-error");
    }

    /**
     * <p>
     *     Handles all unhandled exceptions as interval server error.
     * </p>
     *
     * @param exception to handle
     * @return {@link ProblemDetail} with status {@link HttpStatus#INTERNAL_SERVER_ERROR}
     * and additional information.
     */
    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpectedException(
            @NonNull
            final Exception exception) {

        final ProblemDetail problemDetail = buildProblemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                "Generic"
        );

        problemDetail.setType(ISE_URI);
        problemDetail.setTitle("Internal Server Error");

        logAsError(exception);
        return problemDetail;
    }

    /**
     * <p>
     *     Handles resource not found exception.
     * </p>
     *
     * @param exception to handle
     * @return {@link ProblemDetail} with status {@link HttpStatus#NOT_FOUND}
     * and additional information.
     */
    @ExceptionHandler(NoSuchElementException.class)
    ProblemDetail handleNotFoundResourceException(
            @NonNull
            final NoSuchElementException exception) {

        final ProblemDetail problemDetail = buildProblemDetail(
                HttpStatus.NOT_FOUND,
                "Resource not found",
                "Resource"
        );

        problemDetail.setType(RESOURCE_NOT_FOUND_URI);
        problemDetail.setTitle("Resource not found");

        logAsWarn(exception);
        return problemDetail;
    }

    /**
     * <p>
     *     Handles authorization failures exception.
     * </p>
     *
     * @param exception to handle
     * @return {@link ProblemDetail} with status {@link HttpStatus#FORBIDDEN}
     * and additional information.
     */
    @ExceptionHandler(AuthorizationDeniedException.class)
    ProblemDetail handleUnauthorized(@NonNull final AuthorizationDeniedException exception) {
        final ProblemDetail problemDetail = buildProblemDetail(
                HttpStatus.UNAUTHORIZED,
                "You are unauthorized.",
                "Authorization requried"
        );

        problemDetail.setType(UNAUTHORIZED_URI);
        problemDetail.setTitle("No permissions");
        return problemDetail;
    }

    /**
     * <p>
     *     Handles rate limiting exceptions.
     * </p>
     *
     * <p>
     *     <strong>
     *         Additionally there are some added headers:
     *     </strong>
     *
     *     <ul>
     *         <li>
     *             <b>X-RateLimit-Limit</b>: maximum requests allowed
     *         </li>
     *
     *         <li>
     *             <b>X-RateLimit-Remaining</b>: remaining tokens to use
     *         </li>
     *
     *         <li>
     *             <b>X-RateLimit-Reset</b>: unix timestamp when limit resets
     *         </li>
     *
     *         <li>
     *             <b>Retry-After</b>: seconds to wait before next request
     *         </li>
     *     </ul>
     * </p>
     *
     * @param exception to handle
     * @param request web request context
     * @return {@link ProblemDetail} with status {@link HttpStatus#TOO_MANY_REQUESTS}
     * and additional information.
     */
    @ExceptionHandler(RateLimitException.class)
    ResponseEntity<ProblemDetail> handleRateLimitException(
            @NonNull final RateLimitException exception,
            final HttpServletRequest request) {

        final ProblemDetail problemDetail = buildProblemDetail(
                HttpStatus.TOO_MANY_REQUESTS,
                "Rate limit exceeded",
                "To many requests"
        );

        problemDetail.setType(RATE_LIMIT_URI);
        problemDetail.setTitle("To many requests");
        problemDetail.setProperty("path", request.getRequestURI());
        problemDetail.setProperty("method", request.getMethod());
        problemDetail.setProperty("clientIp", request.getRemoteAddr());

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                             .headers(getHttpHeaders(exception))
                             .body(problemDetail);
    }

    /**
     * <p>
     *     Handles constraint violations that occur during
     *     method parameter and path variable validation in
     *     REST endpoints.
     * </p>
     *
     * <p>
     *     <strong>
     *         This exception handler catches violations from:
     *     </strong>
     *
     *     <ul>
     *         <li>
     *             {@code @PathVariable}
     *         </li>
     *
     *         <li>
     *             {@code @RequestParam}
     *         </li>
     *
     *         <li>
     *             Method-level validation triggered by {@code @Validated}
     *             annotation
     *         </li>
     *     </ul>
     * </p>
     *
     * @param exception to handle
     * @return {@link ProblemDetail} with status {@link HttpStatus#BAD_REQUEST}
     * and additional information
     */
    @ExceptionHandler(ConstraintViolationException.class)
    ProblemDetail handleConstraintViolationException(
            @NonNull final ConstraintViolationException exception) {

        final ProblemDetail problemDetail = buildProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                "No Permissions"
        );

        problemDetail.setType(VALIDATION_URI);
        problemDetail.setTitle("No Permissions");

        logAsWarn(exception);
        return problemDetail;
    }

    @ExceptionHandler(DeniedPermissionException.class)
    ProblemDetail handleDeniedPermissionException(
            @NonNull final DeniedPermissionException exception) {

        final ProblemDetail problemDetail = buildProblemDetail(
                HttpStatus.FORBIDDEN,
                exception.getMessage(),
                "No permission"
        );

        problemDetail.setType(FORBIDDEN_URI);
        problemDetail.setTitle("No permissions");

        logAsWarn(exception);
        return problemDetail;
    }

    @ExceptionHandler(IdempotencyConflictException.class)
    ProblemDetail handleIdempotencyConflictException(
            @NonNull final IdempotencyConflictException exception) {

        final ProblemDetail problemDetail = buildProblemDetail(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                "Idempotency Conflict"
        );

        problemDetail.setType(IDEMPOTENCY_URI);
        problemDetail.setTitle("Idempotency Conflict");

        logAsWarn(exception);
        return problemDetail;
    }

    @ExceptionHandler(IdempotencyProcessingException.class)
    ProblemDetail handleIdempotencyProcessingException(
            @NonNull final IdempotencyProcessingException exception) {

        final ProblemDetail problemDetail = buildProblemDetail(
                HttpStatus.TOO_MANY_REQUESTS,
                exception.getMessage(),
                "Operation In Progress"
        );

        problemDetail.setType(IDEMPOTENCY_URI);
        problemDetail.setTitle("Operation In Progress");

        logAsWarn(exception);
        return problemDetail;
    }

    /**
     * <p>
     *     Extracts rate limiting headers from exception for client response.
     * </p>
     *
     * @param exception to process, cannot be null
     * @return {@link HttpHeaders} object with extracted headers from the {@code exception} parameter
     */
    @SuppressWarnings("PMD.LooseCoupling")
    private static HttpHeaders getHttpHeaders(
            @NotNull
            final RateLimitException exception) {

        final HttpHeaders headers = new HttpHeaders();
                          headers.set(
                                  exception.getLimitHeader().getKey(),
                                  exception.getLimitHeader().getValue()
                          );

                          headers.set(
                                  exception.getLimitRemainingHeader().getKey(),
                                  exception.getLimitRemainingHeader().getValue()
                          );

                          headers.set(
                                  exception.getLimitResetHeader().getKey(),
                                  exception.getLimitResetHeader().getValue()
                          );

                          headers.set(
                                  exception.getRetryAfterHeader().getKey(),
                                  exception.getRetryAfterHeader().getValue()
                          );

        return headers;
    }

    /**
     * <p>
     *     Handles validation failures exception.
     * </p>
     *
     * @param exception to handle
     * @param headers from the original request
     * @param status incoming HTTP status
     * @param request web request context
     * @return {@link ResponseEntity} with status {@link HttpStatus#BAD_REQUEST}
     * and additional information
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(

            @NonNull
            final MethodArgumentNotValidException exception,

            @NonNull
            final HttpHeaders headers,

            @NonNull
            final HttpStatusCode status,

            @NonNull
            final WebRequest request) {

        final ProblemDetail problemDetail = buildProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                "Validation"
        );

        problemDetail.setType(VALIDATION_URI);
        problemDetail.setTitle("Validation error");
        problemDetail.setProperty("violations", getValidationViolations(exception));

        logAsWarn(exception);
        return ResponseEntity.of(problemDetail).build();
    }

    /**
     * <p>
     *     Auxiliary method to log exception with <b>warn</b> log level.
     * </p>
     * @param exception to log
     */
    private void logAsWarn(final Exception exception) {
        log.warn(
                "Exception handled: {} - {}",
                exception.getClass().getSimpleName(),
                exception.getMessage(),
                exception
        );
    }

    /**
     * <p>
     *     Auxiliary method to log exception with <b>error</b> log level.
     * </p>
     * @param exception to log
     */
    private void logAsError(final Exception exception) {
        log.error(
                "Exception handled: {} - {}",
                exception.getClass().getSimpleName(),
                exception.getMessage(),
                exception
        );
    }

    /**
     *
     * <p>
     *     Auxiliary method to build {@link ProblemDetail} object with initial and common service metadata.
     * </p>
     * @param httpStatus HTTP status, cannot be null
     * @param detail human-readable error description, cannot be null
     * @param category error category classification, cannot be null
     * @return configured {@link ProblemDetail} with standard service metadata and prepared to further customizing
     */
    private ProblemDetail buildProblemDetail(
            @NonNull
            final HttpStatus httpStatus,

            @NonNull
            final String detail,

            @NonNull
            final String category) {

        final ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(httpStatus, detail);
                            problemDetail.setProperty("service", serviceName);
                            problemDetail.setProperty("timestamp", Instant.now());
                            problemDetail.setProperty("error_category", category);

        return problemDetail;
    }

    /**
     * <p>
     *     Auxiliary method to extract validation errors into structured and human-readable format.
     * </p>
     *
     * @param exception containing required errors
     * @return {@link List} containing errors with the following format: {@code key: "field name", "error message"}
     */
    private static List<Map<String, String>> getValidationViolations(
            @NonNull
            final MethodArgumentNotValidException exception) {

        return Optional.of(exception)
                       .map(MethodArgumentNotValidException::getBindingResult)
                       .map(BindingResult::getFieldErrors)
                       .stream()
                       .flatMap(Collection::stream)
                       .map(e -> Map.of(
                               "field", e.getField(),
                               "message", isNotBlank(e.getDefaultMessage()) ? e.getDefaultMessage() : "No data"
                       ))
                       .toList();
    }
}
