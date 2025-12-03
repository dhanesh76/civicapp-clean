package com.visioners.civic.complaint.dto.usercomplaintdtos;

import jakarta.validation.constraints.NotNull;

import com.visioners.civic.complaint.model.Category;
import com.visioners.civic.complaint.model.Location;
import com.visioners.civic.complaint.model.SubCategory;

import jakarta.validation.constraints.NotBlank;

public record ComplaintRaiseRequest(
    @NotBlank(message = "description can't be empty") String description,
    @NotNull(message = "location cannot be empty") Location location,
    @NotNull(message = "category cannot be empty") Category category,
    @NotNull(message = "subcategory cannot be empty") SubCategory subcategory
) {}
