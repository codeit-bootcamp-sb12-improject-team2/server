package com.codeit.server.interest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class InterestCreateRequest {

    @NotBlank(message = "interest name is required")
    private String name;

    private List<String> keywords;
}