package com.visioners.civic.community.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.visioners.civic.auth.userdetails.UserPrincipal;
import com.visioners.civic.community.service.CommunityComplaintService;
import com.visioners.civic.complaint.dto.departmentcomplaintdtos.ComplaintViewDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityComplaintController {
    
    private final double COMMUNITY_RADIUS = 2000;
    
    private final CommunityComplaintService communityService;

    @GetMapping
    public ResponseEntity<Page<ComplaintViewDTO>> getNearbyComplaints(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam double lat,
            @RequestParam double lon,
            Pageable pageable
    ) {
        return ResponseEntity.ok(communityService.getNearby(principal.getUser(), lat, lon, COMMUNITY_RADIUS, pageable));
    }
}
