package com.visioners.civic.blockadmin.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.visioners.civic.auth.userdetails.UserPrincipal;
import com.visioners.civic.blockadmin.dto.*;
import com.visioners.civic.blockadmin.service.BlockAdminService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/blocks/complaints")
@RequiredArgsConstructor
@PreAuthorize("hasRole('BLOCK_ADMIN')")
public class BlockComplaintController {

    private final BlockAdminService blockAdminService;

    // ============================================================
    // 3. PENDING (MANUAL ROUTING) FOR BA
    // ============================================================
    @GetMapping("/pending")
    public ResponseEntity<Page<BAComplaintSummaryDTO>> getPendingComplaints(
            @AuthenticationPrincipal UserPrincipal principal,
            
            @SortDefault(sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.ASC) Pageable pageable,
                        
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat
            (iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.util.Date from,
            
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.util.Date to
    ) {
        return ResponseEntity.ok(blockAdminService.getPendingComplaints(principal, pageable, from, to));
    }

    @PostMapping("/route-pending")
    public ResponseEntity<?> routePendingComplaint(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody BARoutePendingDTO dto) {

        blockAdminService.routePendingComplaint(principal, dto);
        return ResponseEntity.ok().build();
    }
}
