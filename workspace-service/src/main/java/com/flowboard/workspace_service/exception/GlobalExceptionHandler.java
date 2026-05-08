package com.flowboard.workspace_service.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(WorkspaceNotFoundException.class)
    public ResponseEntity<String> handelWorkspaceNotFoundException(WorkspaceNotFoundException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(WorkspaceMemberNotFoundException.class)
    public ResponseEntity<String> handelWorkspaceMemberNotFoundException(WorkspaceMemberNotFoundException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handelGenericException(Exception ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
