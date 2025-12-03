package com.visioners.civic.blockadmin.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.visioners.civic.auth.userdetails.UserPrincipal;
import com.visioners.civic.blockadmin.dto.BlockDepartmentListDto;
import com.visioners.civic.blockadmin.service.BlockAdminService;
import com.visioners.civic.complaint.entity.Block;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/api/blocks/ba")
@PreAuthorize("hasRole('BLOCK_ADMIN')")
@RequiredArgsConstructor
public class BlockAdminController {

    private final BlockAdminService blockAdminService;

    @GetMapping("/departments")
    public ResponseEntity<BlockDepartmentListDto> getBlockDepartments(@AuthenticationPrincipal UserPrincipal principal) {
        
        Block block =  blockAdminService.getAdminBlockByUser(principal.getUser());
        var departments = blockAdminService.getBlockDepartments(block);
        
        return ResponseEntity.ok(new BlockDepartmentListDto(block.getId(), departments));
    }
}
