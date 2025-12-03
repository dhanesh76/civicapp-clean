package com.visioners.civic.complaint.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.visioners.civic.auth.userdetails.UserPrincipal;
import com.visioners.civic.complaint.dto.feedback.CreateFeedbackDTO;
import com.visioners.civic.complaint.dto.feedback.ViewFeedbackDTO;
import com.visioners.civic.complaint.service.ComplaintFeedbackService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController 
@RequiredArgsConstructor
@RequestMapping("/api/complaints/feedback")
public class ComplaintFeedbackController {
    
    private final ComplaintFeedbackService complaintFeedbackService;

    @PostMapping("/{complaintId}")
    public ResponseEntity<ViewFeedbackDTO> giveFeedback(@AuthenticationPrincipal UserPrincipal principal, @PathVariable("complaintId") String complaintId, @Valid @RequestBody CreateFeedbackDTO feedbackDTO) {

        return ResponseEntity.ok(complaintFeedbackService.giveFeedback(principal.getUser(), complaintId, feedbackDTO));        
    }
}
