package com.visioners.civic.blockadmin.dto;

import java.time.Instant;

import com.visioners.civic.complaint.entity.ReopenComplaint;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BAReopenSummaryDTO {
    private String reopenId;
    private String parentComplaintId;
    private String note;
    private String proofUrl;
    private Instant createdAt;
    private int reopenNumber;
    private String departmentName;

    public static BAReopenSummaryDTO fromEntity(ReopenComplaint r) {
        return BAReopenSummaryDTO.builder()
                .reopenId(r.getReopenId())
                .parentComplaintId(r.getParentComplaint().getComplaintId())
                .note(r.getNote())
                .proofUrl(r.getProofUrl())
                .createdAt(r.getCreatedAt())
                .reopenNumber(r.getReopenNumber())
                .departmentName(r.getDepartment() == null ? null : r.getDepartment().getName())
                .build();
    }
}
