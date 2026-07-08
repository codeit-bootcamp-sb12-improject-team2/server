package com.codeit.server.interest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterestCreateRequest {

    @NotBlank(message = "interest name is required")
    private String name;

    private List<String> keywords;
}