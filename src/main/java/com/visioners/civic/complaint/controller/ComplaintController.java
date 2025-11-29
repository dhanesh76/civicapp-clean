package com.visioners.civic.complaint.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.visioners.civic.complaint.dto.ComplaintView;
import com.visioners.civic.complaint.entity.Complaint;
import com.visioners.civic.complaint.service.ComplaintService;
import com.visioners.civic.complaint.service.ComplaintAuditService;
import com.visioners.civic.complaint.entity.ComplaintCycle;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/complaints/")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class ComplaintController {

    private final ComplaintService complaintService;
    private final ComplaintAuditService auditService;

    @GetMapping("/{complaintId}")
    ResponseEntity<ComplaintView> getComplaintById(
        @NotNull @PathVariable("complaintId") String complaintId){
        Complaint complaint = complaintService.getComplaintByComplaintId(complaintId);
        ComplaintView complaintView = ComplaintService.getComplaintView(complaint);
        return ResponseEntity.ok(complaintView);
    }

    @GetMapping("/{complaintId}/history")
    public ResponseEntity<List<ComplaintCycle>> getHistory(@NotNull @PathVariable("complaintId") String complaintId) {
        Complaint complaint = complaintService.getComplaintByComplaintId(complaintId);
        List<ComplaintCycle> history = auditService.getHistory(complaint.getId());
        return ResponseEntity.ok(history);
    }
}
