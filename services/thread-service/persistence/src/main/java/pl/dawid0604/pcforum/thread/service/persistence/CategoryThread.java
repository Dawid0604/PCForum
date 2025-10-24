package pl.dawid0604.pcforum.thread.service.persistence;

import java.time.Instant;

public record CategoryThread(
        long id,
        String publicId,
        String title,
        String userId,
        String status,
        long numberOfViews,
        Instant createdDate,
        long totalCount) {
}
