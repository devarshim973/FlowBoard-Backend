package com.flowboard.workspace_service.util;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@NoArgsConstructor
public class CustomPageResponse<T> {
    private int pageSize;
    private int pageNumber;
    private int numberOfElements;         // elements in current page
    private int totalPages;               // total number of pages
    private Long totalNumberOfElements;    // total elements in db
    private List<T> content;
    private boolean isLast;
    private boolean isFirst;

    public CustomPageResponse(Page<T> page) {
        this.setPageSize(page.getSize());
        this.setPageNumber(page.getNumber());
        this.setNumberOfElements(page.getNumberOfElements());
        this.setTotalPages(page.getTotalPages());
        this.setTotalNumberOfElements(page.getTotalElements());
        this.setContent(page.getContent());
        this.setLast(page.isLast());
        this.setFirst(page.isFirst());
    }
}
