package pl.dawid0604.pcforum.thread.service.core;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.dawid0604.pcforum.thread.service.commons.dto.ThreadEntityDto;
import pl.dawid0604.pcforum.thread.service.persistence.ThreadEntity;
import pl.dawid0604.pcforum.thread.service.persistence.ThreadStatus;

import java.util.Objects;

import static lombok.AccessLevel.PACKAGE;

@Component
@RequiredArgsConstructor(access = PACKAGE)
class ThreadEventFactory {

    ThreadCreatedEvent createThreadCreatedEvent(final String publicId, final ThreadEntity threadEntity,
                                                final ThreadEntityDto payload) {

        Objects.requireNonNull(publicId, "PublicId cannot be null");
        Objects.requireNonNull(threadEntity, "ThreadEntity cannot be null");
        Objects.requireNonNull(payload, "Payload cannot be null");

        Objects.requireNonNull(threadEntity.getUserId(), "UserId cannot be null");
        Objects.requireNonNull(payload.categoryId(), "CategoryId cannot be null");
        Objects.requireNonNull(payload.title(), "Title cannot be null");

        return ThreadCreatedEvent.builder()
                                 .threadId(publicId)
                                 .authorId(threadEntity.getUserId())
                                 .categoryId(payload.categoryId())
                                 .threadTitle(payload.title())
                                 .build();
    }

    ThreadStatusChangedEvent createThreadStatusChangedEvent(final String publicId, final ThreadStatus newStatus) {
        Objects.requireNonNull(publicId, "PublicId cannot be null");
        Objects.requireNonNull(newStatus, "NewStatus cannot be null");

        return ThreadStatusChangedEvent.builder()
                                       .threadId(publicId)
                                       .newStatus(newStatus)
                                       .build();
    }
}
