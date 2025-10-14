package com.visioners.civic.complaint.service;

import org.springframework.stereotype.Service;

import com.visioners.civic.complaint.dto.ComplaintView;
import com.visioners.civic.complaint.dto.departmentcomplaintdtos.ComplaintViewDTO;
import com.visioners.civic.complaint.entity.Complaint;
import com.visioners.civic.complaint.repository.ComplaintRepository;
import com.visioners.civic.exception.ComplaintNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ComplaintService {

    private final ComplaintRepository complaintRepository;

    public Complaint getComplaint(Long complaintId){
        return complaintRepository.findById(complaintId)
            .orElseThrow(() -> new  ComplaintNotFoundException ("no complaint exists with id: " + complaintId));
    }   

    //helper methods
    public static ComplaintView getComplaintView(Complaint complaint){
        return ComplaintView.builder()
                    .id(complaint.getId())
                    .raidedBy(complaint.getRaisedBy().getMobileNumber())
                    .imageUrl(complaint.getImageUrl())
                    .assignedBy(complaint.getAssignedBy().getUser().getUsername())
                    .assignedTo(complaint.getAssignedTo().getUser().getUsername())
                    .severity(complaint.getSeverity())
                    .status(complaint.getStatus())
                    .solutionImageUrl(complaint.getSolutionImageUrl())
                    .solutionNote(complaint.getSolutionNote())
                    .location(complaint.getLocation())
                    .build();
    }

    public static ComplaintViewDTO mapToComplaintViewDTO(Complaint complaint) {
        if (complaint == null) {
            return null;
        }

        return ComplaintViewDTO.builder()
                .id(complaint.getId())
                .description(complaint.getDescription())
                .status(complaint.getStatus())
                .severity(complaint.getSeverity() != null ? complaint.getSeverity() : null)
                .location(complaint.getLocation())
                .assignedBy(
                    complaint.getAssignedBy() != null 
                        ? complaint.getAssignedBy().getUser().getUsername()
                        : null
                )
                .assignedTo(
                    complaint.getAssignedTo() != null 
                        ? complaint.getAssignedTo().getUser().getUsername()
                        : null
                )
                .createdAt(complaint.getCreatedAt())
                .assignedAt(complaint.getAssignedAt())
                .resolvedAt(complaint.getResolvedAt())
                .solutionNote(complaint.getSolutionNote())
                .solutionImageUrl(complaint.getSolutionImageUrl())
                .rejectionNote(complaint.getRejectionNote())
                .build();
    }
}
