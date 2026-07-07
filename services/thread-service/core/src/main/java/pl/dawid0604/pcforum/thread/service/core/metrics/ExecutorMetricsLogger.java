package pl.dawid0604.pcforum.thread.service.core.metrics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadPoolExecutor;

import static lombok.AccessLevel.PACKAGE;

@Slf4j
@Component
@RequiredArgsConstructor(access = PACKAGE)
class ExecutorMetricsLogger {
    private final ThreadPoolTaskExecutor databaseTaskExecutor;
    private final ThreadPoolTaskExecutor viewCounterTaskExecutor;

    @Scheduled(
            fixedDelay = 60_000,
            initialDelay = 10_000
    )
    void logMetrics() {
        log(databaseTaskExecutor, "DatabaseTaskExecutor");
        log(viewCounterTaskExecutor, "ViewCounterTaskExecutor");
    }

    private void log(final ThreadPoolTaskExecutor taskExecutor, final String queueName) {
        final ThreadPoolExecutor poolExecutor = taskExecutor.getThreadPoolExecutor();
        final int queueSize = poolExecutor.getQueue().size();
        final int queueCapacity = taskExecutor.getQueueCapacity();
        final int activeThreads = poolExecutor.getActiveCount();
        final int maximumThreadsCount = poolExecutor.getMaximumPoolSize();
        final double queueSizeEightyPercent = taskExecutor.getQueueCapacity() * 0.80;

        if(queueSize > queueSizeEightyPercent) {
            log.warn(
                    "ALERT: {} executor is almost full: {} / {}",
                    queueName,
                    queueSize,
                    queueCapacity
            );
        }

        if(activeThreads >= maximumThreadsCount) {
            log.warn(
                    "ALERT: {} executor reached maximum thread count: {}/{}",
                    queueName,
                    activeThreads,
                    maximumThreadsCount
            );
        }

        if(queueSize >= queueCapacity && activeThreads >= maximumThreadsCount) {
            log.error(
                    "CRITICAL: {} executor is completely busy: {}/{}",
                    queueName,
                    activeThreads,
                    maximumThreadsCount
            );
        }
    }
}
