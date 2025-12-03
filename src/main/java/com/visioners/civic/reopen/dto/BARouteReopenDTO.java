package com.visioners.civic.reopen.dto;

import lombok.Data;

/** BA payloads */
@Data
public class BARouteReopenDTO {
    private String complaintId;
    private String reopenId;
    private Long departmentId;
    private String note;
}
