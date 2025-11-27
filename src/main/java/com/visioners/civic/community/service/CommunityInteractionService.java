package com.visioners.civic.community.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.visioners.civic.community.dto.CommunityCommentDTO;
import com.visioners.civic.community.dto.ComplaintCommunityDetailDTO;
import com.visioners.civic.community.dto.ComplaintCommunityInteractionDto;
import com.visioners.civic.community.dto.ComplaintInteractionSummaryDTO;
import com.visioners.civic.community.entity.CommunityComment;
import com.visioners.civic.community.entity.CommunitySupport;
import com.visioners.civic.community.repository.CommunityCommentRepository;
import com.visioners.civic.community.repository.CommunitySupportRepository;
import com.visioners.civic.complaint.entity.Complaint;
import com.visioners.civic.complaint.exception.ComplaintNotFoundException;
import com.visioners.civic.complaint.repository.ComplaintRepository;
import com.visioners.civic.user.entity.Users;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommunityInteractionService {

    private final CommunitySupportRepository supportRepo;
    private final CommunityCommentRepository commentRepo;
    private final ComplaintRepository complaintRepo;

    /** SUPPORT / UNSUPPORT toggle */
    @Transactional
    public boolean toggleSupport(Users user, String complaintId) {

        Complaint complaint = complaintRepo.findByComplaintId(complaintId)
                .orElseThrow(() -> new ComplaintNotFoundException("Complaint not found"));

        Optional<CommunitySupport> existing = supportRepo.findByUserAndComplaint(user, complaint);

        if (existing.isPresent()) {
            // Undo support
            supportRepo.delete(existing.get());
            return false; // now NOT supported
        }

        // Add support
        supportRepo.save(
                CommunitySupport.builder()
                        .user(user)
                        .complaint(complaint)
                        .build());

        return true; // now supported
    }

    /** COMMENT */
    @Transactional
    public CommunityCommentDTO addComment(Users user, String complaintId, String text) {

        Complaint complaint = complaintRepo.findByComplaintId(complaintId)
                .orElseThrow(() -> new ComplaintNotFoundException("Complaint not found"));

        CommunityComment comment = CommunityComment.builder()
                .user(user)
                .complaint(complaint)
                .comment(text)
                .build();
        
        commentRepo.save(comment);
        return new CommunityCommentDTO(comment.getUser().getUsername(), comment.getComment(), comment.getCreatedAt());
    }

    /** Count helpers */
    public long getSupportCount(Complaint c) {
        return supportRepo.countByComplaint(c);
    }

    public long getCommentCount(Complaint c) {
        return commentRepo.countByComplaint(c);
    }

    public List<CommunityComment> getComments(String complaintId) {
        Complaint complaint = complaintRepo.findByComplaintId(complaintId)
                .orElseThrow(() -> new ComplaintNotFoundException("Complaint not found"));

        return commentRepo.findByComplaintOrderByCreatedAtDesc(complaint);
    }

    /** to check UI state */
    public boolean userHasSupported(Users user, Complaint complaint) {
        return supportRepo.findByUserAndComplaint(user, complaint).isPresent();
    }

    public ComplaintInteractionSummaryDTO getSummary(Complaint complaint) {
        return new ComplaintInteractionSummaryDTO(
                supportRepo.countByComplaint(complaint),
                commentRepo.countByComplaint(complaint));
    }

    public ComplaintCommunityInteractionDto getDetail(Complaint complaint, Users viewer) {

        long supportCount = supportRepo.countByComplaint(complaint);
        long commentCount = commentRepo.countByComplaint(complaint);

        boolean supported = false;
        if (viewer != null) {
            supported = supportRepo.findByUserAndComplaint(viewer, complaint).isPresent();
        }

        List<CommunityCommentDTO> comments = commentRepo.findByComplaintOrderByCreatedAtDesc(complaint)
                .stream()
                .map(c -> new CommunityCommentDTO(
                        c.getUser().getUsername(),
                        c.getComment(),
                        c.getCreatedAt()))
                .toList();

        return new ComplaintCommunityInteractionDto(
                supportCount,
                commentCount,
                supported,
                comments);
    }

     public ComplaintCommunityDetailDTO getDetail(Complaint complaint) {

        long supportCount = supportRepo.countByComplaint(complaint);
        long commentCount = commentRepo.countByComplaint(complaint);

        List<CommunityCommentDTO> comments = commentRepo.findByComplaintOrderByCreatedAtDesc(complaint)
                .stream()
                .map(c -> new CommunityCommentDTO(
                        c.getUser().getUsername(),
                        c.getComment(),
                        c.getCreatedAt()))
                .toList();

        return new ComplaintCommunityDetailDTO(
                supportCount,
                commentCount,
                comments);
    }
}
