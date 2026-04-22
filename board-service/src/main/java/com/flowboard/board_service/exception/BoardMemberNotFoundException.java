package com.flowboard.board_service.exception;

public class BoardMemberNotFoundException extends RuntimeException {
    public BoardMemberNotFoundException(String message) {
        super(message);
    }
}
