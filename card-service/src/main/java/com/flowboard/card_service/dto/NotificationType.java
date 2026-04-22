package com.flowboard.card_service.dto;

public enum NotificationType {

    ASSIGNMENT,        // Card assigned to user
    MENTION,           // @mention in comment
    DUE_DATE,          // Due date reminder
    COMMENT,           // Comment reply
    MOVE,              // Card moved
    SYSTEM,            // System notifications
    BROADCAST          // Admin broadcast
}