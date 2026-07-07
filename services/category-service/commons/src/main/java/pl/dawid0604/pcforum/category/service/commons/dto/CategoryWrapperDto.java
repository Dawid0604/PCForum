package pl.dawid0604.pcforum.category.service.commons.dto;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.Collections;
import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

/**
 * Object to represents category with its subcategories.
 * @param category given category.
 * @param numberOfPosts data fetched from Posts microservice.
 * @param numberOfThreads data fetched from Threads microservice.
 * @param subCategories mapped subcategories of given category.
 */
@Schema(
        description = "Wrapper containing grouped categories"
)
@SuppressFBWarnings("EI_EXPOSE_REP")
public record CategoryWrapperDto(

        @Schema(
                description = "DTO representing category",
                requiredMode = REQUIRED
        )
        CategoryDto category,

        @Min(
                value = 0,
                message = "Number of posts cannot be negative"
        )
        @Schema(
                description = "Number of posts",
                requiredMode = REQUIRED
        )
        int numberOfPosts,

        @Min(
                value = 0,
                message = "Number of threads cannot be negative"
        )
        @Schema(
                description = "Number of threads",
                requiredMode = REQUIRED
        )
        int numberOfThreads,

        @NotNull(
                message = "Subcategories cannot be null"
        )
        @Schema(
                description = "List of subcategories",
                requiredMode = REQUIRED,
                defaultValue = "[]"
        )
        List<CategoryWrapperDto> subCategories) {

    /**
     * <p>
     *     Defensive subcategories copy.
     * </p>
     */
    public CategoryWrapperDto {
        subCategories = (subCategories != null) ? List.copyOf(subCategories)
                                                : Collections.emptyList();
    }

    /**
     * <p>
     *     Custom temporary constructor.
     * </p>
     *
     * @param incomingCategory given category.
     * @param incomingSubCategories mapped subcategories of given category.
     * @apiNote This constructor will be deleted in the future. Currently {@link #numberOfPosts}
     * and {@link #numberOfThreads} are set as 0 because the Posts and Threads are not created yet.
     */
    public CategoryWrapperDto(final CategoryDto incomingCategory,
                              final List<CategoryWrapperDto> incomingSubCategories) {

        this(incomingCategory, 0, 0, incomingSubCategories);
    }
}
