package pl.dawid0604.pcforum.category.service.core;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dawid0604.pcforum.category.service.commons.Constants;
import pl.dawid0604.pcforum.category.service.commons.dto.CategoryEntityCreateDto;
import pl.dawid0604.pcforum.category.service.commons.dto.CategoryWrapperDto;
import pl.dawid0604.pcforum.category.service.persistence.CategoryEntity;
import pl.dawid0604.pcforum.category.service.persistence.CategoryEntityRepository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.groupingBy;
import static lombok.AccessLevel.PACKAGE;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * Service for Categories.
 * The class is not inheritable.
 * @see CategoryEntity
 */
@Slf4j
@Service
@RequiredArgsConstructor(access = PACKAGE)
class CategoryEntityServiceImpl implements CategoryEntityService {

    /**
     * MapStructs mapper to easier mapping.
     */
    private final CategoryEntityMapper mapper;

    /**
     * Repository to manage entities.
     */
    private final CategoryEntityRepository repository;

    /**
     * <p>
     *     Backoff {@link Retryable} time in milliseconds for {@link #save(CategoryEntityCreateDto)} method.
     * </p>
     */
    private static final int RETRAYABLE_BACKOFF_SAVING = 100;

    /**
     * <p>
     *     Method to search categories with subcategories.
     * </p>
     *
     * <p>
     *     The {@code parentId} parameter is optional. It returns
     *     categories whose parentId is equal a given parameter
     *     otherwise it returns every category with their subcategories.
     * </p>
     *
     * <p>
     *     <strong>Features:</strong>
     *     <ul>
     *         <li>
     *             <b>Caching:</b>
     *             Results are cached to improve performance and database load.
     *         </li>
     *
     *         <li>
     *             <b>Circuit breaker:</b>
     *             Protects against database failures with
     *             {@link #getCategoriesFallback(String, Exception)} fallback method.
     *         </li>
     *
     *         <li>
     *             <b>Time limiter:</b>
     *             Prevents long-running queries from blocking the system.
     *         </li>
     *
     *         <li>
     *             <b>Retry:</b>
     *             Automatically retries transient failures.
     *         </li>
     *     </ul>
     * </p>
     *
     * <p>
     *     <strong>Configuration:</strong>
     *     Resilience patterns are configured in application.yml file under:
     *     <ul>
     *         <li>
     *             {@code resilience4j.circuitbreaker.instances}.{@link Constants#SERVICE_NAME}
     *         </li>
     *
     *         <li>
     *             {@code resilience4j.timelimiter.instances}.{@link Constants#SERVICE_NAME}
     *         </li>
     *
     *         <li>
     *             {@code resilience4j.retry.instances}.{@link Constants#SERVICE_NAME}
     *         </li>
     *     </ul>
     * </p>
     *
     * @param parentId {@link CategoryEntity} public ID.
     * @return {@link CompletableFuture} containing categories or empty
     * list if no categories found or fallback method is triggered.
     * @see CategoryEntity
     * @see CategoryWrapperDto
     * @see CircuitBreaker
     * @see TimeLimiter
     * @see Retry
     * @see #getCategoriesFallback(String, Exception)
     */
    @Override
    @Cacheable(
            value = Constants.CACHE_KEY,
            key = "#parentId != null ? #parentId : 'root'",
            unless = "#result == null || #result.isEmpty()"
    )
    @CircuitBreaker(
            name = Constants.SERVICE_NAME,
            fallbackMethod = "getCategoriesFallback"
    )
    @TimeLimiter(
            name = Constants.SERVICE_NAME
    )
    @Retry(
            name = Constants.SERVICE_NAME
    )
    public CompletableFuture<List<CategoryWrapperDto>> getCategories(final String parentId) {
        return CompletableFuture.supplyAsync(() -> getCategoriesRunnable(parentId));
    }

