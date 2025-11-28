package com.visioners.civic.complaint.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.visioners.civic.complaint.dto.feedback.CreateFeedbackDTO;
import com.visioners.civic.complaint.dto.feedback.ViewFeedbackDTO;
import com.visioners.civic.complaint.entity.Complaint;
import com.visioners.civic.complaint.entity.ComplaintFeedback;
import com.visioners.civic.complaint.exception.ComplaintNotFoundException;
import com.visioners.civic.complaint.model.IssueStatus;
import com.visioners.civic.user.entity.Users;

import lombok.RequiredArgsConstructor;

import com.visioners.civic.complaint.repository.ComplaintFeedbackRepository;
import com.visioners.civic.complaint.repository.ComplaintRepository;;

@RequiredArgsConstructor
@Service
public class ComplaintFeedbackService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintFeedbackRepository feedbackRepository;

    @Transactional
    public ViewFeedbackDTO giveFeedback(Users user, String complaintId, CreateFeedbackDTO feedbackDTO) {
        Complaint complaint = complaintRepository.findByComplaintId(complaintId)
                .orElseThrow(() -> new ComplaintNotFoundException("no co,plaint with complaint id: " + complaintId));

        if (complaint.getStatus() != IssueStatus.CLOSED) {
            throw new IllegalStateException("Feedback allowed only on CLOSED complaints");
        }

        // Allow only one feedback per complaint
        if (feedbackRepository.findByUserAndComplaint(user, complaint).isPresent()) {
            throw new IllegalStateException("Feedback already submitted");
        }

        ComplaintFeedback complaintFeedback = ComplaintFeedback.builder()
                .user(user)
                .complaint(complaint)
                .rating(feedbackDTO.rating())
                .comment(feedbackDTO.comment())
                .build();

        feedbackRepository.save(complaintFeedback);

        return new ViewFeedbackDTO(complaintFeedback.getComment(), complaintFeedback.getRating(),
                complaintFeedback.getCreatedAt());
    }

    public double findDepartmentAvgRating(Long departmentId){
        return feedbackRepository.findDepartmentAvgRating(departmentId);
    }

    public Optional<ComplaintFeedback> getFeedbacks(Users user, Complaint complaint){
        return feedbackRepository.findByUserAndComplaint(user, complaint);
    }
}
