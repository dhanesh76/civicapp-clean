package com.visioners.civic.reopen.controller;


import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.SortDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.visioners.civic.auth.userdetails.UserPrincipal;
import com.visioners.civic.complaint.entity.ReopenComplaint;
import com.visioners.civic.staff.entity.Staff;
import com.visioners.civic.reopen.dto.*;
import com.visioners.civic.reopen.service.ReopenService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/workers/reopens")
@RequiredArgsConstructor
public class WorkerReopenController {

    private final ReopenService reopenService;
    private final com.visioners.civic.staff.service.StaffService staffService;

    @GetMapping
    public ResponseEntity<Page<ReopenSummaryDTO>> listForWorker(
            @AuthenticationPrincipal UserPrincipal principal,
            @SortDefault(sort = "createdAt") Pageable page,
            @RequestParam(required = false) com.visioners.civic.complaint.model.ReopenStatus status,
            @RequestParam(required = false) com.visioners.civic.complaint.model.IssueSeverity severity,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date to) {

        Staff worker = staffService.getStaff(principal.getUser());
        Page<com.visioners.civic.complaint.entity.ReopenComplaint> p = reopenService.listReopensForWorker(worker, page, status, severity, from, to);
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

    @PostMapping(value = "/resolve", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> resolveReopen(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestPart("data") String dto,
            @RequestPart("image") MultipartFile imageFile) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        WorkerResolveDTO request2 = mapper.readValue(dto, WorkerResolveDTO.class);

        Staff worker = staffService.getStaff(principal.getUser());
        reopenService.workerResolveReopen(worker, imageFile, request2);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{reopenId}")
    public ResponseEntity<ReopenDetailDTO> getReopenDetailForWorker(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String reopenId) {

        Staff worker = staffService.getStaff(principal.getUser());

        ReopenComplaint r = reopenService.getReopenByReopenIdForWorker(worker, reopenId);

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

