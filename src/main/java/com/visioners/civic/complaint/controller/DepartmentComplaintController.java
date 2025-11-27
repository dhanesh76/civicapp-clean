package com.visioners.civic.complaint.controller;
import jakarta.validation.Valid;

import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.visioners.civic.auth.userdetails.UserPrincipal;
import com.visioners.civic.complaint.dto.departmentcomplaintdtos.AssignComplaintDTO;
import com.visioners.civic.complaint.dto.departmentcomplaintdtos.ComplaintViewDTO;
import com.visioners.civic.complaint.dto.departmentcomplaintdtos.DepartmentComplaintStatisticsDTO;
import com.visioners.civic.complaint.dto.departmentcomplaintdtos.DeptComplaintsSummaryDTO;
import com.visioners.civic.complaint.dto.departmentcomplaintdtos.RejectComplaintDto;
import com.visioners.civic.complaint.model.IssueSeverity;
import com.visioners.civic.complaint.model.IssueStatus;
import com.visioners.civic.complaint.service.DepartmentComplaintService;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

@RestController
@RequestMapping("/api/departments/complaints")
@RequiredArgsConstructor
public class DepartmentComplaintController {

    private final DepartmentComplaintService departmentComplaintService;

    @GetMapping
    public ResponseEntity<Page<DeptComplaintsSummaryDTO>> viewDeptComplaints(
            @AuthenticationPrincipal UserPrincipal principal,
            Pageable page,
            @RequestParam(required = false) IssueSeverity severity,
            @RequestParam(required = false) IssueStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date to
    ) {
        return ResponseEntity.ok(departmentComplaintService.viewDeptComplaints(principal, page, severity, status, from, to));
    }

    @PostMapping("/assign")
    public ResponseEntity<ComplaintViewDTO> assignComplaint(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AssignComplaintDTO dto
    ) {
        return ResponseEntity.ok(departmentComplaintService.assignComplaint(principal, dto));
    }

    @PostMapping("/approve/{complaintId}")
    public ResponseEntity<ComplaintViewDTO> approveComplaint(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String complaintId
    ) {
        return ResponseEntity.ok(departmentComplaintService.approveComplaint(principal, complaintId));
    }

    @PostMapping("/reject")
    public ResponseEntity<ComplaintViewDTO> rejectComplaint(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody RejectComplaintDto dto
    ) {
        return ResponseEntity.ok(departmentComplaintService.rejectComplaint(principal, dto));
    }

    @GetMapping("/stats")
    public ResponseEntity<DepartmentComplaintStatisticsDTO> getStatistics(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date to
    ) {
        return ResponseEntity.ok(departmentComplaintService.getStatistics(principal, from, to));
    }
    
    @GetMapping("/{complaintId}")
    public ResponseEntity<ComplaintViewDTO> getComplaintDetail(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String complaintId
    ) {
        return ResponseEntity.ok(departmentComplaintService.getComplaintByComplaintIdDetail(principal, complaintId));
    }

    // @GetMapping("/rejected-complaints")
    // public ResponseEntity<Page<ComplaintViewDTO>> getRejectedComplaints(
    //         @AuthenticationPrincipal UserPrincipal principal,
    //         Pageable pageable
    // ) {
    //     return ResponseEntity.ok(departmentComplaintService.getRejectedComplaints(principal, pageable));
    // }
}
