package com.flowboard.comment_service.dto;

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
    @NotNull
    private Integer cardId;

    @NotNull
    private Integer uploaderId;
}