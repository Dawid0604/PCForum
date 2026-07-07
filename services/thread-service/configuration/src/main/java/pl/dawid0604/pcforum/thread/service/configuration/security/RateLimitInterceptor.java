package pl.dawid0604.pcforum.thread.service.configuration.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.EstimationProbe;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import pl.dawid0604.pcforum.thread.service.commons.RegexUtils;
import pl.dawid0604.pcforum.thread.service.commons.exception.RateLimitException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * <p>
 *     Rate limiting interceptor implementing dual-tier throttling
 *     with automatic buckets cleanup.
 * </p>
 *
 * <p>
 *     This interceptor provides API rate limiting using the
 *     token Bucket algorithm with separate limits for authenticated
 *     users and IP addresses. Interceptor provides client identification,
 *     HTTP header support, and automatic management through scheduled cleanup
 *     of inactive buckets.
 * </p>
 *
 * @see Bucket
 * @see RateLimitException
 * @see HandlerInterceptor
 * @see JwtAuthenticationToken
 */
@Component
@NoArgsConstructor(access = PACKAGE)
class RateLimitInterceptor implements HandlerInterceptor {

    /**
     * <p>
     *     Thread-safe storage for authenticated user rate limit buckets.
     * </p>
     *
     * <ul>
     *     <li>
     *         <b>Key:</b> JWT sub claim (userId)
     *     </li>
     *
     *     <li>
     *         <b>Value:</b> User specific bucket
     *     </li>
     * </ul>
     */
    private final Map<String, BucketWrapper> userBuckets = new ConcurrentHashMap<>();

    /**
     * <p>
     *     Thread-safe storage for IP address rate limit buckets.
     * </p>
     *
     * <ul>
     *     <li>
     *         <b>Key:</b> Client IP address
     *     </li>
     *
     *     <li>
     *         <b>Value:</b> IP specific bucket wrapper
     *     </li>
     * </ul>
     */
    private final Map<String, BucketWrapper> ipBuckets = new ConcurrentHashMap<>();

    /**
     * <p>
     *     Standard HTTP headers for client IP identification.
     * </p>
     */
    private static final List<String> IP_HEADERS;

    /**
     * <p>
     *     Cleanup interval for removing unused buckets (15 minutes in milliseconds).
     * </p>
     *
     * <p>
     *     Prevents memory leaks while maintaining reasonable bucket retention for
     *     active clients. Buckets inactive for 2+ hours are automatically removed
     *     from memory.
     * </p>
     */
    private static final int BUCKET_CLEANUP_TIMEOUT = 900_000;

    static {
        IP_HEADERS = List.of(
                "X-Forwarded-For",
                "X-Real-IP",
                "CF-Connecting-IP"
        );
    }

    /**
     * <p>
     *     Intercepts incoming HTTP headers to enforce rate
     *     limiting before controller execution.
     * </p>
     *
     * <p>
     *     This method implements the core rate limiting logic using
     *     Token Bucket algorithm. Attempts to consume one token from
     *     the appropriate bucket (user or IP-based). On success, update
     *     bucket metadata and adds informational headers. On failure,
     *     throws {@link RateLimitException} with complete header information
     *     for proper HTTP 429 response generation.
     * </p>
     *
     * <p>
     *     <strong>Processing flow:</strong>
     *     <ol>
     *         <li>
     *             Identify client (authenticated user or IP address)
     *         </li>
     *
     *         <li>
     *             Retrieve or create appropriate rate limit bucket
     *         </li>
     *
     *         <li>
     *             Attempt to consume one token from bucket
     *         </li>
     *
     *         <li>
     *             On success: update usage timestamp, add headers and proceed.
     *             On failure: throw exception with rate limit headers
     *         </li>
     *     </ol>
     * </p>
     * @param request incoming HTTP requests to processing
     * @param response HTTP response for adding rate limit headers
     * @param handler target controller method (unused)
     * @throws RateLimitException when rate limit is exceeded
     * @return {@code true} if request should proceed
     * @apiNote This method is automatically invoked by Spring. It
     * should not be invoked manually.
     *
     * @see BucketWrapper
     * @see RateLimitException
     * @see HeaderMapper
     * @see #getBucket(HttpServletRequest)
     * @see #addRateLimitHeaders(HttpServletResponse, BucketWrapper)
     */
    @Override
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public boolean preHandle(
            @NonNull
            final HttpServletRequest request,

            @NonNull
            final HttpServletResponse response,

            @NonNull
            final Object handler) {

        final BucketWrapper bucketWrapper = getBucket(request);

        if (bucketWrapper.tryConsume()) {
            bucketWrapper.updateLastUsageDate();
            addRateLimitHeaders(response, bucketWrapper);

            return true;

        } else {
            throw new RateLimitException(
                    HeaderMapper.getLimitHeader(bucketWrapper),
                    HeaderMapper.getLimitRemainingHeader(bucketWrapper),
                    HeaderMapper.getLimitResetHeader(bucketWrapper),
                    HeaderMapper.getRetryAfterHeader(bucketWrapper)
            );
        }
    }

