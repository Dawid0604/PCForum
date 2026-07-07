package pl.dawid0604.pcforum.thread.service.configuration;

import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.support.RetryTemplate;

import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLTimeoutException;
import java.sql.SQLTransientConnectionException;

import static lombok.AccessLevel.PACKAGE;

/**
 * <p>
 *     Configuration class that enables Spring Retry mechanism.
 * </p>
 *
 * <p>
 *     This configuration allows automatic retry of failed
 *     operations. For example, when saving a CategoryEntity with
 *     duplicate PublicId that must be unique, the service automatically
 *     retry with a newly generated PublicId.
 * </p>
 *
 * <p>
 *     This makes the application more resilient to transient errors
 *     and database conflicts by providing automatic recovery capabilities.
 * </p>
 *
 * @see EnableRetry
 */
@EnableRetry
@Configuration
@SuppressWarnings("unused")
@NoArgsConstructor(access = PACKAGE)
class RetryConfig {

    @Bean
    public RetryTemplate saveRetryTemplate() {
        return RetryTemplate.builder()
                            .retryOn(SQLIntegrityConstraintViolationException.class)
                            .retryOn(SQLTransientConnectionException.class)
                            .retryOn(SQLTimeoutException.class)
                            .retryOn(QueryTimeoutException.class)
                            .retryOn(TransientDataAccessException.class)
                            .maxAttempts(3)
                            .exponentialBackoff(100, 2, 5000)
                            .build();
    }
}
