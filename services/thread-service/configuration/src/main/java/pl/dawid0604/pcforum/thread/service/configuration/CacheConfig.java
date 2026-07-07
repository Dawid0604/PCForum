package pl.dawid0604.pcforum.thread.service.configuration;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.NoArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import pl.dawid0604.pcforum.thread.service.commons.Constants;

import static java.util.concurrent.TimeUnit.MINUTES;
import static lombok.AccessLevel.PACKAGE;

/**
 * Configuration class for the Caffeine cache.
 *
 * <p>
 *     By annotating this class with {@link EnableCaching},
 *     the application enables effective caching and improves
 *     overall performance.
 * </p>
 *
 * @see EnableCaching
 */
@EnableCaching
@Configuration
@SuppressWarnings("unused")
@NoArgsConstructor(access = PACKAGE)
class CacheConfig {

    /**
     * Maximum number of entities kept in cache for key {@link Constants#CACHE_SINGLE_THREADS_KEY}.
     */
    private static final int SINGLE_THREADS_MAX_SIZE = 5000;

    /**
     * TTL for each entry in minutes for key {@link Constants#CACHE_SINGLE_THREADS_KEY}.
     */
    private static final int SINGLE_THREADS_WRITE_TTL = 10;

    /**
     * ACCESS TTL for each entry in minutes for key {@link Constants#CACHE_SINGLE_THREADS_KEY}.
     */
    private static final int SINGLE_THREADS_ACCESS_TTL = 30;

    /**
     * Maximum number of entities kept in cache for key {@link Constants#CACHE_LIST_OF_THREADS_KEY}.
     */
    private static final int LIST_OF_THREADS_MAX_SIZE = 2000;

    /**
     * TTL for each entry in minutes for key {@link Constants#CACHE_LIST_OF_THREADS_KEY}.
     */
    private static final int LIST_OF_THREADS_WRITE_TTL = 5;

    /**
     * Creates and configures the {@link CaffeineCacheManager} for key
     * {@link Constants#CACHE_SINGLE_THREADS_KEY} used by Spring.
     * @return configures cache manager ready for use.
     */

    @Bean
    @Primary
    public CacheManager singleThreadsCacheManager() {
        final Caffeine<Object, Object> caffeine = Caffeine.newBuilder()
                                                          .recordStats()
                                                          .maximumSize(SINGLE_THREADS_MAX_SIZE)
                                                          .expireAfterWrite(SINGLE_THREADS_WRITE_TTL, MINUTES)
                                                          .expireAfterAccess(SINGLE_THREADS_ACCESS_TTL, MINUTES);

        final CaffeineCacheManager cacheManager = new CaffeineCacheManager(Constants.CACHE_SINGLE_THREADS_KEY);
                                   cacheManager.setCaffeine(caffeine);
                                   cacheManager.setAsyncCacheMode(true);

        return cacheManager;
    }

    /**
     * Creates and configures the {@link CaffeineCacheManager} for key
     * {@link Constants#CACHE_LIST_OF_THREADS_KEY} used by Spring.
     * @return configures cache manager ready for use.
     */
    @Bean
    public CacheManager listOfThreadsCacheManager() {
        final Caffeine<Object, Object> caffeine = Caffeine.newBuilder()
                                                          .recordStats()
                                                          .maximumSize(LIST_OF_THREADS_MAX_SIZE)
                                                          .expireAfterWrite(LIST_OF_THREADS_WRITE_TTL, MINUTES);

        final CaffeineCacheManager cacheManager = new CaffeineCacheManager(Constants.CACHE_LIST_OF_THREADS_KEY);
                                   cacheManager.setCaffeine(caffeine);
                                   cacheManager.setAsyncCacheMode(true);

        return cacheManager;
    }
}
