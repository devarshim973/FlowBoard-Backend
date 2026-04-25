package com.flowboard.comment_service.service.impl;

import com.cloudinary.Cloudinary;
import com.flowboard.comment_service.dto.AttachmentRequestDto;
import com.flowboard.comment_service.dto.AttachmentResponseDto;
import com.flowboard.comment_service.entity.Attachment;
import com.flowboard.comment_service.exception.AttachmentNotFoundException;
import com.flowboard.comment_service.exception.FileException;
import com.flowboard.comment_service.mapper.Mapper;
import com.flowboard.comment_service.mapper.impl.AttachmentRequestMapper;
import com.flowboard.comment_service.mapper.impl.AttachmentResponseMapper;
import com.flowboard.comment_service.repository.AttachmentRepository;
import com.flowboard.comment_service.service.AttachmentService;
import com.flowboard.comment_service.util.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttachmentServiceImpl implements AttachmentService {
    private final AttachmentRepository attachmentRepository;
    private final AttachmentResponseMapper attachmentResponseMapper;
    private final AttachmentRequestMapper attachmentRequestMapper;
    private final Cloudinary cloudinary;

    @Override
    public AttachmentResponseDto uploadAttachment(MultipartFile file, AttachmentRequestDto attachmentRequestDto) {
        log.info("Attachment upload requested for card {}", attachmentRequestDto.getCardId());
        Attachment attachment = attachmentRequestMapper.mapTo(attachmentRequestDto);

        long maxSize = AppConstants.maxFileSize;
        long sizeInKb = file.getSize() / 1024;
        String fileFormat = file.getContentType();

        validateFile(file);

        String fileUrl;
        String publicId;
        try{
            // input optional like folder and other things empty for now
            Map<String, Object> options = new HashMap<>();
            options.put("folder", "flowboard/attachments");
            options.put("resource_type", "raw");  // Use "raw" for PDFs
            options.put("access_mode", "public");  // Make URLs publicly accessible


            Map<String, Object> uploadResult =
                    cloudinary.uploader().upload(file.getBytes(), options);

            fileUrl = (String) uploadResult.get("secure_url");
            publicId = (String) uploadResult.get("public_id");
        }
        catch (IOException ex) {
            log.error("Attachment upload failed for card {}", attachmentRequestDto.getCardId(), ex);
            throw new FileException("Error when uploading file, please try again!");
        }

        attachment.setFileName(file.getOriginalFilename());
        attachment.setFileUrl(fileUrl);
        attachment.setFileType(fileFormat);
        attachment.setSizeKb(sizeInKb);
        attachment.setPublicId(publicId);

        Attachment savedAttachment = attachmentRepository.save(attachment);
        log.info("Attachment uploaded with id {}", savedAttachment.getAttachmentId());

        return attachmentResponseMapper.mapTo(savedAttachment);
    }

    @Override
    public List<AttachmentResponseDto> getAttachmentsByCard(Integer cardId) {
        List<Attachment> attachment = attachmentRepository.findByCardId(cardId);
        return attachment.stream()
                .map(attachmentResponseMapper::mapTo)
                .toList();
    }

    @Override
    @Transactional
    public void deleteAttachment(Integer attachmentId) {
        log.info("Delete attachment requested for attachment {}", attachmentId);
        Attachment attachment = attachmentRepository.findByAttachmentId(attachmentId)
                .orElseThrow(() -> new AttachmentNotFoundException("Attachment not found with id " + attachmentId));
        try{
            Map<String, Object> options = new HashMap<>();
            options.put("resource_type", "raw");
            cloudinary.uploader().destroy(attachment.getPublicId(), options);
            attachmentRepository.deleteByAttachmentId(attachmentId);
            log.info("Attachment deleted with id {}", attachmentId);
        }
        catch (Exception ex) {
            log.error("Attachment deletion failed for attachment {}", attachmentId, ex);
            throw new FileException("Unable to delete file");
        }
    }

    private void validateFile(MultipartFile file) {
        long maxSize = AppConstants.maxFileSize;
        long sizeInKb = file.getSize() / 1024;

        List<String> allowedFileFormat = AppConstants.allowedFileFormat;

        String fileFormat = file.getContentType();

        if(file.isEmpty()) {
            throw new FileException("Empty file cannot be uploaded");
        }

        if(sizeInKb > maxSize) {
            throw new FileException("Maximum file size is " + maxSize + "kb");
        }

        if(fileFormat != null && !allowedFileFormat.contains(fileFormat)) {
            throw new FileException("Invalid file format, allowed format " + allowedFileFormat.toString());
        }
    }
}
