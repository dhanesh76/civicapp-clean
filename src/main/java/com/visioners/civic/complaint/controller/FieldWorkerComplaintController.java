package com.visioners.civic.complaint.controller;

import java.io.IOException;
import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

import com.visioners.civic.auth.userdetails.UserPrincipal;
import com.visioners.civic.complaint.dto.ComplaintView;
import com.visioners.civic.complaint.dto.departmentcomplaintdtos.ComplaintRejectView;
import com.visioners.civic.complaint.dto.departmentcomplaintdtos.ComplaintViewDTO;
import com.visioners.civic.complaint.dto.departmentcomplaintdtos.ResolveComplaint;
import com.visioners.civic.complaint.dto.fieldworkerdtos.FieldWorkerComplaintStatsDTO;
import com.visioners.civic.complaint.service.FieldWorkerComplaintService;

import org.springframework.web.bind.annotation.RequestBody;

import com.twilio.twiml.fax.Receive;
import com.visioners.civic.complaint.model.IssueSeverity;
import com.visioners.civic.complaint.model.IssueStatus;

import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/workers/complaints")
public class FieldWorkerComplaintController {

    private final FieldWorkerComplaintService fieldWorkerComplaintService;

    @GetMapping
    public ResponseEntity<Page<ComplaintView>> viewAssignedComplaints(
            @AuthenticationPrincipal UserPrincipal principal,
            Pageable page,
            @RequestParam(required = false) IssueSeverity severity,
            @RequestParam(required = false) IssueStatus status,
            @RequestParam(required = false) Date from,
            @RequestParam(required = false) Date to) {

        Page<ComplaintView> response = fieldWorkerComplaintService
                .viewAssignedComplaints(principal, page, severity, from, to);

        return ResponseEntity.ok(response);
    }

    @PostMapping(value="/resolve", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ComplaintView> resolveProblem(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestPart MultipartFile imageFile,
            @Valid @RequestPart ResolveComplaint resolveComplaintDto) throws IOException {

        ComplaintView response = fieldWorkerComplaintService.resolve(principal, imageFile, resolveComplaintDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/rejected")
    public ResponseEntity<Page<ComplaintRejectView>> rejectedComplaints(
            @AuthenticationPrincipal UserPrincipal principal,
            Pageable page) {

        Page<ComplaintRejectView> response = fieldWorkerComplaintService.rejectedComplaints(principal, page);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    public ResponseEntity<FieldWorkerComplaintStatsDTO> getComplaintStats(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Date from,
            @RequestParam(required = false) Date to) {

        FieldWorkerComplaintStatsDTO response = fieldWorkerComplaintService.getComplaintStats(principal, from, to);
        return ResponseEntity.ok(response);
    }

    @GetMapping("details/{complaintId}")
    public ResponseEntity<ComplaintViewDTO> getComplaintDetail(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long complaintId) {

        ComplaintViewDTO response = fieldWorkerComplaintService.getComplaintDetail(principal, complaintId);
        return ResponseEntity.ok(response);
    }
}
