package pl.dawid0604.pcforum.category.service.core;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.retry.annotation.EnableRetry;
import org.testcontainers.containers.PostgreSQLContainer;
import pl.dawid0604.pcforum.category.service.commons.Constants;

import static java.util.concurrent.TimeUnit.MINUTES;
import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PROTECTED;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static pl.dawid0604.pcforum.category.service.core.IntegrationTestBase.*;
import static pl.dawid0604.pcforum.category.service.core.IntegrationTestBase.ContainersConfig;

/**
 * The base of all integration tests.
 * <p>
 *     Provides the server port for RestAssured and
 *     sets up required infrastructures components,
 *     such as databases, message brokers etc. This
 *     setup closely reflects the real application state.
 * </p>
 *
 * Extend this class in your integration tests to
 * ensure proper environment configuration.
 */
@Getter(PROTECTED)
@EnableAutoConfiguration
@SpringBootConfiguration
@Import({
        ContainersConfig.class,
        CacheableConfig.class,
        RetryConfig.class
})
@NoArgsConstructor(access = PACKAGE)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@EntityScan(basePackages = "pl.dawid0604.pcforum")
@ComponentScan(basePackages = "pl.dawid0604.pcforum")
@EnableJpaRepositories(basePackages = "pl.dawid0604.pcforum")
public abstract class IntegrationTestBase {

    /**
     * Test configuration that provides containerized dependencies
     * for integration tests.
     *
     * <p>
     *     This class configures and exposes required dependencies
     *     as {@link Bean}, which are used by tests running in the
     *     Spring Boot environment. Thanks to the {@link ServiceConnection}
     *     annotation, the containers are automatically connected as data
     *     sources for teh application context.
     * </p>
     */
    @TestConfiguration
    @SuppressWarnings("PMD.CommentDefaultAccessModifier")
    static class ContainersConfig {

        /**
         * Creates and configures the PostgreSQL container (Version 17-alpine) for
         * integration tests.
         * @return a new PostgreSQL container instance running in the environment
         */
        @Bean
        @ServiceConnection
        @SuppressWarnings("unused")
        PostgreSQLContainer<?> postgreSQLContainer() {
            return new PostgreSQLContainer<>("postgres:17-alpine");
        }
    }

    @EnableRetry
    @TestConfiguration
    static class RetryConfig { }

    @EnableCaching
    @TestConfiguration
    static class CacheableConfig {
        private static final int MAX_SIZE = 500;
        private static final int WRITE_TTL = 30;

        @Bean
        @Primary
        @SuppressWarnings("unused")
        public CaffeineCacheManager caffeineCacheManager() {
            final Caffeine<Object, Object> caffeine = Caffeine.newBuilder()
                                                              .recordStats()
                                                              .maximumSize(MAX_SIZE)
                                                              .expireAfterWrite(WRITE_TTL, MINUTES);

            final CaffeineCacheManager cacheManager = new CaffeineCacheManager(Constants.CACHE_KEY);
                                       cacheManager.setCaffeine(caffeine);
                                       cacheManager.setAsyncCacheMode(true);
            return cacheManager;
        }
    }
}
