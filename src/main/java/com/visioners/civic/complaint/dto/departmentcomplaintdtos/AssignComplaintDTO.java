package com.visioners.civic.complaint.dto.departmentcomplaintdtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AssignComplaintDTO {
	@NotBlank(message = "complaintId cannot be blank")
	private String complaintId;

	@NotNull(message = "workerId is required")
	private Long workerId;
}


