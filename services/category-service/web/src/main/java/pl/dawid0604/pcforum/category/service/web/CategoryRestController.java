package pl.dawid0604.pcforum.category.service.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.dawid0604.pcforum.category.service.commons.Constants;
import pl.dawid0604.pcforum.category.service.commons.dto.CategoryEntityCreateDto;
import pl.dawid0604.pcforum.category.service.commons.dto.CategoryWrapperDto;
import pl.dawid0604.pcforum.category.service.core.CategoryEntityService;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static java.util.function.Predicate.not;
import static lombok.AccessLevel.PACKAGE;

/**
 * Rest controller for Categories.
 *
 * @see RestController
 * @see SecurityRequirement
 */
@Validated
@RestController
@SuppressWarnings("unused")
@RequiredArgsConstructor(access = PACKAGE)
@SecurityRequirement(name = "security_auth")
class CategoryRestController {

    /**
     * The category service to serve data.
     */
    private final CategoryEntityService categoryEntityService;

    /**
     * Retrieves a list of categories.
     * @param parentId optional parent category ID to get children categories.
     * @return Status HTTP 200 (OK) with category list or HTTP 204 (No content) if none found.
     */
    @GetMapping("/all")
    @Operation(
            summary = "Get categories",
            description = """
                        Retrieves a list of categories.
                        Optionally filters by Parent category ID.
                    """
    )
    @ApiResponse(responseCode = "200", description = "Categories found")
    @ApiResponse(responseCode = "204", description = "Categories not found")
    CompletableFuture<ResponseEntity<List<CategoryWrapperDto>>> getCategories(

            @Size(
                    min = Constants.NANO_ID_LENGTH,
                    max = Constants.NANO_ID_LENGTH,
                    message = "ID should have 21 characters."
            )
            @RequestParam(
                    value = "parentId",
                    required = false
            )
            @Parameter(
                    description = "Optional parent category ID",
                    example = "abc123def456ghi789jkl"
            )
            final String parentId) {

        return categoryEntityService.getCategories(parentId)
                                    .thenApply(this::mapToResponse);
    }

    /**
     * Retrieves a single category by specified public ID.
     * After category creation via {@link #create(CategoryEntityCreateDto)}
     * this endpoint is used as URI Header location.
     * @param categoryId public entity NanoID.
     * @return Status HTTP 200 (OK) with category or HTTP 404 (Not found) if none found.
     * @see #create(CategoryEntityCreateDto)
     * @see com.aventrix.jnanoid.jnanoid.NanoIdUtils
     */
    @GetMapping("/{categoryId}")
    @Operation(
            summary = "Get a single category",
            description = "Retrieves a single category by public ID"
    )
    @ApiResponse(responseCode = "200", description = "Category found")
    @ApiResponse(responseCode = "404", description = "Category not found")
    CompletableFuture<ResponseEntity<CategoryWrapperDto>> getCategory(

            @Size(
                    min = Constants.NANO_ID_LENGTH,
                    max = Constants.NANO_ID_LENGTH,
                    message = "ID should have 21 characters."
            )
            @PathVariable(value = "categoryId")
            @Parameter(
                    description = "Category ID",
                    example = "abc123def456ghi789jkl"
            )
            final String categoryId) {

        return categoryEntityService.getCategory(categoryId)
                                    .thenApply(c -> c.map(ResponseEntity::ok)
                                                     .orElse(ResponseEntity.notFound().build()));
    }

    /**
     * Category creation. This endpoint is accessible only for
     * users with Admin and Moderator role.
     * @param payload category data.
     * @return Status HTTP 201 (Created) created category with
     * URI Header location that indicates {@link #getCategory(String)} method.
     * @see #getCategory(String)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Operation(
            summary = "Category creation",
            description = "Create a category entity."
    )
    @ApiResponse(responseCode = "201", description = "Successful category creation.")
    ResponseEntity<Void> create(

            @Validated
            @RequestBody
            final CategoryEntityCreateDto payload) {

        final String publicCategoryId = categoryEntityService.save(payload);
        final URI resourceLocation = ServletUriComponentsBuilder.fromCurrentRequest()
                                                                .path("/{categoryId}")
                                                                .buildAndExpand(publicCategoryId)
                                                                .toUri();

        return ResponseEntity.created(resourceLocation)
                             .build();
    }

    /**
     * <p>
     *     Utils method to map categories into proper {@link ResponseEntity} object.
     * </p>
     *
     * @param categories incoming result
     * @return If {@code categories} parameter is present then is returned {@link ResponseEntity}
     *         with status {@link org.springframework.http.HttpStatus#OK}, otherwise
     *         the same object with status {@link org.springframework.http.HttpStatus#NO_CONTENT}
     */
    private ResponseEntity<List<CategoryWrapperDto>> mapToResponse(final List<CategoryWrapperDto> categories) {
        return Optional.ofNullable(categories)
                       .filter(not(List::isEmpty))
                       .map(ResponseEntity::ok)
                       .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
