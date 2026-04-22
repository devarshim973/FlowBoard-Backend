package com.flowboard.comment_service.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<String> handelCommentNotFound(CommentNotFoundException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(FileException.class)
    public ResponseEntity<String> handelLargeFileSizeException(FileException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(AttachmentNotFoundException.class)
    public ResponseEntity<String> handelAttachmentNotException(AttachmentNotFoundException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handelAllException(Exception ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }


}
