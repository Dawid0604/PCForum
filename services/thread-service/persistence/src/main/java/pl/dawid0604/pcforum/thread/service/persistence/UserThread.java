package pl.dawid0604.pcforum.thread.service.persistence;

import java.time.Instant;

public record UserThread(
        long id,
        String publicId,
        String title,
        String status,
        long numberOfViews,
        Instant createdDate,
        long totalCount) {
}
