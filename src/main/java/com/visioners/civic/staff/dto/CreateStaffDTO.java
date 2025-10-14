package com.visioners.civic.staff.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateStaffDTO {
    private String username;
    private String mobileNumber;
    private String password;
    private String roleName;         // ID of the role (e.g., FIELD_WORKER)
    private Long departmentId;
    private Long districtId;
    private Long blockId;
}