    /**
     * <p>
     *     Method to add standard rate limiting headers to
     *     successful HTTP responses.
     * </p>
     *
     * @param response HTTP response to modify with rate limit headers
     * @param bucketWrapper containing current limit state
     * @see HeaderMapper
     */
    private static void addRateLimitHeaders(
            @NonNull
            final HttpServletResponse response,

            @NonNull
            final BucketWrapper bucketWrapper) {

        final Pair<String, String> limitHeader = HeaderMapper.getLimitHeader(bucketWrapper);
        final Pair<String, String> limitRemaining = HeaderMapper.getLimitRemainingHeader(bucketWrapper);
        final Pair<String, String> limitReset = HeaderMapper.getLimitResetHeader(bucketWrapper);

        response.setHeader(limitHeader.getKey(), limitHeader.getValue());
        response.setHeader(limitRemaining.getKey(), limitRemaining.getValue());
        response.setHeader(limitReset.getKey(), limitReset.getValue());
    }

    /**
     * <p>
     *     Utility class for mapping bucket state to standard
     *     HTTP rate limiting headers.
     * </p>
     */
    @NoArgsConstructor(access = PRIVATE)
    static final class HeaderMapper {

        /**
         * <p>
         *     Number to conversion milliseconds to seconds for
         *     Unix timestamp format.
         * </p>
         */
        private static final int TIMESTAMP_SECONDS = 1000;

        /**
         * <p>
         *     Creates <b>X-RateLimit-Limit</b> header indicating maximum
         *     requests per time window.
         * </p>
         *
         * @param bucketWrapper containing current limit state
         * @return {@link Pair} containing header name as key and appropriate value.
         * @throws IllegalArgumentException when bucket state cannot be resolved
         * @see #getBucketException(BucketWrapper)
         * @see BucketType#getLimit()
         */
        public static Pair<String, String> getLimitHeader(@NonNull final BucketWrapper bucketWrapper) {
            return Optional.of(bucketWrapper)
                           .map(BucketWrapper::getType)
                           .map(BucketType::getLimit)
                           .map(String::valueOf)
                           .map(l -> Pair.of("X-RateLimit-Limit", l))
                           .orElseThrow(() -> getBucketException(bucketWrapper));
        }

        /**
         * <p>
         *     Creates <b>X-RateLimit-Remaining</b> header indicating available
         *     tokens to use.
         * </p>
         *
         * @param bucketWrapper containing current limit state
         * @return {@link Pair} containing header name as key and appropriate value
         * @throws IllegalArgumentException when bucket state cannot be resolved
         * @see #getBucketException(BucketWrapper)
         * @see Bucket#getAvailableTokens()
         */
        public static Pair<String, String> getLimitRemainingHeader(@NonNull final BucketWrapper bucketWrapper) {
            return Optional.of(bucketWrapper)
                           .map(BucketWrapper::getAvailableTokens)
                           .map(String::valueOf)
                           .map(l -> Pair.of("X-RateLimit-Remaining", l))
                           .orElseThrow(() -> getBucketException(bucketWrapper));
        }

        /**
         * <p>
         *     Creates <b>X-RateLimit-Reset</b> header indicating when
         *     bucket will refill.
         * </p>
         *
         * @param bucketWrapper containing current limit state
         * @return {@link Pair} containing header name as key and appropriate value (Unix timestamp)
         * @throws IllegalArgumentException when bucket state cannot be resolved
         * @see #getBucketException(BucketWrapper)
         */
        public static Pair<String, String> getLimitResetHeader(@NonNull final BucketWrapper bucketWrapper) {
            return Optional.of(bucketWrapper)
                           .map(HeaderMapper::getLimitReset)
                           .map(String::valueOf)
                           .map(l -> Pair.of("X-RateLimit-Reset", l))
                           .orElseThrow(() -> getBucketException(bucketWrapper));
        }

