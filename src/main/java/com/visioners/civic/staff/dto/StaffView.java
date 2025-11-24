package com.visioners.civic.staff.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StaffView(

    @NotNull(message = "ID cannot be null")
    Long id,

    @NotBlank(message = "Name cannot be empty")
    String name
) {}