    /**
     * <p>
     *     Method to search a single category.
     * </p>
     *
     * <p>
     *     The {@code categoryId} parameter indicates
     *     the desired category.
     * </p>
     *
     * <p>
     *     Unlike {@link #getCategories(String)}, this method
     *     returns a single category without its subcategory hierarchy.
     *     This method is mainly to indicate in HTTP Location Header a
     *     new created {@link CategoryEntity} via {@link #save(CategoryEntityCreateDto)}
     * </p>
     *
     * <p>
     *     <strong>Features:</strong>
     *     <ul>
     *         <li>
     *             <b>Circuit breaker:</b>
     *             Protects against database failures with
     *             {@link #getCategoryFallback(String, Exception)} fallback method.
     *         </li>
     *
     *         <li>
     *             <b>Time limiter:</b>
     *             Prevents long-running queries from blocking the system.
     *         </li>
     *
     *         <li>
     *             <b>Retry:</b>
     *             Automatically retries transient failures.
     *         </li>
     *     </ul>
     * </p>
     *
     * <p>
     *     <strong>Configuration:</strong>
     *     Resilience patterns are configured in application.yml file under:
     *     <ul>
     *         <li>
     *             {@code resilience4j.circuitbreaker.instances}.{@link Constants#SERVICE_NAME}
     *         </li>
     *
     *         <li>
     *             {@code resilience4j.timelimiter.instances}.{@link Constants#SERVICE_NAME}
     *         </li>
     *
     *         <li>
     *             {@code resilience4j.retry.instances}.{@link Constants#SERVICE_NAME}
     *         </li>
     *     </ul>
     * </p>
     * @param categoryId {@link CategoryEntity} public ID.
     * @return {@link CompletableFuture} containing category or
     * empty optional if no category found or fallback method is triggered.
     * {@link CompletableFuture#join()} method.
     * @see CategoryEntity
     * @see CategoryWrapperDto
     * @see CircuitBreaker
     * @see TimeLimiter
     * @see Retry
     * @see #getCategoryFallback(String, Exception)
     */
    @Override
    @CircuitBreaker(
            name = Constants.SERVICE_NAME,
            fallbackMethod = "getCategoryFallback"
    )
    @TimeLimiter(
            name = Constants.SERVICE_NAME
    )
    @Retry(
            name = Constants.SERVICE_NAME
    )
    public CompletableFuture<Optional<CategoryWrapperDto>> getCategory(final String categoryId) {
        return CompletableFuture.supplyAsync(() -> getCategoryRunnable(categoryId));
    }

    /**
     * <p>
     *     Method to create a new category.
     * </p>
     *
     * <p>
     *     The {@code payload} parameter contains all
     *     necessary data for category creation. The {@code parentId}
     *     within payload is optional - when provided, creates a subcategory;
     *     when {@code null}, creates a new root category.
     * </p>
     *
     * <p>
     *     <strong>Unique public ID generation:</strong>
     *     Each category receives a unique 21-character NanoId
     *     (via {@link com.aventrix.jnanoid.jnanoid.NanoIdUtils}) as its public
     *     identifier automatically generated via {@link CategoryEntity} pre
     *     persist method. In the extremely rare case of ID collision, the retry
     *     mechanism ensures a new unique ID is generated. Which is why
     *     method contains {@link Retryable} annotation. The {@link Retryable#retryFor()}
     *     indicates the {@link DataIntegrityViolationException} which is thrown when
     *     the PublicId collides with (Database unique index) existing query.
     *     There are default 3 save attempts with {@link #RETRAYABLE_BACKOFF_SAVING}ms
     *     backoff. If all retry attempts fail, the
     *     {@link #saveRecover(Exception, CategoryEntityCreateDto)}
     *     method provides graceful error handling and user-friendly error messages.
     * </p>
     *
     * <p>
     *     This method is transactional to ensure data consistency
     *     and automatic rollback on failures.
     * </p>
     *
     * @param payload containing all necessary data to create a new {@link CategoryEntity}.
     * @throws EntityNotFoundException when entity with payload
     * {@code parentId} not exists in the database.
     * @return public ID (NanoId) of newly created category.
     * @apiNote This method returns public ID that should be only
     * used in HTTP Location Header to indicate that category.
     * @see Transactional
     * @see Retryable
     * @see com.aventrix.jnanoid.jnanoid.NanoIdUtils
     * @see #saveRecover(Exception, CategoryEntityCreateDto)
     * @see CategoryEntityCreateDto
     */
    @Override
    @Transactional
    @Retryable(
            retryFor = DataIntegrityViolationException.class,
            notRecoverable = EntityNotFoundException.class,
            backoff = @Backoff(delay = RETRAYABLE_BACKOFF_SAVING),
            recover = "saveRecover"
    )
    public String save(final CategoryEntityCreateDto payload) {
        final CategoryEntity parent;

        if (isNotBlank(payload.parentId())) {
            parent = repository.findByPublicId(payload.parentId())
                               .orElseThrow(() ->
                                       new EntityNotFoundException(
                                               "Category with publicId not found:: " + payload.parentId()
                                       )
                               );

        } else {
            parent = null;
        }

        final CategoryEntity category = CategoryEntity.builder()
                                                      .name(payload.name())
                                                      .description(payload.description())
                                                      .iconPath(payload.iconPath())
                                                      .parent(parent)
                                                      .build();

        return repository.save(category)
                         .getPublicId();
    }

