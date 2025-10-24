package pl.dawid0604.pcforum.thread.service.web;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import pl.dawid0604.pcforum.thread.service.commons.Constants;
import pl.dawid0604.pcforum.thread.service.commons.RequestContext;
import pl.dawid0604.pcforum.thread.service.commons.annotation.*;
import pl.dawid0604.pcforum.thread.service.commons.dto.*;
import pl.dawid0604.pcforum.thread.service.commons.json.DtoViews;
import pl.dawid0604.pcforum.thread.service.core.ThreadEntityService;

import java.net.URI;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

@Validated
@RestController
@SecurityRequirement(name = "security_auth")
class ThreadRestController {
    private final ThreadEntityService threadEntityService;
    private final String gatewayServiceUrl;
    private final RequestContext requestContext;

    ThreadRestController(

            final ThreadEntityService threadEntityService,
            final RequestContext requestContext,

            @Value("${custom.gatewayServiceUrl}")
            final String gatewayServiceUrl) {

        this.threadEntityService = threadEntityService;
        this.gatewayServiceUrl = gatewayServiceUrl;
        this.requestContext = requestContext;
    }

    @GetMapping("/category")
    CompletableFuture<ResponseEntity<Page<CategoryThreadDto>>> findAllByCategoryId(

            @Size(
                    min = Constants.NANO_ID_LENGTH,
                    max = Constants.NANO_ID_LENGTH,
                    message = Constants.NANO_ID_MESSAGE
            )
            @Parameter(example = Constants.SCHEMA_NANO_ID_EXAMPLE)
            @RequestParam
            final String categoryId,

            @PositiveOrZero(message = Constants.NEGATIVE_VALUE_MESSAGE)
            @Parameter(
                    description = "Number of page",
                    example = "0"
            )
            @RequestParam(defaultValue = "0")
            final int page,

            @Min(
                    value = 25,
                    message = "Value must be greater or equal than 25"
            )
            @Parameter(
                    description = "Number of threads at page",
                    example = "50"
            )
            @RequestParam(defaultValue = "25")
            final int size) {

        return threadEntityService.findAllByCategoryId(categoryId, page, size)
                                  .thenApply(this::pageToResponseEntity);
    }

    @GetMapping("/user")
    CompletableFuture<ResponseEntity<Page<UserThreadDto>>> findAllByUserId(

            @Size(
                    min = Constants.NANO_ID_LENGTH,
                    max = Constants.NANO_ID_LENGTH,
                    message = Constants.NANO_ID_MESSAGE
            )
            @Parameter(
                    example = Constants.SCHEMA_NANO_ID_EXAMPLE
            )
            @RequestParam
            final String userId,

            @PositiveOrZero(message = Constants.NEGATIVE_VALUE_MESSAGE)
            @Parameter(
                    description = "Number of page",
                    example = "0"
            )
            @RequestParam(defaultValue = "0")
            final int page,

            @Min(
                    value = 25,
                    message = "Value must be greater or equal than 25"
            )
            @Parameter(
                    description = "Number of threads at page",
                    example = "50"
            )
            @RequestParam(defaultValue = "25")
            final int size) {

        return threadEntityService.findAllByUserId(userId, page, size)
                                  .thenApply(this::pageToResponseEntity);
    }

    @GetMapping("/match")
    CompletableFuture<ResponseEntity<Page<MatchedThreadDto>>> findAllByQuery(

            @Size(
                    min = 2,
                    message = "Value should be greater than or equal 2 characters"
            )
            @Parameter(example = "Intel")
            @RequestParam
            final String query,

            @PositiveOrZero(message = Constants.NEGATIVE_VALUE_MESSAGE)
            @Parameter(
                    description = "Number of page",
                    example = "0"
            )
            @RequestParam(defaultValue = "0")
            final int page,

            @Min(
                    value = 25,
                    message = "Value must be greater or equal than 25"
            )
            @Parameter(
                    description = "Number of threads at page",
                    example = "50"
            )
            @RequestParam(defaultValue = "25")
            final int size) {

        return threadEntityService.findAllByTitleAndContent(query, page, size)
                                  .thenApply(this::pageToResponseEntity);
    }

