package com.visioners.civic.complaint.dto.usercomplaintdtos;

import jakarta.validation.constraints.NotNull;
import com.visioners.civic.complaint.model.Location;

import jakarta.validation.constraints.NotBlank;

public record ComplaintRaiseRequest(
    @NotBlank(message = "description can't be empty") String description,
    @NotNull(message = "location cannot be empty") Location location
) {}
