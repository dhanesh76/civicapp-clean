package com.visioners.civic.community.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.visioners.civic.auth.userdetails.UserPrincipal;
import com.visioners.civic.community.entity.CommunityComment;
import com.visioners.civic.community.service.CommunityInteractionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/community/interaction")
@RequiredArgsConstructor
public class CommunityInteractionController {

        private final CommunityInteractionService interactionService;

        @PostMapping("/{complaintId}/support")
        public ResponseEntity<Map<String, Object>> toggleSupport(
                        @PathVariable String complaintId,
                        @AuthenticationPrincipal UserPrincipal principal) {

                boolean supported = interactionService.toggleSupport(
                                principal.getUser(),
                                complaintId);

                return ResponseEntity.ok(
                                Map.of(
                                        "supported", supported)
                                );
        }

        @PostMapping("/{complaintId}/comment")
        public ResponseEntity<?> addComment(
                        @PathVariable String complaintId,
                        @RequestBody Map<String, String> body,
                        @AuthenticationPrincipal UserPrincipal principal) {
                String text = body.get("comment");

                CommunityComment comment = interactionService.addComment(
                                principal.getUser(),
                                complaintId,
                                text);

                return ResponseEntity.ok(comment);
        }
}
