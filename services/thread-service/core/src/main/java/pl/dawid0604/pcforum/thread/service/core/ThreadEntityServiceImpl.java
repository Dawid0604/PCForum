package pl.dawid0604.pcforum.thread.service.core;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import pl.dawid0604.pcforum.thread.service.commons.Constants;
import pl.dawid0604.pcforum.thread.service.commons.dto.*;
import pl.dawid0604.pcforum.thread.service.commons.exception.ResourceNotFoundException;
import pl.dawid0604.pcforum.thread.service.persistence.*;
import pl.dawid0604.pcforum.thread.service.persistence.repository.ThreadEntityRepository;

import javax.naming.ServiceUnavailableException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static lombok.AccessLevel.PACKAGE;

@Slf4j
@Service
@RequiredArgsConstructor(access = PACKAGE)
class ThreadEntityServiceImpl implements ThreadEntityService {
    private final ThreadEntityRepository repository;
    private final ThreadEntityMapper mapper;
    private final TransactionTemplate transactionTemplate;
    private final TransactionTemplate transactionReadOnlyTemplate;
    private final RetryTemplate retryTemplate;
    private final ThreadEventPublisher threadEventPublisher;
    private final IdempotencyService idempotencyService;
    private final ThreadEventFactory eventFactory;

    @Override
    @Async("databaseTaskExecutor")
    @TimeLimiter(name = Constants.TIME_LIMITER_FAST_KEY)
    @CircuitBreaker(
            name = Constants.SERVICE_NAME,
            fallbackMethod = "countFallback"
    )
    public CompletableFuture<Long> count(final String categoryId) {
        final Long countResult = transactionReadOnlyTemplate.execute(
                status -> repository.countByCategoryId(categoryId)
        );

        return CompletableFuture.completedFuture(countResult);
    }

    @Override
    @Async("databaseTaskExecutor")
    @Cacheable(
            value = Constants.CACHE_SINGLE_THREADS_KEY,
            key = "#publicId",
            cacheManager = "singleThreadsCacheManager"
    )
    @TimeLimiter(name = Constants.TIME_LIMITER_FAST_KEY)
    @CircuitBreaker(
            name = Constants.SERVICE_NAME,
            fallbackMethod = "findDetailsFallback"
    )
    public CompletableFuture<ThreadDetailsDto> findDetails(final String publicId) {
        final ThreadDetailsDto detailsResult = transactionReadOnlyTemplate.execute(status ->
                repository.findByPublicId(publicId)
                          .map(mapper::toDetailsDto)
                          .orElseThrow(this::getResourceNotFoundException)
        );

        return CompletableFuture.completedFuture(detailsResult);
    }

    @Override
    @Async("databaseTaskExecutor")
    @Cacheable(
            value = Constants.CACHE_LIST_OF_THREADS_KEY,
            cacheManager = "listOfThreadsCacheManager",
            keyGenerator = "cacheHashKeyGenerator"
    )
    @TimeLimiter(name = Constants.TIME_LIMITER_MODERATE_KEY)
    @CircuitBreaker(
            name = Constants.SERVICE_NAME,
            fallbackMethod = "findAllByCategoryIdFallback"
    )
    public CompletableFuture<Page<CategoryThreadDto>> findAllByCategoryId(final String categoryId, final int page, final int size) {
        final Page<CategoryThreadDto> pageResult = transactionReadOnlyTemplate.execute(status -> {
            final int offset = page * size;
            final List<CategoryThread> result = repository.findAllByCategoryId(categoryId, size, offset);

            if (result.isEmpty()) {
                return Page.empty(PageRequest.of(page, size));
            }

            final long totalCount = result.getFirst().totalCount();
            final List<CategoryThreadDto> dtos = result.stream()
                                                       .map(mapper::toCategoryDto)
                                                       .toList();

            return new PageImpl<>(dtos, PageRequest.of(page, size), totalCount);
        });

        return CompletableFuture.completedFuture(pageResult);
    }