    /**
     * <p>
     *     The runnable method used by {@link #getCategories(String)}.
     * </p>
     *
     * <p>
     *     The results are mapped by MapStruct {@link #mapper}.
     * </p>
     *
     * @param parentId public category parentId.
     * @return {@link List} containing {@link CategoryWrapperDto}.
     * @apiNote unlike {@link #getCategoryRunnable(String)} this method returns
     * categories with their subcategories.
     */
    private List<CategoryWrapperDto> getCategoriesRunnable(final String parentId) {
        return buildCategoryTree(
                parentId,
                Optional.ofNullable(parentId)
                        .filter(StringUtils::isNotBlank)
                        .map(repository::findByParentId)
                        .orElseGet(repository::findAllCustom)
        );
    }

    /**
     * <p>
     *     The runnable method used by {@link #getCategory(String)}.
     * </p>
     *
     * <p>
     *     The result is mapped by MapStruct {@link #mapper}.
     * </p>
     *
     * @param categoryId public category ID.
     * @return {@link Optional} containing {@link CategoryWrapperDto}.
     * @see #repository
     * @see #mapper
     * @apiNote This method returns only category without its subcategories.
     */
    private Optional<CategoryWrapperDto> getCategoryRunnable(final String categoryId) {
        return repository.findByPublicId(categoryId)
                         .map(mapper::wrap);
    }

