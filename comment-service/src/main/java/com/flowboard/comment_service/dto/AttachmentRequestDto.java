package com.flowboard.comment_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttachmentRequestDto {
    @ Schema(description = "Card ID", example = "1")
    @NotNull
    private Integer cardId;

    @Schema(description = "Uploader user ID", example = "5")
    @NotNull
    private Integer uploaderId;
}