    @Override
    @Async("databaseTaskExecutor")
    @Cacheable(
            value = Constants.CACHE_LIST_OF_THREADS_KEY,
            cacheManager = "listOfThreadsCacheManager",
            keyGenerator = "cacheHashKeyGenerator"
    )
    @TimeLimiter(name = Constants.TIME_LIMITER_MODERATE_KEY)
    @CircuitBreaker(
            name = Constants.SERVICE_NAME,
            fallbackMethod = "findAllByUserIdFallback"
    )
    public CompletableFuture<Page<UserThreadDto>> findAllByUserId(final String userId, final int page, final int size) {
        final Page<UserThreadDto> pageResult = transactionReadOnlyTemplate.execute(status -> {
            final int offset = page * size;
            final List<UserThread> result = repository.findAllByUserId(userId, size, offset);

            if (result.isEmpty()) {
                return Page.empty(PageRequest.of(page, size));
            }

            final long totalCount = result.getFirst().totalCount();
            final List<UserThreadDto> dtos = result.stream()
                                                   .map(mapper::toUserDto)
                                                   .toList();

            return new PageImpl<>(dtos, PageRequest.of(page, size), totalCount);
        });

        return CompletableFuture.completedFuture(pageResult);
    }

    @Override
    @Async("databaseTaskExecutor")
    @Cacheable(
            value = Constants.CACHE_LIST_OF_THREADS_KEY,
            cacheManager = "listOfThreadsCacheManager",
            keyGenerator = "cacheHashKeyGenerator"
    )
    @TimeLimiter(name = Constants.TIME_LIMITER_MODERATE_KEY)
    @CircuitBreaker(
            name = Constants.SERVICE_NAME,
            fallbackMethod = "findAllByTitleAndContentFallback"
    )
    public CompletableFuture<Page<MatchedThreadDto>> findAllByTitleAndContent(final String query, final int page, final int size) {
        final Page<MatchedThreadDto> pageResult = transactionReadOnlyTemplate.execute(status -> {
            final int offset = page * size;
            final List<MatchedThread> result = repository.findAllByTitleAndContent(query, size, offset);

            if (result.isEmpty()) {
                return Page.empty(PageRequest.of(page, size));
            }

            final long totalCount = result.getFirst().totalCount();
            final List<MatchedThreadDto> dtos = result.stream()
                                                      .map(mapper::toMatchedDto)
                                                      .toList();

            return new PageImpl<>(dtos, PageRequest.of(page, size), totalCount);
        });

        return CompletableFuture.completedFuture(pageResult);
    }

    @Override
    @Async("databaseTaskExecutor")
    @CircuitBreaker(
            name = Constants.SERVICE_NAME,
            fallbackMethod = "createFallback"
    )
    public CompletableFuture<String> create(final ThreadEntityDto payload, final String idempotencyKey) {
        try {
            final Optional<String> cachedResult = idempotencyService.checkAndLock(idempotencyKey, payload);

            if (cachedResult.isPresent()) {
                log.info("Returning existing result for idempotency key: {}", idempotencyKey);
                return CompletableFuture.completedFuture(cachedResult.get());
            }

            final String publicId = retryTemplate.execute(context -> transactionTemplate.execute(status -> {
                        final ThreadEntity thread = ThreadEntity.builder()
                                                                .title(payload.title())
                                                                .content(payload.content())
                                                                .categoryId(payload.categoryId())
                                                                .userId(NanoIdUtils.randomNanoId()) // TODO: temporary
                                                                .build();

                        final String generatedPublicId = repository.save(thread)
                                                                   .getPublicId();

                        idempotencyService.markAsSuccess(idempotencyKey, generatedPublicId);

                        final ThreadCreatedEvent event = eventFactory.createThreadCreatedEvent(generatedPublicId, thread, payload);
                        threadEventPublisher.publishThreadCreatedEvent(event);

                        return generatedPublicId;
                    })
            );

            return CompletableFuture.completedFuture(publicId);

        } catch (Exception e) {
            log.error("Failed to create thread with idempotency key: {}", idempotencyKey, e);
            idempotencyService.markAsFailed(idempotencyKey);
            throw e;
        }
    }

