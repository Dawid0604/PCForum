package pl.dawid0604.pcforum.thread.service.configuration;

import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

import static lombok.AccessLevel.PACKAGE;

@EnableAsync
@Configuration
@SuppressWarnings("unused")
@NoArgsConstructor(access = PACKAGE)
class TaskExecutorsConfig {

    @Bean("databaseTaskExecutor")
    public ThreadPoolTaskExecutor databaseTaskExecutor(

            @Value("${custom.async.db.core-pool-size}")
            final int corePoolSize,

            @Value("${custom.async.db.max-pool-size}")
            final int maxPoolSize,

            @Value("${custom.async.db.queue-capacity}")
            final int queueCapacity,

            @Value("${custom.async.db.await-termination-seconds}")
            final int awaitTerminationSeconds,

            @Value("${custom.async.db.keep-alive-seconds}")
            final int keepAliveSeconds) {

        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
                                     executor.setCorePoolSize(corePoolSize);
                                     executor.setMaxPoolSize(maxPoolSize);
                                     executor.setQueueCapacity(queueCapacity);
                                     executor.setKeepAliveSeconds(keepAliveSeconds);
                                     executor.setThreadNamePrefix("db-async-");
                                     executor.setWaitForTasksToCompleteOnShutdown(true);
                                     executor.setAwaitTerminationSeconds(awaitTerminationSeconds);
                                     executor.setRejectedExecutionHandler(
                                             new ThreadPoolExecutor.AbortPolicy()
                                     );
                                     executor.initialize();

        return executor;
    }

    @Bean("viewCounterTaskExecutor")
    public ThreadPoolTaskExecutor viewCounterTaskExecutor(

            @Value("${custom.async.view-counter.core-pool-size}")
            final int corePoolSize,

            @Value("${custom.async.view-counter.max-pool-size}")
            final int maxPoolSize,

            @Value("${custom.async.view-counter.queue-capacity}")
            final int queueCapacity,

            @Value("${custom.async.view-counter.await-termination-seconds}")
            final int awaitTerminationSeconds,

            @Value("${custom.async.view-counter.keep-alive-seconds}")
            final int keepAliveSeconds) {

        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
                                     executor.setCorePoolSize(corePoolSize);
                                     executor.setMaxPoolSize(maxPoolSize);
                                     executor.setQueueCapacity(queueCapacity);
                                     executor.setKeepAliveSeconds(keepAliveSeconds);
                                     executor.setThreadNamePrefix("view-counter-async-");
                                     executor.setWaitForTasksToCompleteOnShutdown(true);
                                     executor.setAwaitTerminationSeconds(awaitTerminationSeconds);
                                     executor.setRejectedExecutionHandler(
                                             new ThreadPoolExecutor.CallerRunsPolicy()
                                     );
                                     executor.initialize();

        return executor;
    }
}
