package com.visioners.civic.reopen.dto;
import lombok.Data;

/** Request sent by user to reopen a complaint (multipart: proofImage + data JSON) */
@Data
public class ReopenRequest {
    private String complaintId;
    private String note;
    private Double lat;
    private Double lon;
}
