package com.codeit.server.article.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CursorPageResponse<T> {
    private List<T> content;
    private String nextCursor;
    private Long nextIdAfter;
    private int size;
    private long totalElements;
    private boolean hasNext;
}
