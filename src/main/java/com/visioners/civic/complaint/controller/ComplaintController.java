package com.visioners.civic.complaint.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.visioners.civic.complaint.dto.ComplaintView;
import com.visioners.civic.complaint.entity.Complaint;
import com.visioners.civic.complaint.service.ComplaintService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/complaints/")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;

    @GetMapping("/{complaintId}")
    ResponseEntity<ComplaintView> getComplaintById(@PathVariable("complaintId") long complaintId){
        Complaint complaint = complaintService.getComplaint(complaintId);
        ComplaintView complaintView = ComplaintService.getComplaintView(complaint);
        return ResponseEntity.ok(complaintView);
    }
}