    @GetMapping("/count/{categoryId}")
    CompletableFuture<ResponseEntity<Long>> countByCategoryId(

            @Size(
                    min = Constants.NANO_ID_LENGTH,
                    max = Constants.NANO_ID_LENGTH,
                    message = Constants.NANO_ID_MESSAGE
            )
            @Parameter(example = Constants.SCHEMA_NANO_ID_EXAMPLE)
            @PathVariable
            final String categoryId) {

        return threadEntityService.count(categoryId)
                                  .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/details/{publicId}")
    CompletableFuture<ResponseEntity<ThreadDetailsDto>> findThreadByPublicId(

            @Size(
                    min = Constants.NANO_ID_LENGTH,
                    max = Constants.NANO_ID_LENGTH,
                    message = Constants.NANO_ID_MESSAGE
            )
            @Parameter(example = Constants.SCHEMA_NANO_ID_EXAMPLE)
            @PathVariable
            final String publicId) {

        return threadEntityService.findDetails(publicId)
                                  .thenApply(ResponseEntity::ok);
    }

    @PostMapping
    @RequireAuthentication
    @RequireCreationPermission(interval = 15)
    CompletableFuture<ResponseEntity<Void>> create(

            @SuppressWarnings("unused")
            @RequestHeader(Constants.IDEMPOTENCY_KEY_HEADER)
            final String idempotencyKey,

            @Validated
            @RequestBody
            @JsonView(DtoViews.Creation.class)
            final ThreadEntityDto payload) {

        return threadEntityService.create(payload, requestContext.getIdempotencyKey())
                                  .thenApply(publicId -> toResponseWithLocation(publicId, HttpStatus.CREATED));
    }

    @PutMapping
    @RequireAuthentication
    @RequireEditPermission(interval = 15)
    @RequireOwnership(identifier = "publicId")
    CompletableFuture<ResponseEntity<Void>> edit(

            @Validated
            @RequestBody
            @JsonView(DtoViews.Edit.class)
            final ThreadEntityDto payload) {

        return threadEntityService.edit(payload)
                                  .thenApply(publicId -> toResponseWithLocation(publicId, HttpStatus.OK));
    }

    @RequireModeratorAdminRole
    @PatchMapping("/pin/{publicId}")
    @RequireOwnership(identifier = "publicId")
    CompletableFuture<ResponseEntity<Void>> pin(

            @Size(
                    min = Constants.NANO_ID_LENGTH,
                    max = Constants.NANO_ID_LENGTH,
                    message = Constants.NANO_ID_MESSAGE
            )
            @Parameter(example = Constants.SCHEMA_NANO_ID_EXAMPLE)
            @PathVariable
            final String publicId) {

        return threadEntityService.pin(publicId)
                                  .thenApply(v -> new ResponseEntity<>(HttpStatus.NO_CONTENT));
    }

    @RequireModeratorAdminRole
    @PatchMapping("/ban/{publicId}")
    @RequireOwnership(identifier = "publicId")
    CompletableFuture<ResponseEntity<Void>> ban(

            @Size(
                    min = Constants.NANO_ID_LENGTH,
                    max = Constants.NANO_ID_LENGTH,
                    message = Constants.NANO_ID_MESSAGE
            )
            @Parameter(example = Constants.SCHEMA_NANO_ID_EXAMPLE)
            @PathVariable
            final String publicId) {

        return threadEntityService.ban(publicId)
                                  .thenApply(v -> new ResponseEntity<>(HttpStatus.NO_CONTENT));
    }

    @RequireAuthentication
    @PatchMapping("/close/{publicId}")
    @RequireOwnership(identifier = "publicId")
    CompletableFuture<ResponseEntity<Void>> close(

            @Size(
                    min = Constants.NANO_ID_LENGTH,
                    max = Constants.NANO_ID_LENGTH,
                    message = Constants.NANO_ID_MESSAGE
            )
            @Parameter(example = Constants.SCHEMA_NANO_ID_EXAMPLE)
            @PathVariable
            final String publicId) {

        return threadEntityService.close(publicId)
                                  .thenApply(v -> new ResponseEntity<>(HttpStatus.NO_CONTENT));
    }

    @RequireAuthentication
    @PatchMapping("/views/{publicId}")
    CompletableFuture<ResponseEntity<Void>> incrementNumberOfViews(

            @Size(
                    min = Constants.NANO_ID_LENGTH,
                    max = Constants.NANO_ID_LENGTH,
                    message = Constants.NANO_ID_MESSAGE
            )
            @Parameter(example = Constants.SCHEMA_NANO_ID_EXAMPLE)
            @PathVariable
            final String publicId) {

        return threadEntityService.incrementNumberOfViews(publicId)
                                  .thenApply(v -> new ResponseEntity<>(HttpStatus.NO_CONTENT));
    }

    @RequireAuthentication
    @RequireDeletePermission
    @DeleteMapping("/{publicId}")
    @RequireOwnership(identifier = "publicId")
    CompletableFuture<ResponseEntity<Void>> delete(
            @Size(
                    min = Constants.NANO_ID_LENGTH,
                    max = Constants.NANO_ID_LENGTH,
                    message = Constants.NANO_ID_MESSAGE
            )
            @Parameter(example = Constants.SCHEMA_NANO_ID_EXAMPLE)
            @PathVariable
            final String publicId) {

        return threadEntityService.delete(publicId)
                                  .thenApply(v -> new ResponseEntity<>(HttpStatus.NO_CONTENT));
    }

    private ResponseEntity<Void> toResponseWithLocation(final String publicId, final HttpStatus status) {
        final URI resourceLocation = UriComponentsBuilder.fromUriString(gatewayServiceUrl)
                                                         .path("/thread/details/{publicId}")
                                                         .buildAndExpand(publicId)
                                                         .toUri();

        return ResponseEntity.status(status)
                             .location(resourceLocation)
                             .build();
    }

    private <T> ResponseEntity<Page<T>> pageToResponseEntity(final Page<T> page) {
        return Optional.ofNullable(page)
                       .filter(Predicate.not(Page::isEmpty))
                       .map(ResponseEntity::ok)
                       .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