        /**
         * <p>
         *     Creates <b>Retry-After</b> header indicating seconds until
         *     request allowed.
         * </p>
         *
         * <p>
         *     Uses bucket estimation to calculate precise wait time
         *     until token becomes available. Minimum value of 1 second
         *     prevents client flooding with immediate retries.
         * </p>
         *
         * @param bucketWrapper containing current limit state
         * @return {@link Pair} containing header name as key and appropriate value
         * @see #getBucketException(BucketWrapper)
         * @see Bucket#estimateAbilityToConsume(long)
         */
        public static Pair<String, String> getRetryAfterHeader(@NonNull final BucketWrapper bucketWrapper) {
            final EstimationProbe probe = bucketWrapper.estimateAbilityToConsume();
            final long nanosToWait = probe.getNanosToWaitForRefill();
            final long retryAfter = Math.max(1, TimeUnit.NANOSECONDS.toSeconds(nanosToWait));
            return Pair.of("Retry-After", String.valueOf(retryAfter));
        }

        /**
         * <p>
         *     Calculates Unix timestamp when bucket will have tokens available.
         * </p>
         *
         * @param bucketWrapper containing current limit state
         * @return Unix timestamp in seconds when buckets resets
         * @see Bucket#estimateAbilityToConsume(long)
         */
        public static String getLimitReset(@NonNull final BucketWrapper bucketWrapper) {
            final EstimationProbe probe = bucketWrapper.estimateAbilityToConsume();
            final long nanosToWait = probe.getNanosToWaitForRefill();
            final long resetTimestampEpoch = System.currentTimeMillis() + TimeUnit.NANOSECONDS.toMillis(nanosToWait);
            return String.valueOf(resetTimestampEpoch / TIMESTAMP_SECONDS);
        }

        /**
         * <p>
         *     Creates standardized exception for bucket resolution failures.
         * </p>
         *
         * @param bucketWrapper containing current limit state
         * @return {@link IllegalStateException} with bucket identification details
         */
        private static IllegalStateException getBucketException(@NonNull final BucketWrapper bucketWrapper) {
            return new IllegalStateException("Cannot resolve bucket for key: " + bucketWrapper.getKey());
        }
    }

    /**
     * <p>
     *     Scheduled cleanup task for removing inactive rate limit buckets.
     * </p>
     *
     * <p>
     *     Prevents memory leaks by periodically removing buckets that haven't
     *     been used for 2+ hours. Runs every 15 minutes to balance memory
     *     efficiency with cleanup overhead. Uses atomic operations to ensure
     *     thread safety during concurrent access.
     * </p>
     *
     * @apiNote This method is automatically invoked by Spring. It should not
     * be invoked manually.
     * @see Scheduled
     * @see #bucketShouldBeRemoved(Map.Entry, LocalDateTime)
     */
    @SuppressWarnings("unused")
    @Scheduled(fixedDelay = BUCKET_CLEANUP_TIMEOUT)
    void clearUnusedBuckets() {
        final LocalDateTime cutoff = LocalDateTime.now()
                                                  .minusHours(2);

        ipBuckets.entrySet()
                 .removeIf(entry -> bucketShouldBeRemoved(entry, cutoff));

        userBuckets.entrySet()
                   .removeIf(entry -> bucketShouldBeRemoved(entry, cutoff));
    }

    /**
     * <p>
     *     Determines if bucket should be removed based on last usage date.
     * </p>
     *
     * @param entry bucket map to evaluate for deletion
     * @param cutoff threshold time for bucket deletion decision
     * @return {@code true} if bucket should be deleted, {@code false} otherwise
     */
    private static boolean bucketShouldBeRemoved(final Map.Entry<String, BucketWrapper> entry,
                                                 final LocalDateTime cutoff) {

       return Optional.ofNullable(entry)
                      .map(Map.Entry::getValue)
                      .map(BucketWrapper::getLastUsageDate)
                      .map(AtomicReference::get)
                      .map(d -> d.isBefore(cutoff))
                      .orElse(false);
    }

