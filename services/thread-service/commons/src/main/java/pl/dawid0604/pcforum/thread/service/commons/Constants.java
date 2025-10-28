package pl.dawid0604.pcforum.thread.service.commons;

import lombok.experimental.UtilityClass;

/**
 * Utility class to store common things.
 */
@UtilityClass
@SuppressWarnings({
        "PMD.LongVariable",
        "PMD.DataClass"
})
public final class Constants {

    /**
     * Single threads.
     */
    public static final String CACHE_SINGLE_THREADS_KEY = "single-thread";

    /**
     * Lists of threads.
     */
    public static final String CACHE_LIST_OF_THREADS_KEY = "threads-list";

    /**
     * <p>The service name mainly used in application.yml file.</p>
     *
     * <strong>Example of usages:</strong>
     * <ul>
     *     <li>
     *         {@code resilience4j.circuitbreaker.instances}.thread-service
     *     </li>
     *
     *     <li>
     *         {@code resilience4j.timelimiter.instances}.thread-service
     *     </li>
     *
     *     <li>
     *         {@code resilience4j.retry.instances}.thread-service
     *     </li>
     * </ul>
     */
    public static final String SERVICE_NAME = "thread-service";

    /**
     * <p>
     *     Key for Time Limiter implementation. The 'fast' means the
     *     given operation should be fast. The implementation can be
     *     found under application.yml.
     * </p>
     */
    public static final String TIME_LIMITER_FAST_KEY = "thread-service-fast";

    /**
     * <p>
     *     Key for Time Limiter implementation. The 'fast' means the
     *     given operation should be moderate fast. The implementation can be
     *     found under application.yml.
     * </p>
     */
    public static final String TIME_LIMITER_MODERATE_KEY = "thread-service-moderate";

    /**
     * <p>
     *     Validation message for {@code title} field when value is null.
     * </p>
     */
    public static final String TITLE_NOT_NULL_MESSAGE = "Title cannot be null";

    /**
     * <p>
     *     Validation message for {@code title} field when is blank.
     * </p>
     */
    public static final String TITLE_NOT_BLANK_MESSAGE = "Title cannot be blank";

    /**
     * <p>
     *     Validation message for {@code title} field when min length condition
     *     is not met.
     * </p>
     */
    public static final int TITLE_MIN_LENGTH = 6;

    /**
     * <p>
     *     Validation message for {@code title} field when max length condition
     *     is not met.
     * </p>
     */
    public static final int TITLE_MAX_LENGTH = 128;

    /**
     * <p>
     *     Validation message for {@code title} field when min and max
     *     length condition is not met.
     * </p>
     */
    public static final String TITLE_SIZE_MESSAGE =
            "Name must be greater than "  + TITLE_MIN_LENGTH
            + " characters and lower than " + TITLE_MAX_LENGTH
            + " characters";

    /**
     * <p>
     *     Swagger schema description for {@code title} field.
     * </p>
     */
    public static final String TITLE_SCHEMA_DESCRIPTION = "Thread title";

    /**
     * <p>
     *     Swagger schema example value for {@code title} field.
     * </p>
     */
    public static final String TITLE_SCHEMA_EXAMPLE = "Intel or AMD?";

    /**
     * <p>
     *     Validation message for {@code content} field when value is null.
     * </p>
     */
    public static final String CONTENT_NOT_NULL_MESSAGE = "Content cannot be null";

    /**
     * <p>
     *     Validation message for {@code content} field when value is blank.
     * </p>
     */
    public static final String CONTENT_NOT_BLANK_MESSAGE = "Content cannot be blank";

    /**
     * <p>
     *     Validation message for {@code content} field when min length condition
     *     is not met.
     * </p>
     */
    public static final int CONTENT_MIN_LENGTH = 6;

    /**
     * <p>
     *     Validation message for {@code content} field when min
     *     length condition is not met.
     * </p>
     */
    public static final String CONTENT_SIZE_MESSAGE =
            "Name must be greater than " + TITLE_MIN_LENGTH + " characters";

