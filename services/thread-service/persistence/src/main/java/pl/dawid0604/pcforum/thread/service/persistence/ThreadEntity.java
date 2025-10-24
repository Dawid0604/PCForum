package pl.dawid0604.pcforum.thread.service.persistence;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.SEQUENCE;
import static lombok.AccessLevel.PACKAGE;

/**
 * Entity that represents Thread.
 *
 * @see lombok.Lombok
 * @see NanoIdUtils
 * @see AuditingEntityListener
 * @see EntityListeners
 */
@SqlResultSetMapping(
        name = "ThreadEntity.mapping.findAllByCategoryId",
        classes = {
                @ConstructorResult(
                        targetClass = CategoryThread.class,
                        columns = {
                                @ColumnResult(name = "id", type = long.class),
                                @ColumnResult(name = "publicId", type = String.class),
                                @ColumnResult(name = "title", type = String.class),
                                @ColumnResult(name = "userId", type = String.class),
                                @ColumnResult(name = "status", type = String.class),
                                @ColumnResult(name = "numberOfViews", type = long.class),
                                @ColumnResult(name = "createdDate", type = Instant.class),
                                @ColumnResult(name = "totalCount", type = long.class)
                        }
                )
        }
)
@SqlResultSetMapping(
        name = "ThreadEntity.mapping.findAllByUserId",
        classes = {
                @ConstructorResult(
                        targetClass = UserThread.class,
                        columns = {
                                @ColumnResult(name = "id", type = long.class),
                                @ColumnResult(name = "publicId", type = String.class),
                                @ColumnResult(name = "title", type = String.class),
                                @ColumnResult(name = "status", type = String.class),
                                @ColumnResult(name = "numberOfViews", type = long.class),
                                @ColumnResult(name = "createdDate", type = Instant.class),
                                @ColumnResult(name = "totalCount", type = long.class)
                        }
                )
        }
)
@SqlResultSetMapping(
        name = "ThreadEntity.mapping.findByPublicId",
        classes = {
                @ConstructorResult(
                        targetClass= ThreadDetails.class,
                        columns = {
                                @ColumnResult(name = "id", type = long.class),
                                @ColumnResult(name = "title", type = String.class),
                                @ColumnResult(name = "content", type = String.class),
                                @ColumnResult(name = "userId", type = String.class),
                                @ColumnResult(name = "status", type = String.class),
                                @ColumnResult(name = "createdDate", type = Instant.class)
                        }
                )
        }
)
@SqlResultSetMapping(
        name = "ThreadEntity.mapping.findAllByTitleAndContent",
        classes = {
                @ConstructorResult(
                        targetClass = MatchedThread.class,
                        columns = {
                                @ColumnResult(name = "id", type = long.class),
                                @ColumnResult(name = "publicId", type = String.class),
                                @ColumnResult(name = "title", type = String.class),
                                @ColumnResult(name = "content", type = String.class),
                                @ColumnResult(name = "userId", type = String.class),
                                @ColumnResult(name = "status", type = String.class),
                                @ColumnResult(name = "createdDate", type = Instant.class),
                                @ColumnResult(name = "totalCount", type = long.class)
                        }
                )
        }
)
@NamedNativeQuery(
        name = "ThreadEntity.query.findAllByCategoryId",
        resultSetMapping = "ThreadEntity.mapping.findAllByCategoryId",
        query = """
                    SELECT
                        t.id,
                        t.public_id as publicId,
                        t.title,
                        t.user_id as userId,
                        t.status,
                        t.number_of_views as numberOfViews,
                        t.created_date as createdDate,
                        COUNT(*) OVER() AS totalCount
                    FROM
                        threads t
                    WHERE
                        t.category_id = :categoryId AND
                        t.status IS DISTINCT FROM 'BANNED'
                    ORDER BY
                        t.created_date DESC
                    OFFSET :offset
                    LIMIT :limit
                """
)
@NamedNativeQuery(
        name = "ThreadEntity.query.findAllByUserId",
        resultSetMapping = "ThreadEntity.mapping.findAllByUserId",
        query = """
                    SELECT
                        t.id,
                        t.public_id as publicId,
                        t.title,
                        t.status,
                        t.number_of_views as numberOfViews,
                        t.created_date as createdDate,
                        COUNT(*) OVER() AS totalCount
                    FROM
                        threads t
                    WHERE
                        t.user_id = :userId
                    ORDER BY
                        t.created_date DESC
                    OFFSET :offset
                    LIMIT :limit
                """
)
@NamedNativeQuery(
        name = "ThreadEntity.query.findByPublicId",
        resultSetMapping = "ThreadEntity.mapping.findByPublicId",
        query = """
                    SELECT
                        t.id,
                        t.title,
                        t.content,
                        t.user_id as userId,
                        t.status,
                        t.number_of_views as numberOfViews,
                        t.created_date as createdDate
                    FROM
                        threads t
                    WHERE
                        t.public_id = :publicId
                """
)
@NamedNativeQuery(
        name = "ThreadEntity.query.findAllByTitleAndContent",
        resultSetMapping = "ThreadEntity.mapping.findAllByTitleAndContent",
        query = """
                SELECT
                    thread_id as id,
                    thread_public_id as publicId,
                    thread_title as title,
                    thread_user_id as userId,
                    thread_content as content,
                    thread_status as status,
                    thread_created_date as createdDate,
                    total_count as totalCount
                FROM
                    match_by_title_content(:query)
                WHERE
                    thread_status IS DISTINCT FROM 'BANNED'
                OFFSET :offset
                LIMIT :limit
                """
)
@Entity
@Getter
@Builder
@EqualsAndHashCode
@Table(name = "threads")
@NoArgsConstructor(access = PACKAGE)
@SuppressFBWarnings("EI_EXPOSE_REP2")
@AllArgsConstructor(access = PACKAGE)
@EntityListeners(AuditingEntityListener.class)
public class ThreadEntity {

