package com.visioners.civic.complaint.Specifications;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

import org.springframework.data.jpa.domain.Specification;

import com.visioners.civic.complaint.entity.ReopenComplaint;
import com.visioners.civic.complaint.model.ReopenStatus;
import com.visioners.civic.complaint.model.IssueSeverity;

public class ReopenSpecification {

    public static Specification<ReopenComplaint> hasStatus(ReopenStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    // filter by parent complaint severity
    public static Specification<ReopenComplaint> hasSeverity(IssueSeverity severity) {
        return (root, query, cb) -> severity == null ? null : cb.equal(root.get("parentComplaint").get("severity"), severity);
    }

    // filter by department id (reopen.department OR parentComplaint.department)
    public static Specification<ReopenComplaint> hasDepartmentId(Long departmentId) {
        return (root, query, cb) -> {
            if (departmentId == null) return null;
            return cb.equal(root.get("department").get("id"), departmentId);
        };
    }

    // filter by block id using parentComplaint.block
    public static Specification<ReopenComplaint> hasBlockId(Long blockId) {
        return (root, query, cb) -> {
            if (blockId == null) return null;
            return cb.equal(root.get("parentComplaint").get("block").get("id"), blockId);
        };
    }

    // filter by assigned worker (parentComplaint.assignedTo)
    public static Specification<ReopenComplaint> hasAssignedWorkerId(Long workerId) {
        return (root, query, cb) -> workerId == null ? null : cb.equal(root.get("parentComplaint").get("assignedTo").get("id"), workerId);
    }

    // createdAt date range on ReopenComplaint.createdAt (Date -> Instant handling)
    public static Specification<ReopenComplaint> hasDateRange(Date from, Date to) {
        return (root, query, cb) -> {
            Instant fromInst = from == null ? null : from.toInstant();
            Instant toInst = null;
            if (to != null) {
                ZoneId zone = ZoneId.systemDefault();
                LocalDate toDate = to.toInstant().atZone(zone).toLocalDate();
                toInst = toDate.atTime(LocalTime.MAX).atZone(zone).toInstant();
            }
            if (fromInst != null && toInst != null) return cb.between(root.get("createdAt"), fromInst, toInst);
            if (fromInst != null) return cb.greaterThanOrEqualTo(root.get("createdAt"), fromInst);
            if (toInst != null) return cb.lessThanOrEqualTo(root.get("createdAt"), toInst);
            return null;
        };
    }

   public static Specification<ReopenComplaint> hasRaisedByUserId(Long userId) {
        return (root, query, cb) ->
                userId == null ? null :
                cb.equal(root.get("raisedBy").get("id"), userId);
    }
}

