package pl.dawid0604.pcforum.category.service.core;

import pl.dawid0604.pcforum.category.service.commons.dto.CategoryEntityCreateDto;
import pl.dawid0604.pcforum.category.service.commons.dto.CategoryWrapperDto;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * <p>
 *     Service interface for Categories.
 * </p>
 *
 * @see CategoryEntityServiceImpl
 */
public interface CategoryEntityService {

    /**
     * <p>
     *     Retrieves categories with their subcategories asynchronously.
     * </p>
     *
     * @param parentId public category parentId
     * @return {@link CompletableFuture} containing list of {@link CategoryWrapperDto}
     */
    CompletableFuture<List<CategoryWrapperDto>> getCategories(String parentId);

    /**
     * <p>
     *     Retrieves category asynchronously.
     * </p>
     *
     * @param categoryId public category ID
     * @return {@link CompletableFuture} containing {@link Optional} with possible {@link CategoryWrapperDto}
     */
    CompletableFuture<Optional<CategoryWrapperDto>> getCategory(String categoryId);

    /**
     * <p>
     *     Creates {@link pl.dawid0604.pcforum.category.service.persistence.CategoryEntity} and returns
     *     its public ID generated via {@link com.aventrix.jnanoid.jnanoid.NanoIdUtils}.
     * </p>
     *
     * @param payload category creation data
     * @return public ID saved {@link pl.dawid0604.pcforum.category.service.persistence.CategoryEntity}
     */
    String save(CategoryEntityCreateDto payload);
}