    /**
     * <p>
     *     The unique thread ID, generated by sequence.
     * </p>
     */
    @Id
    @Setter(PACKAGE)
    @Column(
            name = "id",
            updatable = false
    )
    @GeneratedValue(
            strategy = SEQUENCE,
            generator = "thread_id_sequence"
    )
    @SequenceGenerator(
            name = "thread_id_sequence",
            sequenceName = "thread_id_sequence",
            allocationSize = 1
    )
    @SuppressWarnings("PMD.ShortVariable")
    private Long id;

    /**
     * <p>
     *     Unique ID for use outside the application.
     *     It's not updatable and cannot be nullable.
     * </p>
     */
    @Setter(PACKAGE)
    @Column(
            name = "public_id",
            unique = true,
            updatable = false,
            nullable = false
    )
    private String publicId;

    /**
     * <p>
     *     Thread title. It cannot be nullable.
     * </p>
     */
    @Setter
    @Column(
            name = "title",
            nullable = false
    )
    private String title;

    /**
     * <p>
     *     Thread content. It cannot be nullable.
     * </p>
     */
    @Setter
    @Column(
            name = "content",
            nullable = false
    )
    private String content;

    /**
     * <p>
     *     Thread categoryId. It's not updatable and
     *     cannot be nullable.
     * </p>
     */
    @Setter(PACKAGE)
    @Column(
            name = "category_id",
            updatable = false,
            nullable = false
    )
    private String categoryId;

    /**
     * <p>
     *     Thread creator. It's not updatable and
     *     cannot be nullable.
     * </p>
     */
    @Setter(PACKAGE)
    @Column(
            name = "user_id",
            updatable = false,
            nullable = false
    )
    private String userId;

    /**
     * <p>
     *     Thread status.
     * </p>
     */
    @Column(
            name = "status"
    )
    @Enumerated(STRING)
    private ThreadStatus status;

    @Column(
            name = "number_of_views",
            nullable = false
    )
    private long numberOfViews;

    /**
     * Auditing field that indicates founder.
     */
    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private String createdBy;

    /**
     * Auditing field that indicates user who modified this entity.
     */
    @LastModifiedBy
    @Column(name = "modified_by")
    private String modifiedBy;

    /**
     * Auditing field that indicates creation date.
     */
    @CreatedDate
    @Column(name = "created_date", updatable = false)
    private Instant createdDate;

    /**
     * Auditing field that indicates last modified date.
     */
    @LastModifiedDate
    @Column(name = "last_modified_date")
    private Instant lastModifiedDate;

    /**
     * Method to generate public_id if it's not present.
     */
    @PrePersist
    @SuppressWarnings("unused")
    void generatePublicId() {
        if (this.publicId == null) {
            this.publicId = NanoIdUtils.randomNanoId();
        }
    }
}
