package pl.dawid0604.pcforum.category.service.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * {@link CategoryEntity} repository implementation.
 *
 * @see CategoryEntity
 * @see JpaRepository
 */
@Repository
public interface CategoryEntityRepository extends JpaRepository<CategoryEntity, Long> {

    /**
     * <p>
     *     Native query using {@link jakarta.persistence.NamedNativeQuery}.
     * </p>
     *
     * @param categoryId as parentId
     * @return {@link List} of {@link CategoryEntity} containing {@code categoryId} as
     * parent.
     * @see CategoryEntity
     * @see jakarta.persistence.NamedNativeQuery
     */
    @Query(name = "CategoryEntity.findByParentId", nativeQuery = true)
    List<CategoryEntity> findByParentId(String categoryId);

    /**
     * <p>
     *     Native query using {@link jakarta.persistence.NamedNativeQuery}.
     * </p>
     *
     * @return {@link List} of all {@link CategoryEntity}
     * parent.
     * @see CategoryEntity
     * @see jakarta.persistence.NamedNativeQuery
     */
    @Query(name = "CategoryEntity.findAllCustom", nativeQuery = true)
    List<CategoryEntity> findAllCustom();

    /**
     * <p>
     *     Query to find {@link CategoryEntity} by its {@code publicId} field.
     * </p>
     * @param publicId generated via {@link com.aventrix.jnanoid.jnanoid.NanoIdUtils}
     * @return {@link Optional} containing {@link CategoryEntity}
     */
    Optional<CategoryEntity> findByPublicId(String publicId);
}
