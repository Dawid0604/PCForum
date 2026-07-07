package pl.dawid0604.pcforum.thread.service.commons.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import pl.dawid0604.pcforum.thread.service.commons.Constants;
import pl.dawid0604.pcforum.thread.service.commons.view.DtoView;

@Schema(description = "DTO representing single thread")
public record ThreadDto(

        @Schema(
                description = Constants.SCHEMA_NANO_ID_DESCRIPTION,
                example = Constants.SCHEMA_NANO_ID_EXAMPLE,
                pattern = Constants.NANO_ID_REGEXP
        )
        @JsonView(DtoView.Common.class)
        String publicId,

        @Schema(
                description = Constants.TITLE_SCHEMA_DESCRIPTION,
                example = Constants.TITLE_SCHEMA_EXAMPLE,
                minLength = Constants.TITLE_MIN_LENGTH,
                maxLength = Constants.TITLE_MAX_LENGTH
        )
        @JsonView(DtoView.Common.class)
        String title,

        @Schema(
                description = Constants.CONTENT_SCHEMA_DESCRIPTION,
                example = Constants.CONTENT_SCHEMA_EXAMPLE,
                minLength = Constants.CONTENT_MIN_LENGTH
        )
        @JsonView({
                DtoView.Matching.class,
                DtoView.Details.class
        })
        String content,

        @Schema(
                description = Constants.USER_SCHEMA_DESCRIPTION,
                example = Constants.SCHEMA_NANO_ID_EXAMPLE,
                pattern = Constants.NANO_ID_REGEXP
        )
        @JsonView({
                DtoView.Matching.class,
                DtoView.Category.class
        })
        String userId,

        @Schema(
                description = Constants.IS_PINNED_SCHEMA_DESCRIPTION,
                example = Constants.BOOLEAN_EXAMPLE_SCHEMA_VALUE
        )
        @JsonView(DtoView.Common.class)
        boolean isPinned,

        @Schema(
                description = Constants.IS_BANNED_SCHEMA_DESCRIPTION,
                example = Constants.BOOLEAN_EXAMPLE_SCHEMA_VALUE
        )
        @JsonView({
                DtoView.User.class,
                DtoView.Details.class
        })
        boolean isBanned,

        @Schema(
                description = Constants.IS_CLOSED_SCHEMA_DESCRIPTION,
                example = Constants.BOOLEAN_EXAMPLE_SCHEMA_VALUE
        )
        @JsonView(DtoView.Common.class)
        boolean isClosed,

        @Schema(
                description = Constants.NUMBER_OF_VIEWS_SCHEMA_DESCRIPTION,
                example = Constants.NUMBER_EXAMPLE_SCHEMA_VALUE
        )
        @JsonView({
                DtoView.User.class,
                DtoView.Category.class
        })
        long numberOfViews,

        @Schema(
                description = Constants.CREATED_DATE_SCHEMA_DESCRIPTION,
                example = Constants.DATE_EXAMPLE_SCHEMA_VALUE
        )
        @JsonView(DtoView.Common.class)
        String createdDate,

        @JsonIgnore
        long totalCount) { }
