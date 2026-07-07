package pl.dawid0604.pcforum.category.service.commons;

/**
 * Utility class to store common things.
 */
public final class Constants {

    /**
     * The cache manager key.
     */
    public static final String CACHE_KEY = "category-cache";

    /**
     * <p>The service name mainly used in application.yml file.</p>
     *
     * <strong>Example of usages:</strong>
     * <ul>
     *     <li>
     *         {@code resilience4j.circuitbreaker.instances}.category-service
     *     </li>
     *
     *     <li>
     *         {@code resilience4j.timelimiter.instances}.category-service
     *     </li>
     *
     *     <li>
     *         {@code resilience4j.retry.instances}.category-service
     *     </li>
     * </ul>
     */
    public static final String SERVICE_NAME = "category-service";

    /**
     * The default length of generated NanoId.
     */
    public static final int NANO_ID_LENGTH = 21;

    /**
     * Creating class instance is prohibited.
     */
    private Constants() { }
}
