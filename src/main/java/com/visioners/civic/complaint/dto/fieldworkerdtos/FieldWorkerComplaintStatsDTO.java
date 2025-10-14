package com.visioners.civic.complaint.dto.fieldworkerdtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FieldWorkerComplaintStatsDTO {
    private long total;
    private long open;
    private long assigned;
    private long resolved;
    private long rejected;
    private long closed;
}