    /**
     * <p>
     *     Retrieves or creates appropriate rate limit bucket for the request.
     * </p>
     *
     * <p>
     *     Implements intelligent client identification prioritizing authenticated users
     *     over IP-based limiting. Authenticated users receive higher limits and better
     *     tracking, while anonymous requests are limited by IP address with more
     *     restrictive quotas. Bucket is created when is not present by key (user or IP address).
     * </p>
     *
     * @param request HTTP request for client identification
     * @return {@link BucketWrapper} for rate limiting client identification
     * @see #getUserKey()
     * @see #createUserBucket(String)
     * @see #getIpFromRequest(HttpServletRequest)
     * @see #createIpBucket(String)
     */
    private BucketWrapper getBucket(final HttpServletRequest request) {
        return getUserKey().map(key -> userBuckets.computeIfAbsent(key, this::createUserBucket))
                           .orElseGet(() -> ipBuckets.computeIfAbsent(getIpFromRequest(request), this::createIpBucket));
    }

    /**
     * <p>
     *     Extracts client IP address from HTTP request with proxy supports.
     * </p>
     *
     * @param request HTTP request containing IP information
     * @throws IllegalArgumentException if IP address cannot be determined
     * @return client IP address for rate limiting identification
     * @see #IP_HEADERS
     */
    @SuppressWarnings("PMD.OnlyOneReturn")
    private static String getIpFromRequest(@NonNull final HttpServletRequest request) {
        for (final String header: IP_HEADERS) {
            final String ipAddress = request.getHeader(header);

            if (isNotBlank(ipAddress)) {
                return ipAddress.contains(",") ? RegexUtils.split(RegexUtils.COMMA_PATTERN, ipAddress, 0)
                                               : ipAddress;
            }
        }

        return Optional.of(request)
                       .map(HttpServletRequest::getRemoteAddr)
                       .filter(StringUtils::isNotBlank)
                       .orElseThrow(() ->
                               new IllegalArgumentException("The IP request cannot be extracted:: Request: " + request)
                       );
    }

    /**
     * <p>
     *     Extracts authenticated user ID from JWT Token.
     * </p>
     *
     * <p>
     *     Attempts to identify authenticated users through Spring Security
     *     JWT authentication token. Uses the standard <b>'sub' claim</b> as
     *     the unique user ID for rate limiting purposes.
     * </p>
     *
     * @return {@link Optional} containing user ID if authenticated, empty if anonymous.
     * @see SecurityContextHolder
     * @see JwtAuthenticationToken
     */
    private Optional<String> getUserKey() { // TODO: consider changes
        Optional<String> result = Optional.empty();

        final Authentication authentication = SecurityContextHolder.getContext()
                                                                   .getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtToken && jwtToken.isAuthenticated()) {
            final String userId = jwtToken.getToken()
                                          .getClaimAsString("sub");

            if (isNotBlank(userId)) {
                result = Optional.of(userId);
            }
        }

        return result;
    }

    /**
     * <p>
     *     Creates rate limit bucket for authenticated users with proper limits.
     * </p>
     *
     * @param key user ID as bucket key
     * @return {@link BucketWrapper} for authenticated user
     * @see BucketType#USER
     */
    private BucketWrapper createUserBucket(@NonNull final String key) {
        return new BucketWrapper(
                key,
                BucketType.USER,
                Bucket.builder()
                      .addLimit(Bandwidth.classic(
                              BucketType.USER.getLimit(),
                              Refill.intervally(
                                      BucketType.USER.getLimit(),
                                      Duration.ofSeconds(BucketType.LIMIT_SECONDS))
                              )
                      )
                      .addLimit(Bandwidth.classic(
                              BucketType.USER.getBurstLimit(),
                              Refill.intervally(
                                      BucketType.USER.getBurstLimit(),
                                      Duration.ofSeconds(BucketType.BURST_SECONDS))
                              )
                      )
                      .build()
        );
    }

    /**
     * <p>
     *     Creates rate limit bucket for IP address with proper limits.
     * </p>
     *
     * @param key IP address as bucket key
     * @return {@link BucketWrapper} for authenticated user
     * @see BucketType#IP
     */
    private BucketWrapper createIpBucket(@NonNull final String key) {
        return new BucketWrapper(
                key,
                BucketType.IP,
                Bucket.builder()
                      .addLimit(Bandwidth.classic(
                              BucketType.IP.getLimit(),
                              Refill.intervally(
                                      BucketType.IP.getLimit(),
                                      Duration.ofSeconds(BucketType.LIMIT_SECONDS))
                              )
                      )
                      .addLimit(Bandwidth.classic(
                              BucketType.IP.getBurstLimit(),
                              Refill.intervally(
                                      BucketType.IP.getBurstLimit(),
                                      Duration.ofSeconds(BucketType.BURST_SECONDS))
                              )
                      )
                      .build()
        );
    }
}
