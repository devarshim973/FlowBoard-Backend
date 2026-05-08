package com.flowboard.comment_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentRequestDto {
    @Schema(description = "Card ID", example = "1")
    @NotNull
    private Integer cardId;

    @Schema(description = "Author user ID", example = "5")
    @NotNull
    private Integer authorId;

    @Schema(description = "Comment content", example = "This task needs review")
    @NotBlank
    private String content;

    @Schema(description = "Parent comment ID for reply", example = "2")
    private Integer parentCommentId;
}
