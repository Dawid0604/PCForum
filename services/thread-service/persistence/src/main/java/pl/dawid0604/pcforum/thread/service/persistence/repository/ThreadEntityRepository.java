package pl.dawid0604.pcforum.thread.service.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.dawid0604.pcforum.thread.service.persistence.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ThreadEntityRepository extends JpaRepository<ThreadEntity, Long> {

    @Query(name = "ThreadEntity.query.findAllByCategoryId", nativeQuery = true)
    List<CategoryThread> findAllByCategoryId(
            @Param("categoryId") String categoryId,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    @Query(name = "ThreadEntity.query.findAllByUserId", nativeQuery = true)
    List<UserThread> findAllByUserId(
            @Param("userId") String userId,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    @Query(name = "ThreadEntity.query.findAllByTitleAndContent", nativeQuery = true)
    List<MatchedThread> findAllByTitleAndContent(
            @Param("query") String query,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    @Query(name = "ThreadEntity.query.findByPublicId", nativeQuery = true)
    Optional<ThreadDetails> findByPublicId(@Param("publicId") String publicId);

    @Modifying(clearAutomatically = true)
    @Query("""
                UPDATE #{#entityName} t
                SET t.numberOfViews = t.numberOfViews + 1
                WHERE t.publicId = :publicId
           """)
    int updateNumberOfViews(@Param("publicId") String publicId);

    @Query("SELECT t FROM #{#entityName} t WHERE t.publicId = :publicId")
    Optional<ThreadEntity> findEntityByPublicId(@Param("publicId") String publicId);

    long countByCategoryId(String categoryId);

    @Query("""
               SELECT CASE WHEN COUNT(t) > 0 THEN TRUE ELSE FALSE END
               FROM #{#entityName} t
               WHERE t.publicId = :publicId
           """)
    boolean existsByPublicIdAndUserId(String publicId, String userId);

    @Modifying(clearAutomatically = true)
    @Query("""
               DELETE FROM #{#entityName} t
               WHERE t.publicId = :publicId
           """)
    int deleteByPublicId(String publicId);

    @Modifying(clearAutomatically = true)
    @Query("""
                UPDATE #{#entityName} t
                SET t.status = :status
                WHERE t.publicId = :publicId
           """)
    int updateStatus(String publicId, ThreadStatus status);

    @Query(
            value = """
                        SELECT t.created_date
                        FROM threads t
                        WHERE t.user_id = :userId
                        ORDER BY t.created_date DESC
                        LIMIT 1
                        FOR UPDATE
                    """,
            nativeQuery = true
    )
    Optional<Instant> findLastThreadCreationTime(String userId);

    @Query(
            value = """
                        SELECT t.last_modified_date
                        FROM threads t
                        WHERE t.user_id = :userId
                        ORDER BY t.last_modified_date DESC
                        LIMIT 1
                        FOR UPDATE
                    """,
            nativeQuery = true
    )
    Optional<Instant> findLastThreadModifiedTime(String userId);
}
