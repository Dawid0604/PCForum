package pl.dawid0604.pcforum.thread.service.configuration.security;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * <p>
 * Enum defining rate limit configurations for different client
 * types.
 * </p>
 */
@Getter
@RequiredArgsConstructor
enum BucketType {

    /**
     * <p>
     * IP-based rate limiting with conservative limits
     * for anonymous requests.
     * </p>
     */
    @SuppressWarnings("PMD.ShortVariable")
    IP(30, 15),

    /**
     * <p>
     * User-based rate limiting with looser limits
     * for authenticated requests.
     * </p>
     */
    USER(60, 30);

    /**
     * <p>
     * Primary rate limit (request per {@link #LIMIT_SECONDS}).
     * </p>
     */
    private final int limit;

    /**
     * <p>
     * Burst rate limit (requests per {@link #BURST_SECONDS}).
     * </p>
     */
    private final int burstLimit;

    /**
     * <p>
     * Indicating number of overall allowed requests.
     * </p>
     */
    public static final int LIMIT_SECONDS = 60;

    /**
     * <p>
     * Indicating number of burst seconds.
     * </p>
     */
    public static final int BURST_SECONDS = 10;
}
