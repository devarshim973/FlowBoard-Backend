package com.flowboard.comment_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Attachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer attachmentId;

    private Integer cardId;
    private Integer uploaderId;

    private String fileName;
    private String fileUrl;
    private String publicId;
    private String fileType;

    private Long sizeKb;

    @CreationTimestamp
    private LocalDateTime uploadedAt;
}