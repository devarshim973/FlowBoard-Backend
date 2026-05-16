package com.flowboard.comment_service.controller;

import com.flowboard.comment_service.dto.AttachmentRequestDto;
import com.flowboard.comment_service.dto.AttachmentResponseDto;
import com.flowboard.comment_service.service.AttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/attachments")
@RequiredArgsConstructor
@Tag(name = "Attachment Controller", description = "Attachment management related APIs")
public class AttachmentController {
    private final AttachmentService attachmentService;

    @Operation(summary = "Upload attachment", description = "Uploads file attachment for a card")
    @ApiResponse(responseCode = "200", description = "Attachment uploaded successfully")
    @PostMapping("/upload")
    public ResponseEntity<AttachmentResponseDto> upload(@RequestParam("file") MultipartFile file,
                                                        @RequestParam Integer cardId,
                                                        @RequestParam(required = false) Integer commentId,
                                                        @RequestParam Integer uploaderId) {
        AttachmentRequestDto attachmentRequestDto = new AttachmentRequestDto();
        attachmentRequestDto.setCardId(cardId);
        attachmentRequestDto.setCommentId(commentId);
        attachmentRequestDto.setUploaderId(uploaderId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(attachmentService.uploadAttachment(file, attachmentRequestDto));
    }

    @Operation(summary = "Get attachments by card", description = "Returns all attachments of given card")
    @ApiResponse(responseCode = "200", description = "Attachments fetched successfully")
    @GetMapping("/card/{cardId}")
    public ResponseEntity<List<AttachmentResponseDto>> getByCard(@PathVariable Integer cardId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(attachmentService.getAttachmentsByCard(cardId));
    }

    @Operation(summary = "Get attachments by comment", description = "Returns all attachments of given comment")
    @ApiResponse(responseCode = "200", description = "Attachments fetched successfully")
    @GetMapping("/comment/{commentId}")
    public ResponseEntity<List<AttachmentResponseDto>> getByComment(@PathVariable Integer commentId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(attachmentService.getAttachmentsByComment(commentId));
    }

    @Operation(summary = "Delete attachment", description = "Deletes attachment by ID")
    @ApiResponse(responseCode = "200", description = "Attachment deleted successfully")
    @DeleteMapping("/delete/{attachmentId}")
    public ResponseEntity<String> delete(@PathVariable Integer attachmentId) {
        attachmentService.deleteAttachment(attachmentId);
        return ResponseEntity.status(HttpStatus.OK).body("Attachment deleted");
    }
}
