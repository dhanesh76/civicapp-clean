package com.visioners.civic.staff.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateStaffDTO {
    @NotBlank(message = "Username cannot be empty")
    private String username;

    @NotBlank(message = "Mobile number cannot be empty")
    @Pattern(regexp = "[0-9]{10}", message = "Enter a valid 10-digit mobile number")
    private String mobileNumber;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "Role name cannot be empty")
    private String roleName;         // ID of the role (e.g., FIELD_WORKER)

    @NotNull(message = "Department ID cannot be null")
    private Long departmentId;

    @NotNull(message = "District ID cannot be null")
    private Long districtId;

    @NotNull(message = "Block ID cannot be null")
    private Long blockId;
}
