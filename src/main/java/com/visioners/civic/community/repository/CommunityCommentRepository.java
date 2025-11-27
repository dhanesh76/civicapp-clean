package com.visioners.civic.community.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.visioners.civic.community.entity.CommunityComment;
import com.visioners.civic.complaint.entity.Complaint;

@Repository
public interface CommunityCommentRepository extends JpaRepository<CommunityComment, Long> {

    List<CommunityComment> findByComplaintOrderByCreatedAtDesc(Complaint complaint);

    long countByComplaint(Complaint complaint);
}

