package com.flowboard.comment_service.dto;

import jakarta.persistence.Column;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
public class CommentResponseDto {
    private Integer commentId;

    private Integer cardId;

    private Integer authorId;

    private String content;

    private Boolean isDeleted;

    private Integer parentCommentId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
