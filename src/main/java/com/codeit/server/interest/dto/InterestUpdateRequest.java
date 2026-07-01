package com.codeit.server.interest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class InterestUpdateRequest {

    @NotBlank(message = "Revised name is required.")
    private String name;
}