package com.codeit.server.interest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CursorPageResponse<T> {

    private List<T> content;
    private String nextCursor;
    private String nextAfter;
    private int size;
    private long totalElements;
    private boolean hasNext;

    public static <T> CursorPageResponse<T> of(
            List<T> content,
            String nextCursor,
            String nextAfter,
            int size,
            long totalElements
    ) {
        return CursorPageResponse.<T>builder()
                .content(content)
                .nextCursor(nextCursor)
                .nextAfter(nextAfter)
                .size(size)
                .totalElements(totalElements)
                .hasNext(nextCursor != null)
                .build();
    }
}