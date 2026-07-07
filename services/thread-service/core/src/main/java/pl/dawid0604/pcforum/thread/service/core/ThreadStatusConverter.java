package pl.dawid0604.pcforum.thread.service.core;

import lombok.NoArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import pl.dawid0604.pcforum.thread.service.persistence.ThreadStatus;

import static lombok.AccessLevel.PACKAGE;

@Component
@NoArgsConstructor(access = PACKAGE)
class ThreadStatusConverter {

    @Named("isPinned")
    @SuppressWarnings("unused")
    boolean isPinned(final ThreadStatus status) {
        return ThreadStatus.PINNED == status;
    }

    @Named("isBanned")
    @SuppressWarnings("unused")
    boolean isBanned(final ThreadStatus status) {
        return ThreadStatus.BANNED == status;
    }

    @Named("isClosed")
    @SuppressWarnings("unused")
    boolean isClosed(final ThreadStatus status) {
        return ThreadStatus.CLOSED == status;
    }
}