package com.visioners.civic.reopen.controller;

import java.io.IOException;
import java.util.Comparator;
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
import com.visioners.civic.reopen.dto.*;
import com.visioners.civic.reopen.repository.ReopenComplaintRepository;
import com.visioners.civic.reopen.service.ReopenService;
import com.visioners.civic.user.entity.Users;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users/reopens")
@RequiredArgsConstructor
public class UserReopenController {

    private final ReopenService reopenService;
    private final ReopenComplaintRepository reopenRepo;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReopenCreatedResponse> reopenComplaint(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestPart("data") String request,
            @RequestPart("proofImage") MultipartFile proofImage) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        ReopenRequest request2 = mapper.readValue(request, ReopenRequest.class);
        
        var resp = reopenService.createReopen(proofImage, request2, principal);
        return ResponseEntity.ok(resp);
    }

    @GetMapping
    public ResponseEntity<Page<ReopenSummaryDTO>> listUserReopens(
            @AuthenticationPrincipal UserPrincipal principal,
            @SortDefault(sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable page,
            @RequestParam(required = false) com.visioners.civic.complaint.model.ReopenStatus status,
            @RequestParam(required = false) com.visioners.civic.complaint.model.IssueSeverity severity,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date to) {

        
        var user = principal.getUser(); // obtain Users entity from principal using UsersRepository/Service; we assume you can inject UsersRepository here
        Page<ReopenComplaint> pageResult = reopenService.listReopensForUser(user, page, status, severity, from, to);
        var dtoPage = pageResult.map(r -> ReopenSummaryDTO.builder()
                .reopenId(r.getReopenId())
                .parentComplaintId(r.getParentComplaint().getComplaintId())
                .status(ReopenStatusDTO.valueOf(r.getStatus().name()))
                .severity(r.getParentComplaint().getSeverity())
                .createdAt(r.getCreatedAt())
                .reopenNumber(r.getReopenNumber())
                .build());
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/{reopenId}")
        public ResponseEntity<ReopenDetailDTO> getReopenDetail(
                @AuthenticationPrincipal UserPrincipal principal,
                @PathVariable String reopenId) {

        // 1️⃣ Fetch correct Users entity
        Users user = principal.getUser();

        // 2️⃣ Fetch the reopen complaint WITH access validation
        ReopenComplaint r = reopenService.getReopenByReopenIdForUser(user, reopenId);

        // 3️⃣ Fetch ONLY reopens belonging to THIS parent complaint
        List<ReopenComplaint> allForThisComplaint =
                reopenRepo.findByParentComplaint(r.getParentComplaint());

        // 4️⃣ Build previous reopens (excluding the current one)
        List<ReopenSummaryDTO> previous = allForThisComplaint.stream()
                .filter(rr -> !rr.getReopenId().equals(r.getReopenId()))
                .sorted(Comparator.comparing(ReopenComplaint::getCreatedAt))
                .map(rr -> ReopenSummaryDTO.builder()
                        .reopenId(rr.getReopenId())
                        .parentComplaintId(rr.getParentComplaint().getComplaintId())
                        .status(ReopenStatusDTO.valueOf(rr.getStatus().name()))
                        .severity(rr.getParentComplaint().getSeverity())
                        .createdAt(rr.getCreatedAt())
                        .reopenNumber(rr.getReopenNumber())
                        .build())
                .toList();

        // 5️⃣ Build final detail response
        ReopenDetailDTO detail = ReopenDetailDTO.builder()
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

        return ResponseEntity.ok(detail);
        }

}
