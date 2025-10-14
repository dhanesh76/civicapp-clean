package com.visioners.civic.staff.dto;

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
    private Long id;
    private String username;
    private String mobileNumber;
    private String roleName;
    private String departmentName;
    private String districtName;
    private String blockName;
    private Instant createdAt;
}

