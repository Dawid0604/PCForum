package pl.dawid0604.pcforum.thread.service.commons.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record MatchedThreadDto(
        String publicId,
        String title,
        String content,
        String userId,
        boolean isClosed,
        boolean isPinned,
        String createdDate,

        @JsonIgnore
        long totalCount) { }
