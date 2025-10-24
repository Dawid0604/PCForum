package pl.dawid0604.pcforum.thread.service.core;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import pl.dawid0604.pcforum.thread.service.persistence.ThreadStatus;

@Getter
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
class ThreadStatusChangedEvent extends ThreadEvent {
    private final ThreadStatus newStatus;
}
