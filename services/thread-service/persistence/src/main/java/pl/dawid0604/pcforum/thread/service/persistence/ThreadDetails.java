package pl.dawid0604.pcforum.thread.service.persistence;

import java.time.Instant;

public record ThreadDetails(
        long id,
        String title,
        String content,
        String userId,
        String status,
        Instant createdDate) { }
