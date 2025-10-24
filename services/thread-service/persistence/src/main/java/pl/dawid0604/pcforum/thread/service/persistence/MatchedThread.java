package pl.dawid0604.pcforum.thread.service.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.Instant;

public record MatchedThread(
        long id,
        String publicId,
        String title,
        String userId,
        String content,
        String status,
        Instant createdDate,

        @JsonIgnore
        long totalCount) { }
