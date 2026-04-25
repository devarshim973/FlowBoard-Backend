package com.flowboard.comment_service.exception;

public class AttachmentNotFoundException extends RuntimeException{
    public AttachmentNotFoundException(String message) {
        super(message);
    }
}
