package com.visioners.civic.complaint.controller;

import java.io.IOException;
import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.http.MediaType;
import com.visioners.civic.auth.userdetails.UserPrincipal;
import com.visioners.civic.complaint.dto.ComplaintView;
import com.visioners.civic.complaint.dto.departmentcomplaintdtos.ComplaintRejectView;
import com.visioners.civic.complaint.dto.departmentcomplaintdtos.ComplaintViewDTO;
import com.visioners.civic.complaint.dto.fieldworkerdtos.FieldWorkerComplaintStatsDTO;
import com.visioners.civic.complaint.dto.fieldworkerdtos.ResolveComplaintDto;
import com.visioners.civic.complaint.model.IssueSeverity;
import com.visioners.civic.complaint.model.IssueStatus;
import com.visioners.civic.complaint.service.FieldWorkerComplaintService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('FIELD_WORKER')")
@RequestMapping("/api/workers/complaints")
public class FieldWorkerComplaintController {

    private final FieldWorkerComplaintService fieldWorkerComplaintService;

    @GetMapping
    public ResponseEntity<Page<ComplaintView>> viewAssignedComplaints(
            @AuthenticationPrincipal UserPrincipal principal,
            @SortDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable page,
            @RequestParam(required = false) IssueSeverity severity,
            @RequestParam(required = false) IssueStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date to) {

        Page<ComplaintView> response = fieldWorkerComplaintService
                .viewAssignedComplaints(principal, page, severity, from, to);

        return ResponseEntity.ok(response);
    }
    
    @PostMapping(value = "/resolve", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ComplaintView> resolveProblem(@AuthenticationPrincipal UserPrincipal principal,
            @RequestPart MultipartFile imageFile, @RequestPart("data") @Valid ResolveComplaintDto resolveComplaintDto)
            throws IOException {
        ComplaintView response = fieldWorkerComplaintService.resolve(principal, imageFile, resolveComplaintDto);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/rejected")
    public ResponseEntity<Page<ComplaintRejectView>> rejectedComplaints(
            @AuthenticationPrincipal UserPrincipal principal,
            @SortDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable page) {

        Page<ComplaintRejectView> response = fieldWorkerComplaintService.rejectedComplaints(principal, page);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    public ResponseEntity<FieldWorkerComplaintStatsDTO> getComplaintStats(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date to) {

        FieldWorkerComplaintStatsDTO response = fieldWorkerComplaintService.getComplaintStats(principal, from, to);
        return ResponseEntity.ok(response);
    }

    @GetMapping("details/{complaintId}")
    public ResponseEntity<ComplaintViewDTO> getComplaintDetail(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String complaintId) {

        ComplaintViewDTO response = fieldWorkerComplaintService.getComplaintDetail(principal, complaintId);
        return ResponseEntity.ok(response);
    }
}
