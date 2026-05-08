package com.flowboard.card_service.entity;

public enum ActivityType {
    CREATED,
    UPDATED,
    MOVED,
    REORDERED,

    ASSIGNED,
    UNASSIGNED,

    PRIORITY_CHANGED,
    STATUS_CHANGED,

    DUE_DATE_CHANGED,

    ARCHIVED,
    UNARCHIVED,
    DELETED
}