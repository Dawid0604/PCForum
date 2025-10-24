package pl.dawid0604.pcforum.thread.service.commons;

/**
 * Utility class to store common things.
 */
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

    public static final String TIME_LIMITER_FAST_KEY = "thread-service-fast";
    public static final String TIME_LIMITER_MODERATE_KEY = "thread-service-moderate";
    public static final String TIME_LIMITER_LONG_KEY = "thread-service-long";

    public static final String TITLE_NOT_NULL_MESSAGE = "Title cannot be null";
    public static final String TITLE_NOT_BLANK_MESSAGE = "Title cannot be blank";
    public static final int TITLE_MIN_SIZE = 6;
    public static final int TITLE_MAX_SIZE = 128;
    public static final String TITLE_SIZE_MESSAGE =
            "Name must be greater than " + TITLE_MIN_SIZE + " characters and lower than " + TITLE_MAX_SIZE + " characters";

    public static final String TITLE_SCHEMA_DESCRIPTION = "Thread title";
    public static final String TITLE_SCHEMA_EXAMPLE = "Intel or AMD?";

    public static final String CONTENT_NOT_NULL_MESSAGE = "Content cannot be null";
    public static final String CONTENT_NOT_BLANK_MESSAGE = "Content cannot be blank";

    public static final int CONTENT_MIN_SIZE = 6;
    public static final String CONTENT_SIZE_MESSAGE = "Name must be greater than " + TITLE_MIN_SIZE + " characters";

    public static final String CONTENT_SCHEMA_DESCRIPTION = "Thread content";
    public static final String CONTENT_SCHEMA_EXAMPLE = "I wonder about AMD Ryzen 5 5700X3D";

    public static final String NANO_ID_REGEXP = "^[A-Za-z0-9_-]{21}$";
    public static final String NANO_ID_MESSAGE = "Value must be a valid NanoId and contains 21 characters";
    public static final String SCHEMA_NANO_ID_EXAMPLE = "Epvc81_boiyhn5_d5dfXp";
    public static final String SCHEMA_NANO_ID_DESCRIPTION = "Unique identifier of the thread (NanoId format)";

    public static final String NEGATIVE_VALUE_MESSAGE = "Value cannot be negative";

    public static final String KAFKA_THREAD_CREATED_TOPIC = "threads.new-thread.v1";
    public static final String KAFKA_THREAD_STATUS_CHANGED_TOPIC = "threads.status-changed.v1";

    public static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    /**
     * The default length of generated NanoId.
     */
    public static final int NANO_ID_LENGTH = 21;

    /**
     * Creating class instance is prohibited.
     */
    private Constants() { }
}
