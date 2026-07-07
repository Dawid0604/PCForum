package pl.dawid0604.pcforum.thread.service.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.dawid0604.pcforum.thread.service.persistence.IdempotencyKeyEntity;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface IdempotencyKeyEntityRepository extends JpaRepository<IdempotencyKeyEntity, Long> {

    @Modifying(flushAutomatically = true)
    @Query(
            nativeQuery = true,
            value = """
                        INSERT INTO idempotency_keys (
                            idempotency_key, request_hash, expires_at
                        )
                        VALUES (
                            :key, :hash, :expiresAt
                        )
                        ON CONFLICT (idempotency_key)
                        DO NOTHING
                    """
    )
    void insertOrGet(
            @Param("key")
            String idempotentKey,

            @Param("hash")
            String hash,

            @Param("expiresAt")
            Instant expiresAt
    );

    @Modifying(flushAutomatically = true)
    @Query("""
                UPDATE #{#entityName}
                SET status = 'SUCCESS',
                    resultData = :result
                WHERE idempotencyKey = :key
            """)
    void markAsSuccess(
            @Param("key")
            String idempotentKey,

            @Param("result")
            String resultData
    );

    @Modifying(flushAutomatically = true)
    @Query("""
                UPDATE #{#entityName}
                SET status = 'FAILED'
                WHERE idempotencyKey = :key
            """)
    void markAsFailed(
            @Param("key")
            String idempotentKey
    );

    @Modifying(flushAutomatically = true)
    @Query("""
                DELETE FROM #{#entityName} ik
                WHERE ik.expiresAt <= :now AND
                      ik.status = 'SUCCESS'
           """)
    int deleteExpired(
            @Param("now")
            Instant now
    );

    @Query("""
            SELECT ik
            FROM #{#entityName} ik
            WHERE ik.idempotencyKey = :key AND
                  ik.expiresAt > :now
           """)
    Optional<IdempotencyKeyEntity> findValidByKey(
            @Param("key")
            String idempotencyKey,

            @Param("now")
            Instant now
    );
}
