package pl.dawid0604.pcforum.moderation.service.configuration;

import lombok.NoArgsConstructor;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

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
class CacheConfig { }
