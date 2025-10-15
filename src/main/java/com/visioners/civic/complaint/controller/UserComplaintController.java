package com.visioners.civic.complaint.controller;

import java.io.IOException;
import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.visioners.civic.auth.userdetails.UserPrincipal;
import com.visioners.civic.complaint.dto.usercomplaintdtos.ComplaintDetailDTO;
import com.visioners.civic.complaint.dto.usercomplaintdtos.ComplaintRaiseRequest;
import com.visioners.civic.complaint.dto.usercomplaintdtos.ComplaintRaiseResponseDTO;
import com.visioners.civic.complaint.dto.usercomplaintdtos.ComplaintStatisticsDTO;
import com.visioners.civic.complaint.dto.usercomplaintdtos.ComplaintSummaryDTO;
import com.visioners.civic.complaint.model.IssueSeverity;
import com.visioners.civic.complaint.model.IssueStatus;
import com.visioners.civic.complaint.service.UserComplaintService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users/complaints")
@RequiredArgsConstructor
public class UserComplaintController {

    private final UserComplaintService userComplaintService;

    /** Raise a new complaint */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ComplaintRaiseResponseDTO> raiseComplaint(
            @Valid @RequestPart ComplaintRaiseRequest complaintRaiseDto,
            @RequestPart MultipartFile imageFile,
            @AuthenticationPrincipal UserPrincipal principal) throws IOException {

        ComplaintRaiseResponseDTO response = userComplaintService.raiseComplaint(complaintRaiseDto, imageFile, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** Paginated complaints for user with optional filters */
    @GetMapping
    public ResponseEntity<Page<ComplaintSummaryDTO>> getAllComplaints(
            @AuthenticationPrincipal UserPrincipal principal,
            Pageable page,
            @RequestParam(required = false) IssueSeverity severity,
            @RequestParam(required = false) IssueStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date to) {

        Page<ComplaintSummaryDTO> complaints = userComplaintService.getAllComplaints(principal, page, severity, status, from, to);
        return ResponseEntity.ok(complaints);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComplaintDetailDTO> getComplaintDetail(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {

        ComplaintDetailDTO detail = userComplaintService.getComplaintDetail(principal, id);
        return ResponseEntity.ok(detail);
    }

    /** User complaint statistics */
    @GetMapping("/stats")
    public ResponseEntity<ComplaintStatisticsDTO> getUserStats(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date to) {

        ComplaintStatisticsDTO stats = userComplaintService.getUserStatistics(principal, from, to);
        return ResponseEntity.ok(stats);
    }
}
