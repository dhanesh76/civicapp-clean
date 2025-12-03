package com.visioners.civic.da.controller;

import com.visioners.civic.auth.userdetails.UserPrincipal;
import com.visioners.civic.complaint.entity.Block;
import com.visioners.civic.da.service.DistrictAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

@RestController
@RequestMapping("/api/da")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DA')")
public class DistrictAdminController {

    private final DistrictAdminService districtAdminService;

    @GetMapping("/blocks")
    public ResponseEntity<List<Block>> getBlocks(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(districtAdminService.getBlocks(principal));
    }
}
