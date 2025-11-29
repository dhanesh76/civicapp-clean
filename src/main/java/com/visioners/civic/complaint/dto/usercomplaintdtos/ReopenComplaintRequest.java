package com.visioners.civic.complaint.dto.usercomplaintdtos;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReopenComplaintRequest {
    private String complaintId;
    private String note;
    private Double lat;
    private Double lon;

    // convenience accessors for the service code (record-like)
    public String complaintId() { return complaintId; }
    public String note() { return note; }
    public Double lat() { return lat; }
    public Double lon() { return lon; }
}
