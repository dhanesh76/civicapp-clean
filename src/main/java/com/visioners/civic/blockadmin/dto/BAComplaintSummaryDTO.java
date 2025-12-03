package com.visioners.civic.blockadmin.dto;

import java.time.Instant;

import com.visioners.civic.complaint.entity.Complaint;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BAComplaintSummaryDTO {

    private String complaintId;
    private String description;
    private String imageUrl;
    private Instant createdAt;
    private int reopenCount;
    private String departmentName;

    public static BAComplaintSummaryDTO fromComplaint(Complaint c) {
        return BAComplaintSummaryDTO.builder()
                .complaintId(c.getComplaintId())
                .description(c.getDescription())
                .imageUrl(c.getImageUrl())
                .createdAt(c.getCreatedAt())
                .reopenCount(c.getReopenCount())
                .departmentName(c.getDepartment() == null ? null : c.getDepartment().getName())
                .build();
    }
}
