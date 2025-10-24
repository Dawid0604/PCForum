package pl.dawid0604.pcforum.thread.service.core;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
class ThreadCreatedEvent extends ThreadEvent {
    private final String categoryId;
    private final String threadTitle;
    private final String authorId;
}
