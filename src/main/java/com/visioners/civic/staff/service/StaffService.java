package com.visioners.civic.staff.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.visioners.civic.auth.userdetails.UserPrincipal;
import com.visioners.civic.complaint.entity.Block;
import com.visioners.civic.complaint.entity.Department;
import com.visioners.civic.complaint.entity.District;
import com.visioners.civic.complaint.repository.BlockRepository;
import com.visioners.civic.complaint.repository.DepartmentRepository;
import com.visioners.civic.complaint.repository.DistrictRepository;
import com.visioners.civic.exception.RoleNotFoundException;
import com.visioners.civic.role.entity.Role;
import com.visioners.civic.role.repository.RoleRepository;
import com.visioners.civic.staff.dto.CreateStaffDTO;
import com.visioners.civic.staff.dto.StaffDetailDTO;
import com.visioners.civic.staff.dto.StaffView;
import com.visioners.civic.staff.entity.Staff;
import com.visioners.civic.staff.repository.StaffRepository;
import com.visioners.civic.staff.specification.StaffSpecification;
import com.visioners.civic.user.entity.Users;
import com.visioners.civic.user.repository.UsersRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StaffService {

    private final StaffRepository staffRepository;
    private final UsersRepository usersRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final DistrictRepository districtRepository;
    private final BlockRepository blockRepository;
    private final PasswordEncoder passwordEncoder;

    /** Create a new Staff with user */
    @Transactional
    public StaffDetailDTO createStaff(CreateStaffDTO dto) {

        // Check if mobile number already exists
        if (usersRepository.existsByMobileNumber(dto.getMobileNumber())) {
            throw new DuplicateResourceException("Mobile number already in use: " + dto.getMobileNumber());
        }

        // Validate role
        Role role = roleRepository.findByName(dto.getRoleName())
                .orElseThrow(() -> new RoleNotFoundException("Role not found: " + dto.getRoleName()));

        // Validate department, district, block
        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new EntityNotFoundException("Department not found"));
        District district = districtRepository.findById(dto.getDistrictId())
                .orElseThrow(() -> new EntityNotFoundException("District not found"));
        Block block = blockRepository.findById(dto.getBlockId())
                .orElseThrow(() -> new EntityNotFoundException("Block not found"));

        // Create Users entity
        Users user = new Users();
        user.setUsername(dto.getUsername());
        user.setMobileNumber(dto.getMobileNumber());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(role);
        user.setVerified(true); // can be false if verification required
        usersRepository.save(user);

        // Create Staff entity
        Staff staff = new Staff();
        staff.setUser(user);
        staff.setDepartment(department);
        staff.setDistrict(district);
        staff.setBlock(block);
        staffRepository.save(staff);

        // Map to DTO
        return StaffDetailDTO.builder()
                .id(staff.getId())
                .username(user.getUsername())
                .mobileNumber(user.getMobileNumber())
                .departmentName(department.getName())
                .districtName(district.getName())
                .blockName(block.getName())
                .roleName(role.getName())
                .createdAt(staff.getCreatedAt())
                .build();
    }

    /** Get all field workers under the officer's jurisdiction */
    public List<StaffView> getFieldWorkers(UserPrincipal principal, String name) {

        Staff officer = staffRepository.findByUser(principal.getUser())
                .orElseThrow(() -> new EntityNotFoundException("Officer not found"));

        Role fieldWorkerRole = roleRepository.findByName("FIELD_WORKER")
                .orElseThrow(() -> new RoleNotFoundException("Role not found: FIELD_WORKER"));

        Specification<Staff> spec = Specification.unrestricted();
        spec = spec.and(StaffSpecification.hasDepartment(officer.getDepartment()));
        spec=spec.and(StaffSpecification.hasDistrict(officer.getDistrict()))
                .and(StaffSpecification.hasBlock(officer.getBlock()))
                .and(StaffSpecification.hasRole(fieldWorkerRole))
                .and(StaffSpecification.hasName(name));

        return staffRepository.findAll(spec).stream()
                .map(s -> new StaffView(s.getId(), s.getUser().getUsername()))
                .collect(Collectors.toList());
    }

    /** Get staff by Users entity */
    public Staff getStaff(Users user) {
        return staffRepository.findByUser(user)
                .orElseThrow(() -> new EntityNotFoundException("Staff not found for user: " + user.getUsername()));
    }

    /** Get staff by ID */
    public Staff getStaff(Long staffId) {
        return staffRepository.findById(staffId)
                .orElseThrow(() -> new EntityNotFoundException("Staff not found with ID: " + staffId));
    }
}
