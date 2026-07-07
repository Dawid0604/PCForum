package pl.dawid0604.pcforum.thread.service.persistence;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.SEQUENCE;
import static lombok.AccessLevel.PACKAGE;

@Entity
@Getter
@Builder
@Setter(PACKAGE)
@EqualsAndHashCode
@Table(name = "idempotency_keys")
@NoArgsConstructor(access = PACKAGE)
@AllArgsConstructor(access = PACKAGE)
public class IdempotencyKeyEntity {

    @Id
    @Column(
            name = "id",
            updatable = false
    )
    @GeneratedValue(
            strategy = SEQUENCE,
            generator = "idempotency_keys_sequence"
    )
    @SequenceGenerator(
            name = "idempotency_keys_sequence",
            sequenceName =  "idempotency_keys_sequence",
            allocationSize = 1
    )
    @SuppressWarnings("PMD.ShortVariable")
    private Long id;

    @Column(
            name = "idempotency_key",
            unique = true,
            updatable = false,
            nullable = false
    )
    private String idempotencyKey;

    @Column(
            name = "request_hash",
            updatable = false,
            nullable = false
    )
    private String requestHash;

    @Column(
            name = "result_data"
    )
    private String resultData;

    @Enumerated(STRING)
    @Column(
            name = "status",
            nullable = false
    )
    private IdempotencyStatus status;

    @Column(
            name = "created_at",
            nullable = false
    )
    private Instant createdAt;

    @Column(
            name = "updated_at",
            nullable = false,
            updatable = false
    )
    private Instant updatedAt;

    @Column(
            name = "expires_at",
            nullable = false
    )
    private Instant expiresAt;
}
