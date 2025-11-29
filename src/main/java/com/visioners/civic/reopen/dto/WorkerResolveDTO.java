package com.visioners.civic.reopen.dto;
import lombok.Data;

/** Worker resolve DTO (multipart: image + JSON) */
@Data
public class WorkerResolveDTO {
    private String reopenId;
    private String complaintId;
    private String solutionNote;
    private Double lat;
    private Double lon;
}
