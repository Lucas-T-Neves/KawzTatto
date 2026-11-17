package com.kawz.tattoo.api.projeto.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StatusRequest(@NotBlank @Size(max = 45) String status) {}
