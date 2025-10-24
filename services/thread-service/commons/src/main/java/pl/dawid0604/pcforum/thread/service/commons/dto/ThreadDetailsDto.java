package pl.dawid0604.pcforum.thread.service.commons.dto;

public record ThreadDetailsDto(
        String title,
        String content,
        String userId,
        boolean isBanned,
        boolean isClosed,
        String createdDate) { }
