package pl.dawid0604.pcforum.thread.service.core;

import lombok.NoArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static lombok.AccessLevel.PACKAGE;

@Component
@NoArgsConstructor(access = PACKAGE)
class ThreadDateTimeConverter {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
                                                                                  .withZone(ZoneId.systemDefault());

    @Named("instantToString")
    @SuppressWarnings("unused")
    String instantToString(final Instant instant) {
        return Optional.ofNullable(instant)
                       .map(DATE_TIME_FORMATTER::format)
                       .orElse(null);
    }
}
