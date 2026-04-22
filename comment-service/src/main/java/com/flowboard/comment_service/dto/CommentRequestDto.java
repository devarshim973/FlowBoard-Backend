package com.flowboard.comment_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentRequestDto {
    @NotNull
    private Integer cardId;

    @NotNull
    private Integer authorId;

    @NotBlank
    private String content;

    private Integer parentCommentId;
}
