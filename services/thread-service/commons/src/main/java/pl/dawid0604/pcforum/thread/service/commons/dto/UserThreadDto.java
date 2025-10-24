package pl.dawid0604.pcforum.thread.service.commons.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record UserThreadDto(
        String publicId,
        String title,
        boolean isPinned,
        boolean isBanned,
        boolean isClosed,
        long numberOfViews,
        String createdDate,

        @JsonIgnore
        long totalCount) { }
