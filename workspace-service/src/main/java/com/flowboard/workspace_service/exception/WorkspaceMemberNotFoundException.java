package com.flowboard.workspace_service.exception;

public class WorkspaceMemberNotFoundException extends RuntimeException {
    public WorkspaceMemberNotFoundException(String message) {
        super(message);
    }
}
