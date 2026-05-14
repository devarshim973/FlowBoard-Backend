package com.flowboard.comment_service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.flowboard.comment_service.dto.AttachmentRequestDto;
import com.flowboard.comment_service.dto.AttachmentResponseDto;
import com.flowboard.comment_service.entity.Attachment;
import com.flowboard.comment_service.exception.AttachmentNotFoundException;
import com.flowboard.comment_service.exception.FileException;
import com.flowboard.comment_service.mapper.impl.AttachmentRequestMapper;
import com.flowboard.comment_service.mapper.impl.AttachmentResponseMapper;
import com.flowboard.comment_service.repository.AttachmentRepository;
import com.flowboard.comment_service.service.impl.AttachmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceImplTest {

    @Mock
    private AttachmentRepository attachmentRepository;
    @Mock
    private AttachmentResponseMapper responseMapper;
    @Mock
    private AttachmentRequestMapper requestMapper;
    @Mock
    private Cloudinary cloudinary;
    @Mock
    private Uploader uploader;
    @Mock
    private MultipartFile file;

    private AttachmentServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AttachmentServiceImpl(attachmentRepository, responseMapper, requestMapper, cloudinary);
    }

    @Test
    void uploadAttachment_withValidFile_savesAndMaps() throws Exception {
        AttachmentRequestDto request = new AttachmentRequestDto();
        request.setCardId(1);
        Attachment attachment = new Attachment();
        Attachment saved = new Attachment();
        saved.setAttachmentId(11);
        AttachmentResponseDto dto = new AttachmentResponseDto();

        when(requestMapper.mapTo(request)).thenReturn(attachment);
        when(file.getSize()).thenReturn(1024L);
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.isEmpty()).thenReturn(false);
        when(file.getBytes()).thenReturn(new byte[]{1, 2, 3});
        when(file.getOriginalFilename()).thenReturn("spec.pdf");
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap()))
                .thenReturn(Map.of("secure_url", "https://cdn.test/file.pdf", "public_id", "file-1"));
        when(attachmentRepository.save(attachment)).thenReturn(saved);
        when(responseMapper.mapTo(saved)).thenReturn(dto);

        AttachmentResponseDto result = service.uploadAttachment(file, request);

        assertEquals(dto, result);
    }

    @Test
    void uploadAttachment_withEmptyFile_throws() {
        AttachmentRequestDto request = new AttachmentRequestDto();
        request.setCardId(1);
        when(requestMapper.mapTo(request)).thenReturn(new Attachment());
        when(file.getSize()).thenReturn(0L);
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.isEmpty()).thenReturn(true);

        assertThrows(FileException.class, () -> service.uploadAttachment(file, request));
    }

    @Test
    void uploadAttachment_withInvalidType_throws() {
        AttachmentRequestDto request = new AttachmentRequestDto();
        request.setCardId(1);
        when(requestMapper.mapTo(request)).thenReturn(new Attachment());
        when(file.getSize()).thenReturn(1024L);
        when(file.getContentType()).thenReturn("text/plain");
        when(file.isEmpty()).thenReturn(false);

        assertThrows(FileException.class, () -> service.uploadAttachment(file, request));
    }

    @Test
    void uploadAttachment_whenCloudinaryFails_throws() throws Exception {
        AttachmentRequestDto request = new AttachmentRequestDto();
        request.setCardId(1);
        when(requestMapper.mapTo(request)).thenReturn(new Attachment());
        when(file.getSize()).thenReturn(1024L);
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.isEmpty()).thenReturn(false);
        when(file.getBytes()).thenThrow(new IOException("upload failed"));

        assertThrows(FileException.class, () -> service.uploadAttachment(file, request));
    }

    @Test
    void getAttachmentsByCard_returnsMappedList() {
        Attachment attachment = new Attachment();
        AttachmentResponseDto dto = new AttachmentResponseDto();
        when(attachmentRepository.findByCardId(1)).thenReturn(List.of(attachment));
        when(responseMapper.mapTo(attachment)).thenReturn(dto);

        List<AttachmentResponseDto> result = service.getAttachmentsByCard(1);

        assertEquals(1, result.size());
    }

    @Test
    void deleteAttachment_withMissingAttachment_throws() {
        when(attachmentRepository.findByAttachmentId(1)).thenReturn(Optional.empty());

        assertThrows(AttachmentNotFoundException.class, () -> service.deleteAttachment(1));
    }

    @Test
    void deleteAttachment_withValidAttachment_deletes() throws Exception {
        Attachment attachment = new Attachment();
        attachment.setPublicId("public-1");
        when(attachmentRepository.findByAttachmentId(1)).thenReturn(Optional.of(attachment));
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.destroy(any(String.class), anyMap())).thenReturn(Map.of("result", "ok"));

        service.deleteAttachment(1);

        verify(attachmentRepository).deleteByAttachmentId(1);
    }

    @Test
    void deleteAttachment_whenCloudinaryFails_throws() throws Exception {
        Attachment attachment = new Attachment();
        attachment.setPublicId("public-1");
        when(attachmentRepository.findByAttachmentId(1)).thenReturn(Optional.of(attachment));
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.destroy(any(String.class), anyMap())).thenThrow(new RuntimeException("boom"));

        assertThrows(FileException.class, () -> service.deleteAttachment(1));
    }
}
