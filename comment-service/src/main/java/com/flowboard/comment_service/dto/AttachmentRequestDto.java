package com.flowboard.comment_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AttachmentRequestDto {
    @ Schema(description = "Card ID", example = "1")
    @NotNull
    private Integer cardId;

    @Schema(description = "Comment ID", example = "12")
    private Integer commentId;

    @Schema(description = "Uploader user ID", example = "5")
    @NotNull
    private Integer uploaderId;

    public AttachmentRequestDto(Integer cardId, Integer commentId, Integer uploaderId) {
        this.cardId = cardId;
        this.commentId = commentId;
        this.uploaderId = uploaderId;
    }
}
