package com.visioners.civic.complaint.dto.usercomplaintdtos;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReopenDetailDTO {
    private String parentComplaintId;
    private ReopenSummaryDTO currentReopen;
    private List<ReopenSummaryDTO> previousReopens;
}
