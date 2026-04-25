package com.flowboard.comment_service.service;

import com.flowboard.comment_service.dto.AttachmentRequestDto;
import com.flowboard.comment_service.dto.AttachmentResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AttachmentService {
    public AttachmentResponseDto uploadAttachment(MultipartFile file, AttachmentRequestDto request);

    public List<AttachmentResponseDto> getAttachmentsByCard(Integer cardId);

    public void deleteAttachment(Integer attachmentId);
}