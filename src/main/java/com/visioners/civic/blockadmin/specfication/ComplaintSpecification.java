package com.visioners.civic.blockadmin.specfication;

import java.util.Date;

import org.springframework.data.jpa.domain.Specification;

import com.visioners.civic.complaint.entity.Complaint;
import com.visioners.civic.complaint.entity.Department;
import com.visioners.civic.staff.entity.Staff;
import com.visioners.civic.user.entity.Users;
import com.visioners.civic.complaint.model.IssueSeverity;
import com.visioners.civic.complaint.model.IssueStatus;

public class ComplaintSpecification {

    public static Specification<Complaint> hasRaisedBy(Users user){
        return (root, query, builder) ->
                user == null ? builder.conjunction() : builder.equal(root.get("raisedBy"), user);
    }

    public static Specification<Complaint> hasAssignedTo(Staff staff){
        return (root, query, builder) ->
                staff == null ? builder.conjunction() : builder.equal(root.get("assignedTo"), staff);
    }

    public static Specification<Complaint> hasDepartment(Department department){
        return (root, query, builder) ->
                department == null ? builder.conjunction() : builder.equal(root.get("department"), department);
    }

    public static Specification<Complaint> hasSeverity(IssueSeverity severity){
        return (root, query, builder) ->
                severity == null ? builder.conjunction() : builder.equal(root.get("severity"), severity);
    }

    public static Specification<Complaint> hasRejected(){
        return (root, query, builder) ->
                builder.isTrue(root.get("rejected"));
    }

    public static Specification<Complaint> hasStatus(IssueStatus status){
        return (root, query, builder) ->
                status == null ? builder.conjunction() : builder.equal(root.get("status"), status);
    }

    public static Specification<Complaint> hasDate(Date from, Date to){
        return (root, query, builder) -> {
            if (from != null && to != null)
                return builder.between(root.get("createdAt"), from, to);
            if (from != null)
                return builder.greaterThanOrEqualTo(root.get("createdAt"), from);
            if (to != null)
                return builder.lessThanOrEqualTo(root.get("createdAt"), to);
            return builder.conjunction();
        };
    }

    // ============================================================
    // NEW BLOCK ADMIN + REOPEN SPECS
    // ============================================================

    /** ML could not determine department **/
    public static Specification<Complaint> isMLUnknown() {
        return (root, query, builder) ->
                builder.isTrue(root.get("mlUnknown")); // recommended field
    }

    /** Reopened twice or more **/
    public static Specification<Complaint> hasMultipleReopens() {
        return (root, query, builder) ->
                builder.greaterThanOrEqualTo(root.get("reopenCount"), 2);
    }

    /** Filter by block **/
    public static Specification<Complaint> hasBlock(Long blockId) {
        return (root, query, builder) ->
                blockId == null ? builder.conjunction() : builder.equal(root.get("block").get("id"), blockId);
    }

    /** Unrestricted base spec **/
    public static Specification<Complaint> unrestricted() {
        return (root, query, builder) -> builder.conjunction();
    }
}
