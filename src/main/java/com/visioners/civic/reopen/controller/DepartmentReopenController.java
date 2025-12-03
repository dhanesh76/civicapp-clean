package com.visioners.civic.reopen.controller;


import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.SortDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.visioners.civic.auth.userdetails.UserPrincipal;
import com.visioners.civic.complaint.entity.ReopenComplaint;
import com.visioners.civic.staff.entity.Staff;
import com.visioners.civic.reopen.dto.*;
import com.visioners.civic.reopen.service.ReopenService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/departments/reopens")
@RequiredArgsConstructor
public class DepartmentReopenController {

    private final ReopenService reopenService;
    private final com.visioners.civic.staff.service.StaffService staffService;

    @GetMapping
    public ResponseEntity<Page<ReopenSummaryDTO>> listForDept(
            @AuthenticationPrincipal UserPrincipal principal,
            @SortDefault(sort = "createdAt") Pageable page,
            @RequestParam(required = false) com.visioners.civic.complaint.model.ReopenStatus status,
            @RequestParam(required = false) com.visioners.civic.complaint.model.IssueSeverity severity,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date to) {

        Staff officer = staffService.getStaff(principal.getUser());
        Page<com.visioners.civic.complaint.entity.ReopenComplaint> p = reopenService.listReopensForDepartment(officer, page, status, severity, from, to);
        var dtoPage = p.map(r -> ReopenSummaryDTO.builder()
                .reopenId(r.getReopenId())
                .parentComplaintId(r.getParentComplaint().getComplaintId())
                .status(ReopenStatusDTO.valueOf(r.getStatus().name()))
                .severity(r.getParentComplaint().getSeverity())
                .createdAt(r.getCreatedAt())
                .reopenNumber(r.getReopenNumber())
                .build());
        return ResponseEntity.ok(dtoPage);
    }

    @PostMapping("/assign")
    public ResponseEntity<?> assignReopen(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody DeptAssignReopenDTO dto) {

        Staff officer = staffService.getStaff(principal.getUser());
        reopenService.deptAssignReopen(officer, dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reject")
    public ResponseEntity<?> rejectReopen(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody DeptRejectReopenDTO dto) {

        Staff officer = staffService.getStaff(principal.getUser());
        reopenService.deptRejectReopen(officer, dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/approve")
    public ResponseEntity<?> approveReopen(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody DeptApproveReopenDTO dto) {

        Staff officer = staffService.getStaff(principal.getUser());
        reopenService.deptApproveReopen(officer, dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{reopenId}")
    public ResponseEntity<ReopenDetailDTO> getReopenDetailForDept(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String reopenId) {

        Staff officer = staffService.getStaff(principal.getUser());

        ReopenComplaint r = reopenService.getReopenByReopenIdForDepartment(officer, reopenId);

        List<ReopenComplaint> all = reopenService.getAllForParent(r.getParentComplaint());

        List<ReopenSummaryDTO> previous = all.stream()
            .filter(rr -> !rr.getReopenId().equals(r.getReopenId()))
            .map(ReopenSummaryDTO::fromEntity)
            .toList();

        ReopenDetailDTO dto = ReopenDetailDTO.builder()
                .reopenId(r.getReopenId())
                .parentComplaintId(r.getParentComplaint().getComplaintId())
                .status(ReopenStatusDTO.valueOf(r.getStatus().name()))
                .proofUrl(r.getProofUrl())
                .note(r.getNote())
                .lat(r.getLat())
                .lon(r.getLon())
                .reopenNumber(r.getReopenNumber())
                .createdAt(r.getCreatedAt())
                .previousReopens(previous)
                .build();

        return ResponseEntity.ok(dto);
    }
}

