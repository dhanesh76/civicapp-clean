package com.visioners.civic.reopen.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.visioners.civic.complaint.entity.ReopenComplaint;
import com.visioners.civic.user.entity.Users;

public interface ReopenComplaintRepository extends JpaRepository<ReopenComplaint, Long>, JpaSpecificationExecutor<ReopenComplaint> {

    Optional<ReopenComplaint> findByReopenId(String reopenId);

    List<ReopenComplaint> findByParentComplaint(com.visioners.civic.complaint.entity.Complaint parent);

    Page<ReopenComplaint> findByRaisedBy(Users user, Pageable pageable);

    Optional<ReopenComplaint> findTopByParentComplaintOrderByCreatedAtDesc(com.visioners.civic.complaint.entity.Complaint parent);
}
