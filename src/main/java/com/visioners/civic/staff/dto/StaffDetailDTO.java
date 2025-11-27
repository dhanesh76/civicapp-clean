package com.visioners.civic.staff.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffDetailDTO {
    @NotNull(message = "ID cannot be null")
    private Long id;

    @NotBlank(message = "Username cannot be empty")
    private String username;

    @NotBlank(message = "Mobile number cannot be empty")
    @Pattern(regexp = "[0-9]{10}", message = "Enter a valid 10-digit mobile number")
    private String mobileNumber;

    @Email
    private String email;

    @NotBlank(message = "Role name cannot be empty")
    private String roleName;

    @NotNull
    private Long departmentId;

    @NotBlank(message = "Department name cannot be empty")
    private String departmentName;

    @NotBlank(message = "District name cannot be empty")
    private String districtName;

    @NotBlank(message = "Block name cannot be empty")
    private String blockName;

    @NotNull(message = "CreatedAt cannot be null")
    private Instant createdAt;
}

