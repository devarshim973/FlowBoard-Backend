package com.flowboard.comment_service.util;

import java.util.List;

public class AppConstants {
    public static final String page = "0";

    public static final String size = "10";

    public static final String sortBy = "commentId";

    public static final String direction = "asc";

    public static final long maxFileSize = 1024L; // in kb

    public static final List<String> allowedFileFormat = List.of("application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
}
