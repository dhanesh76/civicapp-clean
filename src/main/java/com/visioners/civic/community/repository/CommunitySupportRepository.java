package com.visioners.civic.community.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.visioners.civic.community.entity.CommunitySupport;
import com.visioners.civic.complaint.entity.Complaint;
import com.visioners.civic.user.entity.Users;

@Repository
public interface CommunitySupportRepository extends JpaRepository<CommunitySupport, Long> {

    Optional<CommunitySupport> findByUserAndComplaint(Users user, Complaint complaint);

    long countByComplaint(Complaint complaint);
}
