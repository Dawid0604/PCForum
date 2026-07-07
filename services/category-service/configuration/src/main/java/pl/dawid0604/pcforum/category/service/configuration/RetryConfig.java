package pl.dawid0604.pcforum.category.service.configuration;

import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

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
class RetryConfig { }
