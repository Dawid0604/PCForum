package pl.dawid0604.pcforum.category.service.commons.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(
        description = "DTO representing a category"
)
public record CategoryDto(

        @NotBlank(
                message = Constants.PUBLIC_ID_NOT_BLANK_MESSAGE
        )
        @NotNull(
                message = Constants.PUBLIC_ID_NOT_NULL_MESSAGE
        )
        @Pattern(
                regexp = Constants.NANO_ID_REGEX,
                message = Constants.PUBLIC_ID_PATTERN_MESSAGE
        )
        @Schema(
                description = Constants.PUBLIC_ID_SCHEMA_DESCRIPTION,
                example = Constants.NANO_ID_EXAMPLE_MESSAGE,
                pattern = Constants.NANO_ID_REGEX,
                requiredMode = REQUIRED
        )
        String publicId,

        @NotBlank(
                message = Constants.NAME_NOT_BLANK_MESSAGE
        )
        @NotNull(
                message = Constants.NAME_NOT_NULL_MESSAGE
        )
        @Size(
                max = Constants.NAME_MAX_LENGTH,
                message = Constants.NAME_SIZE_MESSAGE
        )
        @Schema(
                description = Constants.NAME_SCHEMA_DESCRIPTION,
                example = Constants.NAME_EXAMPLE_MESSAGE,
                requiredMode = REQUIRED,
                maxLength = Constants.NAME_MAX_LENGTH
        )
        String name,

        @Size(
                max = Constants.DESCRIPTION_MAX_LENGTH,
                message = Constants.DESCRIPTION_SIZE_MESSAGE
        )
        @Schema(
                description = Constants.DESCRIPTION_SCHEMA_MESSAGE,
                example = Constants.DESCRIPTION_EXAMPLE_MESSAGE,
                requiredMode = NOT_REQUIRED,
                maxLength = Constants.DESCRIPTION_MAX_LENGTH
        )
        String description,

        @Pattern(
                regexp = Constants.ICON_PATH_REGEX,
                message = Constants.ICON_PATH_PATTERN_MESSAGE
        )
        @Schema(
                description = Constants.ICON_PATH_SCHEMA_DESCRIPTION,
                example = Constants.ICON_PATH_EXAMPLE,
                pattern = Constants.ICON_PATH_REGEX,
                requiredMode = NOT_REQUIRED
        )
        String iconPath) { }
