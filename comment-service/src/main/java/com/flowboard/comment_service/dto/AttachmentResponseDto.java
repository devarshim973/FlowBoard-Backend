package com.flowboard.comment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentResponseDto {
    private Integer attachmentId;
    private Integer cardId;
    private Integer uploaderId;

    private String fileName;
    private String fileUrl;
    private String fileType;

    private Long sizeKb;
    private LocalDateTime uploadedAt;
}