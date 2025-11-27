package com.visioners.civic.community.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.visioners.civic.community.repository.CommunityComplaintRepository;
import com.visioners.civic.complaint.dto.departmentcomplaintdtos.ComplaintViewDTO;
import com.visioners.civic.complaint.entity.Complaint;
import com.visioners.civic.complaint.service.ComplaintService;
import com.visioners.civic.user.entity.Users;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommunityComplaintService {

    private final CommunityComplaintRepository communityComplaintRepository;
    private final ComplaintService complaintService;
    
    public Page<ComplaintViewDTO> getNearby(Users user, double lat, double lon, double radius, Pageable pageable) {
        Page<Complaint> complaints = communityComplaintRepository.findNearby(user.getId(), lat, lon, radius, pageable);

        // Reuse the central mapping to ensure all DTO fields (including feedback)
        // are populated consistently for nearby complaints.
        return complaints.map(complaintService::mapToComplaintViewDTO);
    }
}
