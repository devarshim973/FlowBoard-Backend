package com.flowboard.admin_service.util;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@NoArgsConstructor
public class CustomPageResponse<T> {
    private int pageSize;
    private int pageNumber;
    private int numberOfElements;
    private int totalPages;
    private Long totalNumberOfElements;
    private List<T> content;
    private boolean isLast;
    private boolean isFirst;

    public CustomPageResponse(Page<T> page) {
        this.pageSize = page.getSize();
        this.pageNumber = page.getNumber();
        this.numberOfElements = page.getNumberOfElements();
        this.totalPages = page.getTotalPages();
        this.totalNumberOfElements = page.getTotalElements();
        this.content = page.getContent();
        this.isLast = page.isLast();
        this.isFirst = page.isFirst();
    }
}
