package com.visioners.civic.da.service;

import com.visioners.civic.auth.userdetails.UserPrincipal;
import com.visioners.civic.complaint.entity.Block;
import com.visioners.civic.complaint.repository.BlockRepository;
import com.visioners.civic.staff.service.StaffService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DistrictAdminService {

    private final BlockRepository blockRepository;
    private final StaffService service;

    public List<Block> getBlocks(UserPrincipal principal) {
        Long districtId =  service.getStaff(principal.getUser()).getDistrict().getId();
        return blockRepository.findByDistrictId(districtId);
    }
}
