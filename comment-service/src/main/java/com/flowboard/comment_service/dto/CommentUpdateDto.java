package com.flowboard.comment_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentUpdateDto {
    @NotNull
    private Integer commentId;

    @NotBlank
    private String content;
}
