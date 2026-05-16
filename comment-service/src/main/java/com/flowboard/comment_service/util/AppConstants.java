package com.flowboard.comment_service.util;

import java.util.List;

public class AppConstants {
    public static final String page = "0";

    public static final String size = "10";

    public static final String sortBy = "commentId";

    public static final String direction = "asc";

    public static final long maxFileSize = 10240L; // in kb

    public static final List<String> allowedFileFormat = List.of(
            "application/pdf",
            "image/png",
            "image/jpeg",
            "image/jpg",
            "image/gif",
            "image/webp",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "text/plain"
    );
}
