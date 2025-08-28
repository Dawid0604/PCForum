package pl.dawid0604.pcforum.frontend.service.configuration;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.NoArgsConstructor;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

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
     * User cache name.
     */
    static final String USER_CACHE_NAME = "user";

    /**
     * Referenced data cache name.
     */
    static final String REFERENCED_CACHE_NAME = "referenced_data";

    /**
     * Maximum number of user data kept in cache.
     */
    private static final int USER_MAX_SIZE = 200;

    /**
     * Maximum number of referenced data kept in cache.
     */
    private static final int REFERENCE_MAX_SIZE = 120;

    /**
     * User TTL for each entry in minutes.
     */
    private static final int USER_WRITE_TTL = 30;

    /**
     * Reference TTL for each entry in minutes.
     */
    private static final int REFERENCE_DATA_WRITE_TTL = 120;

    /**
     * Creates and configures the {@link CaffeineCacheManager} used by Spring.
     *
     * <p>
     *     <strong>It creates two types of cache:</strong>
     *     <ul>
     *         <li>
     *             {@link #USER_CACHE_NAME}
     *             <p>
     *                 This type of cache stores a User data such as IDs, roles etc.
     *             </p>
     *         </li>
     *
     *         <li>
     *             {@link #REFERENCED_CACHE_NAME}
     *             <p>
     *                 This type of cache stores a Referenced data such as categories, settings etc.
     *             </p>
     *         </li>
     *     </ul>
     * </p>
     *
     * @return configures cache manager ready for use.
     */
    @Bean
    public CaffeineCacheManager caffeineCacheManager() {
        final CaffeineCacheManager cacheManager = new CaffeineCacheManager();
                                   cacheManager.setCacheNames(List.of(
                                           REFERENCED_CACHE_NAME,
                                           USER_CACHE_NAME
                                   ));

        cacheManager.registerCustomCache(
                USER_CACHE_NAME,
                Caffeine.newBuilder()
                        .recordStats()
                        .maximumSize(USER_MAX_SIZE)
                        .expireAfterWrite(USER_WRITE_TTL, MINUTES)
                        .build()
       );

        cacheManager.registerCustomCache(
                REFERENCED_CACHE_NAME,
                Caffeine.newBuilder()
                        .recordStats()
                        .maximumSize(REFERENCE_MAX_SIZE)
                        .expireAfterWrite(REFERENCE_DATA_WRITE_TTL, MINUTES)
                        .build()
       );

       return cacheManager;
    }
}
