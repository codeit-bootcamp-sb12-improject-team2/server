package com.codeit.server.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NewsSummaryResponseDto {
    private String summary;
    private List<String> keywords;
}
