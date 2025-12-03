package com.visioners.civic.complaint.dto.usercomplaintdtos;

import java.time.Instant;

import com.visioners.civic.complaint.entity.ReopenComplaint;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReopenSummaryDTO {
    private String reopenId;
    private String parentComplaintId;
    private String note;
    private String proofUrl;
    private Instant createdAt;
    private int reopenNumber;
    private String status;

    public static ReopenSummaryDTO fromEntity(ReopenComplaint r) {
        return ReopenSummaryDTO.builder()
                .reopenId(r.getReopenId())
                .parentComplaintId(r.getParentComplaint().getComplaintId())
                .note(r.getNote())
                .proofUrl(r.getProofUrl())
                .createdAt(r.getCreatedAt())
                .reopenNumber(r.getReopenNumber())
                .status(r.getStatus() == null ? null : r.getStatus().name())
                .build();
    }
}
