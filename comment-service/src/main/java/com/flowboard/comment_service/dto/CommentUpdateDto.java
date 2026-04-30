package com.flowboard.comment_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentUpdateDto {
    @Schema(description = "Comment ID", example = "1")
    @NotNull
    private Integer commentId;

    @Schema(description = "Updated comment content", example = "Updated comment text")
    @NotBlank
    private String content;
}
