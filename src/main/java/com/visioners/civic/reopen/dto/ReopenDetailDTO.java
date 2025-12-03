package com.visioners.civic.reopen.dto;

import java.time.Instant;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReopenDetailDTO {
    private String reopenId;
    private String parentComplaintId;
    private ReopenStatusDTO status;
    private String proofUrl;
    private String note;
    private Double lat;
    private Double lon;
    private int reopenNumber;
    private Instant createdAt;
    private List<ReopenSummaryDTO> previousReopens;
}
