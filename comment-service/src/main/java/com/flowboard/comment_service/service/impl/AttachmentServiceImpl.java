package com.flowboard.comment_service.service.impl;

import com.cloudinary.Cloudinary;
import com.flowboard.comment_service.dto.AttachmentRequestDto;
import com.flowboard.comment_service.dto.AttachmentResponseDto;
import com.flowboard.comment_service.entity.Attachment;
import com.flowboard.comment_service.exception.AttachmentNotFoundException;
import com.flowboard.comment_service.exception.FileException;
import com.flowboard.comment_service.mapper.Mapper;
import com.flowboard.comment_service.repository.AttachmentRepository;
import com.flowboard.comment_service.service.AttachmentService;
import com.flowboard.comment_service.util.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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
    private final Mapper<Attachment, AttachmentResponseDto> attachmentResponseMapper;
    private final Mapper<AttachmentRequestDto, Attachment> attachmentRequestMapper;
    private final Cloudinary cloudinary;

    @Override
    public AttachmentResponseDto uploadAttachment(MultipartFile file, AttachmentRequestDto attachmentRequestDto) {
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

            Map<String, Object> uploadResult =
                    cloudinary.uploader().upload(file.getBytes(), options);

            fileUrl = (String) uploadResult.get("secure_url");
            publicId = (String) uploadResult.get("public_id");
        }
        catch (IOException ex) {
            throw new FileException("Error when uploading file, please try again!");
        }

        attachment.setFileName(file.getOriginalFilename());
        attachment.setFileUrl(fileUrl);
        attachment.setFileType(fileFormat);
        attachment.setSizeKb(sizeInKb);
        attachment.setPublicId(publicId);

        Attachment savedAttachment = attachmentRepository.save(attachment);

        return attachmentResponseMapper.mapTo(savedAttachment);
    }

    @Override
    public List<AttachmentResponseDto> getAttachmentsByCard(Integer cardId) {
        List<Attachment> attachment = attachmentRepository.findByCardId(cardId);
        return attachment.stream()
                .map(attachmentResponseMapper::mapTo)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteAttachment(Integer attachmentId) {
        log.info("Deleting attachment");
        Attachment attachment = attachmentRepository.findByAttachmentId(attachmentId)
                .orElseThrow(() -> new AttachmentNotFoundException("Attachment not found with id " + attachmentId));
        try{
            Map<String, Object> options = new HashMap<>();
//            options.put("folder", "flowboard/attachments");
            cloudinary.uploader().destroy(attachment.getPublicId(), options);
            attachmentRepository.deleteByAttachmentId(attachmentId);
        }
        catch (IOException ex) {
            log.info("Error when deleting attachment with id " + attachmentId);
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