package pl.dawid0604.pcforum.category.service.configuration;

import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import static lombok.AccessLevel.PACKAGE;

/**
 * Spring configuration class that enables JPA Auditing features.
 *
 * <p>
 *     By annotating this class with {@link EnableJpaAuditing}, the
 *     application can automatically populate auditing-related fields
 *     such as created and modified dates or users in JPA entities.
 * </p>
 *
 * @see EnableJpaAuditing
 */
@Configuration
@EnableJpaAuditing
@SuppressWarnings("unused")
@NoArgsConstructor(access = PACKAGE)
class AuditingConfig { }
