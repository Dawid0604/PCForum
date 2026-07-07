package pl.dawid0604.pcforum.thread.service.core;

import org.springframework.data.domain.Page;
import pl.dawid0604.pcforum.thread.service.commons.dto.*;

import java.util.concurrent.CompletableFuture;

public interface ThreadEntityService {

    CompletableFuture<Long> count(String categoryId);

    CompletableFuture<ThreadDetailsDto> findDetails(String publicId);

    CompletableFuture<Page<CategoryThreadDto>> findAllByCategoryId(String categoryId, int page, int size);

    CompletableFuture<Page<UserThreadDto>> findAllByUserId(String userId, int page, int size);

    CompletableFuture<Page<MatchedThreadDto>> findAllByTitleAndContent(String query, int page, int size);

    CompletableFuture<String> create(ThreadEntityDto payload, String idempotencyKey);

    CompletableFuture<String> edit(ThreadEntityDto payload);

    CompletableFuture<Void> incrementNumberOfViews(String publicId);

    CompletableFuture<Void> delete(String publicId);

    CompletableFuture<Void> pin(String publicId);

    CompletableFuture<Void> ban(String publicId);

    CompletableFuture<Void> close(String publicId);
}
