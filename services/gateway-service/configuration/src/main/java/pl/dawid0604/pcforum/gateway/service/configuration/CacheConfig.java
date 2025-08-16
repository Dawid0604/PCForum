package pl.dawid0604.pcforum.gateway.service.configuration;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.NoArgsConstructor;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
     * Maximum number of entities kept in cache.
     */
    private static final int MAX_SIZE = 50;

    /**
     * TTL for each entry in minutes.
     */
    private static final int WRITE_TTL = 5;

    /**
     * Creates and configures the {@link CaffeineCacheManager} used by Spring.
     * @return configures cache manager ready for use.
     */
    @Bean
    public CaffeineCacheManager caffeineCacheManager() {
       final Caffeine<Object, Object> caffeine = Caffeine.newBuilder()
                                                         .recordStats()
                                                         .maximumSize(MAX_SIZE)
                                                         .expireAfterWrite(WRITE_TTL, MINUTES);

       final CaffeineCacheManager cacheManager = new CaffeineCacheManager(Constants.ROUTES_CACHE);
                                  cacheManager.setCaffeine(caffeine);

       return cacheManager;
    }
}
