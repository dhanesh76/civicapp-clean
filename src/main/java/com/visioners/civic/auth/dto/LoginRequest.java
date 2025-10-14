package com.visioners.civic.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginRequest(
    @NotBlank(message = "mobile number cannot be empty")
    @Pattern(regexp="(^$|[0-9]{10})", message = "enter valid number")
    String mobileNumber,

    String password
){} 