    /**
     * <p>
     *     Builds complete category hierarchy from flat list of category entities.
     * </p>
     *
     * <p>
     *     This method transforms a collection of {@link CategoryEntity} objects
     *     derived from {@link #repository} into a structured tree of {@link CategoryWrapperDto}
     *     with proper parent-child relationships. Uses parallel processing with Virtual Threads
     *     for optimal performance.
     * </p>
     *
     * <p>
     *     Results are mapped by MapStruct {@link #mapper}.
     * </p>
     *
     * @param parentId categoryId, can be null
     * @param categories entities derived from {@link #repository}.
     * @return list of {@link CategoryWrapperDto} containing grouped categories with their subcategories.
     * @see Executors#newVirtualThreadPerTaskExecutor()
     * @see #mapper
     * @see CompletableFuture
     */
    @SuppressWarnings({ "PMD.OnlyOneReturn", "PMD.AvoidLiteralsInIfCondition" })
    private List<CategoryWrapperDto> buildCategoryTree(final String parentId, final List<CategoryEntity> categories) {
        final Map<Long, List<CategoryEntity>> childrenByParent = Stream.ofNullable(categories)
                                                                       .flatMap(List::stream)
                                                                       .filter(c -> c.getParent() != null)
                                                                       .collect(groupingBy(c -> c.getParent().getId()));

        final List<CategoryEntity> rootCategories = Stream.ofNullable(categories)
                                                          .flatMap(List::stream)
                                                          .filter(c -> isRootCategory(c, parentId))
                                                          .toList();

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<CategoryWrapperDto> result = rootCategories.stream()
                                                            .map(r -> toFuture(r, childrenByParent, executor))
                                                            .map(CompletableFuture::join)
                                                            .toList();

            // Remove parent category because we need its subcategories
            if (isNotBlank(parentId) && !isEmpty(result)) {
                final CategoryWrapperDto rootCategory = result.getFirst();

                if (result.size() > 1) {
                    result = result.subList(1, result.size());

                } else {
                    result = rootCategory.subCategories();
                }
            }

            return result;
        }
    }

    /**
     * <p>
     *     Auxiliary method to indicate that given category is root category.
     * </p>
     *
     * <p>
     *     A category is treated as root category in the following cases:
     *
     *     <ol>
     *         <li>
     *             The {@code category} has no parent (parent field is null)
     *         </li>
     *
     *         <li>
     *             The {@code parentId} is equal {@code category} public ID field
     *         </li>
     *     </ol>
     * </p>
     *
     * @param category that we want to check
     * @param parentId the parent category public ID used to identify
     *                 if this category should be treated as root
     *                 in specific context.
     * @return {@code true} if given category is root category, {@code false} otherwise
     */
    private static boolean isRootCategory(final CategoryEntity category, final String parentId) {
        return Optional.ofNullable(category)
                       .filter(c -> c.getParent() == null || Strings.CS.equals(c.getPublicId(), parentId))
                       .isPresent();
    }

    /**
     * <p>
     *     Recursively build a complete category hierarchy starting from the
     *     specified root {@link CategoryEntity}.
     * </p>
     *
     * <p>
     *     Results are mapped by MapStruct {@link #mapper}.
     * </p>
     *
     * <p>
     *     <strong>Recursive algorithm:</strong>
     *     <ol>
     *         <li>
     *             Retrieves root children of the current category from {@code childrenPyParent} parameter.
     *         </li>
     *
     *         <li>
     *             For each child, recursively creates an asynchronous task via
     *             {@link #toFuture(CategoryEntity, Map, Executor)}.
     *         </li>
     *
     *         <li>
     *             Waits for all child processing to complete using {@link CompletableFuture#join()}.
     *         </li>
     *
     *         <li>
     *             Constructs the final {@link CategoryWrapperDto} with all nested subcategories.
     *         </li>
     *     </ol>
     * </p>
     * @param rootCategory the starting {@link CategoryEntity} for this recursive branch, will be
     *                     wrapped along with all its descendant categories.
     * @param childrenPyParent grouped subcategories by parent category ID.
     * @param executor Virtual Threads executor.
     * @return Fully constructed {@link CategoryWrapperDto} containing the root category with its
     * subcategories.
     * @see #toFuture(CategoryEntity, Map, Executor)
     * @see CategoryWrapperDto
     * @see #mapper
     */
    private CategoryWrapperDto buildCategoryWrapperRecursively(final CategoryEntity rootCategory,
                                                               final Map<Long, List<CategoryEntity>> childrenPyParent,
                                                               final Executor executor) {

        final List<CategoryEntity> children = childrenPyParent.getOrDefault(rootCategory.getId(), emptyList());
        final List<CategoryWrapperDto> childrenSubCategories = children.stream()
                                                                       .map(c -> toFuture(
                                                                               c, childrenPyParent, executor)
                                                                       )
                                                                       .map(CompletableFuture::join)
                                                                       .toList();

        return mapper.wrap(rootCategory, childrenSubCategories);
    }

    /**
     * <p>
     *     Creates an asynchronous task for recursive category tree building.
     * </p>
     *
     * <p>
     *     This method wraps the {@link #buildCategoryWrapperRecursively(CategoryEntity, Map, Executor)}
     *     operation in a {@link CompletableFuture} to enable parallel processing of category
     *     hierarchies. Each category and its subcategories are processed concurrently using the
     *     provided Virtual Thread Executor ({@code Executor} param).
     * </p>
     *
     * @param category root {@link CategoryEntity}.
     * @param childrenPyParent grouped subcategories by parent category ID.
     * @param executor Virtual Threads executor.
     * @return {@link CompletableFuture} containing {@link CategoryWrapperDto} as result
     * of the operation.
     * @see #buildCategoryWrapperRecursively(CategoryEntity, Map, Executor)
     * @see #buildCategoryTree(String, List)
     */
    private CompletableFuture<CategoryWrapperDto> toFuture(final CategoryEntity category,
                                                           final Map<Long, List<CategoryEntity>> childrenPyParent,
                                                           final Executor executor) {

        return CompletableFuture.supplyAsync(() ->
                buildCategoryWrapperRecursively(category, childrenPyParent, executor), executor
        );
    }

    /**
     * <p>
     *     {@link #getCategories(String)} fallback method when Time Limiter
     *     is invoked.
     * </p>
     *
     * <p>
     *     This method is automatically invoked by Resilience4j when
     *     circuit breaker for single category retrieval transitions to OPEN state
     *     or when other resilience mechanism (TimeLimiter, Retry) exhaust their attempts.
     *     This typically occurs during database outages, network timeouts or system degradation.
     * </p>
     * @param parentId the public parent ID of the category.
     * @param exception triggered by Circuit Breaker.
     * @return {@link CompletableFuture} containing empty {@link List},
     * indicating that the requested category could not be retrieved.
     * @apiNote This is an internal fallback mechanism - it should never be called directly.
     * @apiNote This method signature must be identical to {@link #getCategories(String)} to work
     * properly.
     */
    @SuppressWarnings("unused")
    private CompletableFuture<List<CategoryWrapperDto>> getCategoriesFallback(
            final String parentId, final Exception exception) {

        log.warn(
                "Circuit breaker activated:: ParentId: {}, Reason: {}",
                parentId, exception.getMessage(), exception
        );

        return CompletableFuture.supplyAsync(Collections::emptyList);
    }

    /**
     * <p>
     *     {@link #getCategory(String)} fallback method when Time Limiter
     *     is invoked.
     * </p>
     *
     * <p>
     *     This method is automatically invoked by Resilience4j when
     *     circuit breaker for single category retrieval transitions to OPEN state
     *     or when other resilience mechanism (TimeLimiter, Retry) exhaust their attempts.
     *     This typically occurs during database outages, network timeouts or system degradation.
     * </p>
     * @param categoryId the public ID of the category.
     * @param exception triggered by Circuit Breaker.
     * @return {@link CompletableFuture} containing empty {@link Optional},
     * indicating that the requested category could be retrieved.
     * @apiNote This is an internal fallback mechanism - it should never be called directly.
     * @apiNote This method signature must be identical to {@link #getCategory(String)} to work
     * properly.
     */
    @SuppressWarnings("unused")
    private CompletableFuture<Optional<CategoryWrapperDto>> getCategoryFallback(
            final String categoryId, final Exception exception) {

        log.warn(
                "Circuit breaker activated:: CategoryId: {}, Reason: {}",
                categoryId, exception.getMessage(), exception
        );

        return CompletableFuture.supplyAsync(Optional::empty);
    }

    /**
     * <p>
     *     {@link #save(CategoryEntityCreateDto)} fallback method when all
     *     retry attempts fail.
     * </p>
     *
     * <p>
     *     This method is automatically invoked by Spring Retry when
     *     {@link #save(CategoryEntityCreateDto)} exhausts all retry attempts.
     * </p>
     * @param exception the {@link DataIntegrityViolationException} that caused all
     *                  retry attempts to fail, typically due to unique constraint violation.
     * @param payload category data.
     * @throws IllegalStateException always thrown to indicate unrecoverable failure,
     * handled by {@code @RestControllerAdvice} for HTTP error response.
     * @return this method never returns normally - always throws {@link IllegalStateException}
     * @apiNote This method is an internal recovery mechanism - it should never be called directly.
     * @apiNote The method signature must be identical to {@link #save(CategoryEntityCreateDto)} to work
     * properly.
     * @see Recover
     * @see Retryable
     * @see #save(CategoryEntityCreateDto)
     */
    @Recover
    @SuppressWarnings("unused")
    String saveRecover(final Exception exception, final CategoryEntityCreateDto payload) {
        log.error(
                "Failed to save category after retries. Payload: {}, Exception: {}",
                payload,
                exception.getMessage()
        );

        throw new IllegalStateException("Unable to save category, try again after");
    }
}
