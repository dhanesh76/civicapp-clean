package com.visioners.civic.reopen.dto;

import java.time.Instant;

import com.visioners.civic.complaint.entity.ReopenComplaint;
import com.visioners.civic.complaint.model.IssueSeverity;
import com.visioners.civic.complaint.model.ReopenStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Summary DTO for a Reopen record shown in lists and "previousReopens".
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ReopenSummaryDTO {

    private String reopenId;
    private String parentComplaintId;
    private ReopenStatusDTO status;        // DTO enum you referenced elsewhere
    private IssueSeverity severity;        // reuse domain enum for severity
    private Instant createdAt;
    private int reopenNumber;

    // optional / minimal fields if needed in list
    private String proofUrl;
    private String note;
    private Double lat;
    private Double lon;

    /**
     * Static factory to convert from entity to summary DTO.
     * Safe for null nested fields.
     */
    public static ReopenSummaryDTO fromEntity(ReopenComplaint r) {
        if (r == null) return null;

        // parent complaint fields may be null in some edge cases
        String parentId = (r.getParentComplaint() != null) ? r.getParentComplaint().getComplaintId() : null;
        IssueSeverity sev = (r.getParentComplaint() != null) ? r.getParentComplaint().getSeverity() : null;

        ReopenStatus statusEnum = r.getStatus();
        ReopenStatusDTO statusDto = (statusEnum == null) ? null : ReopenStatusDTO.valueOf(statusEnum.name());

        return ReopenSummaryDTO.builder()
                .reopenId(r.getReopenId())
                .parentComplaintId(parentId)
                .status(statusDto)
                .severity(sev)
                .createdAt(r.getCreatedAt())
                .reopenNumber(r.getReopenNumber())
                .proofUrl(r.getProofUrl())
                .note(r.getNote())
                .lat(r.getLat())
                .lon(r.getLon())
                .build();
    }
}