    @Override
    @Async("databaseTaskExecutor")
    @CircuitBreaker(
            name = Constants.SERVICE_NAME,
            fallbackMethod = "editFallback"
    )
    @CacheEvict(
            value = Constants.CACHE_SINGLE_THREADS_KEY,
            key = "#payload.publicId()",
            cacheManager = "singleThreadsCacheManager"
    )
    public CompletableFuture<String> edit(final ThreadEntityDto payload) {
        final String publicId = transactionTemplate.execute(status -> {
            final ThreadEntity thread = repository.findEntityByPublicId(payload.publicId())
                                                  .orElseThrow(this::getResourceNotFoundException);

            if(!Objects.equals(payload.title(), thread.getTitle())) {
                thread.setTitle(payload.title());
            }

            if(!Objects.equals(payload.content(), thread.getContent())) {
                thread.setContent(payload.content());
            }

            return repository.save(thread)
                             .getPublicId();
        });

        return CompletableFuture.completedFuture(publicId);
    }

    @Override
    @Async("viewCounterTaskExecutor")
    @CircuitBreaker(
            name = Constants.SERVICE_NAME,
            fallbackMethod = "incrementNumberOfViewsFallback"
    )
    @TimeLimiter(name = Constants.TIME_LIMITER_FAST_KEY)
    public CompletableFuture<Void> incrementNumberOfViews(final String publicId) {
        transactionTemplate.execute(status -> {
            if(repository.updateNumberOfViews(publicId) <= 0) {
                throw getResourceNotFoundException();
            }

            return null;
        });

        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async("databaseTaskExecutor")
    @CircuitBreaker(
            name = Constants.SERVICE_NAME,
            fallbackMethod = "deleteFallback"
    )
    @CacheEvict(
            value = Constants.CACHE_SINGLE_THREADS_KEY,
            key = "#publicId",
            cacheManager = "singleThreadsCacheManager"
    )
    public CompletableFuture<Void> delete(final String publicId) {
        transactionTemplate.execute(status -> {
            if(repository.deleteByPublicId(publicId) <= 0) {
                throw getResourceNotFoundException();
            }

            return null;
        });

        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async("databaseTaskExecutor")
    @CircuitBreaker(
            name = Constants.SERVICE_NAME,
            fallbackMethod = "pinFallback"
    )
    @CacheEvict(
            value = Constants.CACHE_SINGLE_THREADS_KEY,
            key = "#publicId",
            cacheManager = "singleThreadsCacheManager"
    )
    public CompletableFuture<Void> pin(final String publicId) {
        transactionTemplate.execute(status -> {
            if(repository.updateStatus(publicId, ThreadStatus.PINNED) <= 0) {
                throw getResourceNotFoundException();
            }

            final ThreadStatusChangedEvent event = eventFactory.createThreadStatusChangedEvent(publicId, ThreadStatus.PINNED);
            threadEventPublisher.publishThreadStatusChangedEvent(event);

            return null;
        });

        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async("databaseTaskExecutor")
    @CircuitBreaker(
            name = Constants.SERVICE_NAME,
            fallbackMethod = "banFallback"
    )
    @CacheEvict(
            value = Constants.CACHE_SINGLE_THREADS_KEY,
            key = "#publicId",
            cacheManager = "singleThreadsCacheManager"
    )
    public CompletableFuture<Void> ban(final String publicId) {
        transactionTemplate.execute(status -> {
            if(repository.updateStatus(publicId, ThreadStatus.BANNED) <= 0) {
                throw getResourceNotFoundException();
            }

            final ThreadStatusChangedEvent event = eventFactory.createThreadStatusChangedEvent(publicId, ThreadStatus.BANNED);
            threadEventPublisher.publishThreadStatusChangedEvent(event);

            return null;
        });

        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async("databaseTaskExecutor")
    @CircuitBreaker(
            name = Constants.SERVICE_NAME,
            fallbackMethod = "closeFallback"
    )
    @CacheEvict(
            value = Constants.CACHE_SINGLE_THREADS_KEY,
            key = "#publicId",
            cacheManager = "singleThreadsCacheManager"
    )
    public CompletableFuture<Void> close(final String publicId) {
        transactionTemplate.execute(status -> {
            if(repository.updateStatus(publicId, ThreadStatus.CLOSED) <= 0) {
                throw getResourceNotFoundException();
            }

            final ThreadStatusChangedEvent event = eventFactory.createThreadStatusChangedEvent(publicId, ThreadStatus.CLOSED);
            threadEventPublisher.publishThreadStatusChangedEvent(event);

            return null;
        });

        return CompletableFuture.completedFuture(null);
    }

    @SuppressWarnings("unused")
    private CompletableFuture<Void> pinFallback(final String publicId, final Exception exception) {
        log.error("Fallback pin method invoked", exception);
        return CompletableFuture.failedFuture(getUnavailableException());
    }

    @SuppressWarnings("unused")
    private CompletableFuture<Void> banFallback(final String publicId, final Exception exception) {
        log.error("Fallback ban method invoked", exception);
        return CompletableFuture.failedFuture(getUnavailableException());
    }

    @SuppressWarnings("unused")
    private CompletableFuture<Void> closeFallback(final String publicId, final Exception exception) {
        log.error("Fallback close method invoked", exception);
        return CompletableFuture.failedFuture(getUnavailableException());
    }

    @SuppressWarnings("unused")
    private CompletableFuture<Void> incrementNumberOfViewsFallback(final String publicId, final Exception exception) {
        log.error("Fallback incrementNumberOfViews method invoked", exception);
        return CompletableFuture.failedFuture(getUnavailableException());
    }

    @SuppressWarnings("unused")
    private CompletableFuture<Void> deleteFallback(final String publicId, final Exception exception) {
        log.error("Fallback delete method invoked", exception);
        return CompletableFuture.failedFuture(getUnavailableException());
    }

    @SuppressWarnings("unused")
    private CompletableFuture<String> createFallback(final ThreadEntityDto payload, final String idempotencyKey, final Exception exception) {
        log.error("Fallback create method invoked", exception);
        return CompletableFuture.failedFuture(getUnavailableException());
    }

    private ResourceNotFoundException getResourceNotFoundException() {
        return new ResourceNotFoundException("Thread with given identifier not found");
    }

    @SuppressWarnings("unused")
    private CompletableFuture<Page<CategoryThreadDto>> findAllByCategoryIdFallback(final String categoryId, final int page,
                                                                                   final int size, final Exception exception) {

        log.error("Fallback findAllByCategoryId method invoked", exception);
        return CompletableFuture.failedFuture(getUnavailableException());
    }

    @SuppressWarnings("unused")
    private CompletableFuture<Page<UserThreadDto>> findAllByUserIdFallback(final String userId, final int page,
                                                                           final int size, final Exception exception) {

        log.error("Fallback findAllByUserId method invoked", exception);
        return CompletableFuture.failedFuture(getUnavailableException());
    }

    @SuppressWarnings("unused")
    private CompletableFuture<String> editFallback(final ThreadEntityDto payload, final Exception exception) {
        log.error("Fallback edit method invoked", exception);
        return CompletableFuture.failedFuture(getUnavailableException());
    }

    @SuppressWarnings("unused")
    private CompletableFuture<Page<MatchedThreadDto>> findAllByTitleAndContentFallback(final String query, final int page,
                                                                                       final int size, final Exception exception) {

        log.error("Fallback findAllByTitleAndContent method invoked", exception);
        return CompletableFuture.failedFuture(getUnavailableException());
    }

    @SuppressWarnings("unused")
    private CompletableFuture<Long> countFallback(final String categoryId, final Exception exception) {
        log.error("Fallback count method invoked", exception);
        return CompletableFuture.failedFuture(getUnavailableException());
    }

    @SuppressWarnings("unused")
    private CompletableFuture<ThreadDetailsDto> findDetailsFallback(final String publicId, final Exception exception) {
        log.error("Fallback findDetails method invoked", exception);
        return CompletableFuture.failedFuture(new ServiceUnavailableException());
    }

    private static ServiceUnavailableException getUnavailableException() {
        return new ServiceUnavailableException("Operation failed, service is currently unavailable");
    }
}
