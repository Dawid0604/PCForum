package pl.dawid0604.pcforum.thread.service.configuration;

import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static lombok.AccessLevel.PACKAGE;

@Configuration
@NoArgsConstructor(access = PACKAGE)
class TransactionConfig {

    @Bean
    @Primary
    public TransactionTemplate transactionTemplate(final PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }

    @Bean
    public TransactionTemplate transactionReadOnlyTemplate(final PlatformTransactionManager transactionManager) {
        final TransactionTemplate template = new TransactionTemplate(transactionManager);
                                  template.setReadOnly(true);
        return template;
    }
}
