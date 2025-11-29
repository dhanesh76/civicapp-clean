package com.visioners.civic.complaint.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.visioners.civic.complaint.entity.ComplaintCycle;

@Repository
public interface ComplaintCycleRepository extends JpaRepository<ComplaintCycle, Long> {

    List<ComplaintCycle> findByComplaintIdOrderByCreatedAtDesc(Long complaintId);

    @Query("select max(c.cycleNumber) from ComplaintCycle c where c.complaintId = :complaintId")
    Integer findMaxCycleNumberByComplaintId(Long complaintId);
}
