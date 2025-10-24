package pl.dawid0604.pcforum.thread.service.commons.dto;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import pl.dawid0604.pcforum.thread.service.commons.Constants;
import pl.dawid0604.pcforum.thread.service.commons.annotation.ValidContent;
import pl.dawid0604.pcforum.thread.service.commons.json.DtoViews;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "DTO representing Thread")
public record ThreadEntityDto(

        @ValidContent(fieldName = "Title")
        @NotNull(message = Constants.TITLE_NOT_NULL_MESSAGE)
        @NotBlank(message = Constants.TITLE_NOT_BLANK_MESSAGE)
        @Size(
                min = Constants.TITLE_MIN_SIZE,
                max = Constants.TITLE_MAX_SIZE,
                message = Constants.TITLE_SIZE_MESSAGE
        )
        @Schema(
                description = Constants.TITLE_SCHEMA_DESCRIPTION,
                example = Constants.TITLE_SCHEMA_EXAMPLE,
                requiredMode = REQUIRED,
                minLength = Constants.TITLE_MIN_SIZE,
                maxLength = Constants.TITLE_MAX_SIZE
        )
        @JsonView(DtoViews.Common.class)
        String title,

        @ValidContent(fieldName = "Content")
        @NotNull(message = Constants.CONTENT_NOT_NULL_MESSAGE)
        @NotBlank(message = Constants.CONTENT_NOT_BLANK_MESSAGE)
        @Size(
                min = Constants.CONTENT_MIN_SIZE,
                message = Constants.CONTENT_SIZE_MESSAGE
        )
        @Schema(
                description = Constants.CONTENT_SCHEMA_DESCRIPTION,
                example = Constants.CONTENT_SCHEMA_EXAMPLE,
                requiredMode = REQUIRED,
                minLength = Constants.CONTENT_MIN_SIZE
        )
        @JsonView(DtoViews.Common.class)
        String content,

        @Pattern(
                regexp = Constants.NANO_ID_REGEXP,
                message = Constants.NANO_ID_MESSAGE
        )
        @Schema(
                description = Constants.SCHEMA_NANO_ID_DESCRIPTION,
                example = Constants.SCHEMA_NANO_ID_EXAMPLE,
                pattern = Constants.NANO_ID_REGEXP,
                requiredMode = REQUIRED
        )
        @JsonView(DtoViews.Creation.class)
        String categoryId,

        @Pattern(
                regexp = Constants.NANO_ID_REGEXP,
                message = Constants.NANO_ID_MESSAGE
        )
        @Schema(
                description = Constants.SCHEMA_NANO_ID_DESCRIPTION,
                example = Constants.SCHEMA_NANO_ID_EXAMPLE,
                pattern = Constants.NANO_ID_REGEXP,
                requiredMode = REQUIRED
        )
        @JsonView(DtoViews.Edit.class)
        String publicId) { }
