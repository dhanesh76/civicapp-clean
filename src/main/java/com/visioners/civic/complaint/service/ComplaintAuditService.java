package com.visioners.civic.complaint.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.visioners.civic.complaint.entity.ComplaintCycle;
import com.visioners.civic.complaint.model.ActionType;
import com.visioners.civic.complaint.model.ActorType;
import com.visioners.civic.complaint.repository.ComplaintCycleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ComplaintAuditService {

    private final ComplaintCycleRepository cycleRepo;

    public List<ComplaintCycle> getHistory(Long complaintId) {
        return cycleRepo.findByComplaintIdOrderByCreatedAtDesc(complaintId);
    }

    public int getNextCycleNumber(Long complaintId) {
        Integer max = cycleRepo.findMaxCycleNumberByComplaintId(complaintId);
        if (max == null) return 1;
        return max + 1;
    }

    @Transactional
    public ComplaintCycle log(
            Long complaintId,
            Integer cycleNumber,
            ActionType actionType,
            ActorType actorType,
            Long actorId,
            String oldStatus,
            String newStatus,
            String note,
            String solutionImageUrl,
            String proofImageUrl,
            Double coordsLat,
            Double coordsLon) {

        int cn = cycleNumber == null ? getNextCycleNumber(complaintId) : cycleNumber;

        ComplaintCycle c = ComplaintCycle.builder()
                .complaintId(complaintId)
                .cycleNumber(cn)
                .actionType(actionType)
                .actorType(actorType)
                .actorId(actorId)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .note(note)
                .solutionImageUrl(solutionImageUrl)
                .proofImageUrl(proofImageUrl)
                .coordsLat(coordsLat)
                .coordsLon(coordsLon)
                .build();

        return cycleRepo.save(c);
    }
}
