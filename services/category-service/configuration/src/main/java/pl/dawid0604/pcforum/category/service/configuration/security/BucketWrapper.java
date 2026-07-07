package pl.dawid0604.pcforum.category.service.configuration.security;


import io.github.bucket4j.Bucket;
import io.github.bucket4j.EstimationProbe;
import lombok.Getter;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

import static lombok.AccessLevel.NONE;

/**
 * <p>
 * Wrapper class combining rate limit bucket with additional
 * {@link #lastUsageDate} field to tracking for cleanup operations.
 * </p>
 *
 * @see Bucket
 */
@Getter
final class BucketWrapper {

    /**
     * <p>
     * Identifier (user ID or IP address) for bucket.
     * </p>
     */
    private final String key;

    /**
     * <p>
     * Bucket token for rate limiting.
     * </p>
     */
    @Getter(NONE)
    private final Bucket bucket;

    /**
     * <p>
     * Bucket type determining rate limit configuration
     * and behavior.
     * </p>
     */
    private final BucketType type;

    /**
     * <p>
     * Thread-safe timestamp tracking for automatic cleanup
     * operations.
     * </p>
     *
     * @see AtomicReference
     */
    private final AtomicReference<LocalDateTime> lastUsageDate;

    /**
     * <p>
     * Constructs bucket wrapper with specified configuration.
     * </p>
     *
     * @param incomingKey    client identifier for bucket tracking
     * @param incomingType   bucket type determining rate limit behavior
     * @param incomingBucket bucket for token management
     */
    BucketWrapper(
            @NonNull final String incomingKey,

            @NonNull final BucketType incomingType,

            @NonNull final Bucket incomingBucket) {

        this.key = incomingKey;
        this.type = incomingType;
        this.bucket = incomingBucket;
        this.lastUsageDate = new AtomicReference<>(LocalDateTime.now());
    }

    /**
     * <p>
     * Updates last usage timestamp for cleanup tracking.
     * This is <b>thread-save</b> operation.
     * </p>
     */
    void updateLastUsageDate() {
        this.lastUsageDate.set(LocalDateTime.now());
    }

    public boolean tryConsume() {
        return bucket.tryConsume(1);
    }

    public EstimationProbe estimateAbilityToConsume() {
       return bucket.estimateAbilityToConsume(1);
    }

    public long getAvailableTokens() {
        return bucket.getAvailableTokens();
    }
}