    /**
     * <p>
     *     Swagger schema description for {@code content} field.
     * </p>
     */
    public static final String CONTENT_SCHEMA_DESCRIPTION = "Thread content";

    /**
     * <p>
     *     Swagger schema example value for {@code content} field.
     * </p>
     */
    public static final String CONTENT_SCHEMA_EXAMPLE = "I wonder about AMD Ryzen 5 5700X3D";

    /**
     * <p>
     *     Regex for NanoId format value.
     * </p>
     */
    public static final String NANO_ID_REGEXP = "^[A-Za-z0-9_-]{21}$";

    /**
     * <p>
     *     Validation message for field when input is different from 21 characters and
     *     is invalid NanoId format.
     * </p>
     */
    public static final String NANO_ID_MESSAGE = "Value must be a valid NanoId and contains 21 characters";

    /**
     * <p>
     *     Swagger schema description for NanoId field.
     * </p>
     */
    public static final String SCHEMA_NANO_ID_DESCRIPTION = "Unique identifier of the thread (NanoId format)";

    /**
     * <p>
     *     Swagger schema example value for NanoId field.
     * </p>
     */
    public static final String SCHEMA_NANO_ID_EXAMPLE = "Epvc81_boiyhn5_d5dfXp";

    /**
     * <p>
     *     Validation message for field when value is negative.
     * </p>
     */
    public static final String NEGATIVE_VALUE_MESSAGE = "Value cannot be negative";

    /**
     * <p>
     *     Kafka topic for created threads.
     * </p>
     */
    public static final String KAFKA_THREAD_CREATED_TOPIC = "threads.new-thread.v1";

    /**
     * <p>
     *     Kafka topic for threads with changed statuses.
     * </p>
     */
    public static final String KAFKA_THREAD_STATUS_CHANGED_TOPIC = "threads.status-changed.v1";

    /**
     * <p>
     *     The idempotency HTTP header required for POST operations.
     * </p>
     */
    public static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    /**
     * The default length of generated NanoId.
     */
    public static final int NANO_ID_LENGTH = 21;

    /**
     * <p>
     *     Common datetime format.
     * </p>
     */
    public static final String DATE_TIME_FORMAT = "dd-MM-yyyy HH:mm";

    /**
     * <p>
     *     Swagger schema description for {@code userId} field.
     * </p>
     */
    public static final String USER_SCHEMA_DESCRIPTION = "Unique identifier of the user (NanoId format)";

    /**
     * <p>
     *     Swagger schema description for {@code isPinned} thread status.
     * </p>
     */
    public static final String IS_PINNED_SCHEMA_DESCRIPTION =
            "Boolean value that indicates that thread has pinned status";

    /**
     * <p>
     *     Swagger schema description for {@code isBanned} thread status.
     * </p>
     */
    public static final String IS_BANNED_SCHEMA_DESCRIPTION =
            "Boolean value that indicates that thread has banned status";

    /**
     * <p>
     *     Swagger schema description for {@code isClosed} thread status.
     * </p>
     */
    public static final String IS_CLOSED_SCHEMA_DESCRIPTION =
            "Boolean value that indicates that thread has closed status";

    /**
     * <p>
     *     Swagger schema example value for boolean field.
     * </p>
     */
    public static final String BOOLEAN_EXAMPLE_SCHEMA_VALUE = "true";

    /**
     * <p>
     *     Swagger schema example value for integer field.
     * </p>
     */
    public static final String NUMBER_EXAMPLE_SCHEMA_VALUE = "123";

    /**
     * <p>
     *     Swagger schema example value for datetime field.
     * </p>
     */
    public static final String DATE_EXAMPLE_SCHEMA_VALUE = "15-01-2021 13:21";

    /**
     * <p>
     *     Swagger schema description for {@code numberOfViews} field.
     * </p>
     */
    public static final String NUMBER_OF_VIEWS_SCHEMA_DESCRIPTION = "Number of thread views";

    /**
     * <p>
     *     Swagger schema example value for {@code createdDate} field.
     * </p>
     */
    public static final String CREATED_DATE_SCHEMA_DESCRIPTION =
            "Thread created date in '" + DATE_TIME_FORMAT + "' format";
}
