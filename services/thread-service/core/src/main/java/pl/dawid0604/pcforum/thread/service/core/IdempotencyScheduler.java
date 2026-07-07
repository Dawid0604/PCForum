package pl.dawid0604.pcforum.thread.service.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static lombok.AccessLevel.PACKAGE;

@Slf4j
@Component
@RequiredArgsConstructor(access = PACKAGE)
class IdempotencyScheduler {
    private final IdempotencyService idempotencyService;

    @Scheduled(cron = "0 0 */6 * * *")
    void cleanupExpiredKeys() {
        final int deletedKeys = idempotencyService.deleteExpired();
        log.info("Cleaned up {} expired idempotency keys", deletedKeys);
    }
}
