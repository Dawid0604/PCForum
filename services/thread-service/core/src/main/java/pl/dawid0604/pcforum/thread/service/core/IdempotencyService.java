package pl.dawid0604.pcforum.thread.service.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Strings;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dawid0604.pcforum.thread.service.commons.exception.IdempotencyConflictException;
import pl.dawid0604.pcforum.thread.service.commons.exception.IdempotencyProcessingException;
import pl.dawid0604.pcforum.thread.service.persistence.IdempotencyKeyEntity;
import pl.dawid0604.pcforum.thread.service.persistence.repository.IdempotencyKeyEntityRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;

import static lombok.AccessLevel.PACKAGE;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@Slf4j
@Service
@RequiredArgsConstructor(access = PACKAGE)
class IdempotencyService {
    private final IdempotencyKeyEntityRepository repository;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = REQUIRES_NEW)
    public Optional<String> checkAndLock(final String idempotencyKey, final Object requestPayload) {
        final String requestHash = calculateHash(requestPayload);
        final Optional<IdempotencyKeyEntity> possibleKey = repository.findValidByKey(idempotencyKey, Instant.now());

        if(possibleKey.isPresent()) {
            final IdempotencyKeyEntity idempotencyKeyEntity = possibleKey.get();

            if(!Strings.CS.equals(idempotencyKeyEntity.getRequestHash(), requestHash)) {
                log.error("Idempotency key reused with different parameters for key: {}", idempotencyKey);
                throw new IdempotencyConflictException();
            }

            return handle(idempotencyKeyEntity);
        }

        try {
            repository.insertOrGet(
                    idempotencyKey,
                    requestHash,
                    Instant.now().plus(24, ChronoUnit.HOURS)
            );

            log.info("Created new idempotency lock for key: {}", idempotencyKey);
            return Optional.empty();

        } catch (DataIntegrityViolationException e) {
            log.warn("Concurrent insert detected for key: {}, checking again", idempotencyKey);
            throw new IdempotencyProcessingException();
        }
    }

    private Optional<String> handle(final IdempotencyKeyEntity idempotencyKeyEntity) {
        return switch (idempotencyKeyEntity.getStatus()) {
            case PROCESSING -> {
                log.warn("Operation still processing for key: {}", idempotencyKeyEntity.getIdempotencyKey());
                throw new IdempotencyProcessingException();
            }

            case SUCCESS -> {
                log.info("Returning cached result for key: {}", idempotencyKeyEntity.getIdempotencyKey());
                yield Optional.of(idempotencyKeyEntity.getResultData());
            }

            case FAILED -> {
                log.info("Previous operation failed for key: {} - Allowing retry", idempotencyKeyEntity);
                yield Optional.empty();
            }
        };
    }

    @Transactional(propagation = REQUIRES_NEW)
    public void markAsSuccess(final String idempotencyKey, final String result) {
        repository.markAsSuccess(idempotencyKey, result);
    }

    @Transactional(propagation = REQUIRES_NEW)
    public void markAsFailed(final String idempotencyKey) {
        repository.markAsFailed(idempotencyKey);
    }

    private String calculateHash(final Object payload) {
        try {
            final String json = objectMapper.writeValueAsString(payload);
            final MessageDigest md = MessageDigest.getInstance("SHA-256");
            final byte[] hash = md.digest(json.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder()
                         .encodeToString(hash);

        } catch (Exception e) {
            log.error("Failed to calculate request hash", e);
            throw new IllegalArgumentException("Failed to calculate request hash", e);
        }
    }

    @Transactional
    public int deleteExpired() {
        return repository.deleteExpired(Instant.now());
    }
}
