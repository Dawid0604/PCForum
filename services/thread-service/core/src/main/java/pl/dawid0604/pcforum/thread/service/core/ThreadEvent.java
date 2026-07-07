package pl.dawid0604.pcforum.thread.service.core;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

import static lombok.AccessLevel.PROTECTED;

@Getter
@SuperBuilder
@EqualsAndHashCode
@RequiredArgsConstructor(access = PROTECTED)
abstract class ThreadEvent {
    private final String eventId = UUID.randomUUID().toString();
    private final String threadId;

    @Builder.Default
    private String schemaVersion = "1.0";

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final Instant timestamp = Instant.now();
}
