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
@RequestMapping("/api/blocks/reopens")
@RequiredArgsConstructor
public class BlockReopenController {

    private final ReopenService reopenService;
    private final com.visioners.civic.staff.service.StaffService staffService;

    @GetMapping
    public ResponseEntity<Page<ReopenSummaryDTO>> listForBA(
            @AuthenticationPrincipal UserPrincipal principal,
            @SortDefault(sort = "createdAt") Pageable page,
            @RequestParam(required = false) com.visioners.civic.complaint.model.IssueSeverity severity,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date to) {

        Staff ba = staffService.getStaff(principal.getUser());
        Page<com.visioners.civic.complaint.entity.ReopenComplaint> p = reopenService.listReopensForBlockAdmin(ba, page, severity, from, to);

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

    @PostMapping("/reopen/route")
    public ResponseEntity<?> routeReopen(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody BARouteReopenDTO dto) {

        Staff ba = staffService.getStaff(principal.getUser());
        reopenService.baRouteReopen(ba, dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reopen/reject")
    public ResponseEntity<?> rejectReopen(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody BARejectReopenDTO dto) {

        Staff ba = staffService.getStaff(principal.getUser());
        reopenService.baRejectReopen(ba, dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{reopenId}")
    public ResponseEntity<ReopenDetailDTO> getReopenDetailForBA(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String reopenId) {

        Staff ba = staffService.getStaff(principal.getUser());

        ReopenComplaint r = reopenService.getReopenByReopenIdForBlockAdmin(ba, reopenId);

        // fetch previous reopens only for same complaint
        List<ReopenComplaint> all = reopenService.getAllForParent(r.getParentComplaint());

        List<ReopenSummaryDTO> previous = all.stream()
            .filter(rr -> !rr.getReopenId().equals(r.getReopenId()))
            .map(ReopenSummaryDTO::fromEntity) // use builder like earlier
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

