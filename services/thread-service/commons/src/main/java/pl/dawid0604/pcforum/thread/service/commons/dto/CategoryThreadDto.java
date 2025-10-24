package pl.dawid0604.pcforum.thread.service.commons.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record CategoryThreadDto(
        String publicId,
        String title,
        String userId,
        boolean isPinned,
        boolean isClosed,
        long numberOfViews,
        String createdDate,

        @JsonIgnore
        long totalCount) { }
