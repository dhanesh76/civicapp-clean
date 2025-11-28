package com.visioners.civic.complaint.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.visioners.civic.complaint.entity.Complaint;
import com.visioners.civic.complaint.entity.ComplaintFeedback;
import com.visioners.civic.user.entity.Users;

@Repository
public interface ComplaintFeedbackRepository extends JpaRepository<ComplaintFeedback, Long> {

    public Optional<ComplaintFeedback> findByUserAndComplaint(Users user, Complaint complaint);

    @Query(value = "SELECT AVG(complaint_feedback.rating) " +
            "FROM complaint " +
            "JOIN complaint_feedback ON complaint.id = complaint_feedback.complaint_id " +
            "WHERE complaint.department_id = :deptId", nativeQuery = true)
    public Double findDepartmentAvgRating(@Param("deptId") Long deptId);
}
