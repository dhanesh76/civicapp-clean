package com.visioners.civic.staff.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.visioners.civic.auth.userdetails.UserPrincipal;
import com.visioners.civic.staff.dto.CreateStaffDTO;
import com.visioners.civic.staff.dto.StaffDetailDTO;
import com.visioners.civic.staff.dto.StaffView;
import com.visioners.civic.staff.entity.Staff;
import com.visioners.civic.staff.service.StaffService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/staff")
@PreAuthorize("hasRole('OFFICER')")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

    /** Create a new staff */
    @PostMapping
    public ResponseEntity<StaffDetailDTO> createStaff(@Valid @RequestBody CreateStaffDTO dto) {
        StaffDetailDTO staff = staffService.createStaff(dto);
        return ResponseEntity.ok(staff);
    }

    /** Get list of field workers under the officer's jurisdiction (optional name filter) */
    @GetMapping("/field-workers")
    public ResponseEntity<List<StaffView>> getFieldWorkers(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(value = "name", required = false, defaultValue = "") String name) {

        List<StaffView> workers = staffService.getFieldWorkers(principal, name);
        return ResponseEntity.ok(workers);
    }

    /** Get staff details by staff ID */
    @GetMapping("/{staffId}")
    public ResponseEntity<StaffDetailDTO> getStaffById(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long staffId) {
        
        Staff staff = staffService.getStaff(principal, staffId);
        return ResponseEntity.ok(converToStaffDetailDTO(staff));
    }

    private StaffDetailDTO converToStaffDetailDTO(Staff s){
        return StaffDetailDTO.builder()
                        .id(s.getId())
                        .username(s.getUser().getUsername())
                        .mobileNumber(s.getUser().getMobileNumber())
                        .roleName(s.getUser().getRole().getName())
                        .departmentName(s.getDepartment().getName())
                        .districtName(s.getDistrict().getName())
                        .blockName(s.getBlock().getName())
                        .createdAt(s.getCreatedAt())
                        .build();
    }
}
