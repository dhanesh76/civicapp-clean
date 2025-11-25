package com.visioners.civic.complaint.Specifications;

import java.util.Date;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.visioners.civic.complaint.entity.Block;
import com.visioners.civic.complaint.entity.Complaint;
import com.visioners.civic.complaint.entity.Department;
import com.visioners.civic.complaint.entity.District;
import com.visioners.civic.complaint.model.IssueSeverity;
import com.visioners.civic.complaint.model.IssueStatus;
import com.visioners.civic.user.entity.Users;
import com.visioners.civic.staff.entity.Staff;

@Component 
public class ComplaintSpecification {
    
    public static Specification<Complaint> hasStatus(IssueStatus status){
        return (root, query, cb) -> 
            status == null ? null :
            cb.equal(root.get("status"), status);
    }

    public static Specification<Complaint> hasSeverity(IssueSeverity severity){
        return (root, query, cb) -> 
            severity == null ? null :
            cb.equal(root.get("severity"),severity);
    }

    public static Specification<Complaint> hasDate(Date from, Date to){
        return (root, query, cb) -> {
            if(from != null && to != null)
                return cb.between(root.get("createdAt"), from, to);
            else if(from != null)
                return cb.greaterThanOrEqualTo(root.get("createdAt"), from);
            else if(to != null)
                return cb.lessThanOrEqualTo(root.get("createdAt"), to);
            return null;
        };
    }

    public static Specification<Complaint> hasRaisedBy(Users user){
        return (root, query, cb) -> 
                    user == null ? null : 
                    cb.equal(root.get("raisedBy"), user);   
    }

    public static Specification<Complaint> hasDistrict(District district){
        return (root, query, cb) -> 
            district == null ? null : 
            cb.equal(root.get("district"), district);   
    }

    public static Specification<Complaint> hasBlock(Block block){
        return (root, query, cb) -> 
            block == null ? null : 
            cb.equal(root.get("block"), block);
    }

    public static Specification<Complaint> hasDepartment(Department department){
         return (root, query, cb) -> 
            department == null ? null : 
            cb.equal(root.get("department"), department);
    }

    public static Specification<Complaint> hasAssignedTo(Staff assignedTo){
        return (root, query, cb) -> 
            assignedTo == null ? null :
            cb.equal(root.get("assignedTo"), assignedTo);
    } 

    public static Specification<Complaint> getComplaintSpecification(IssueSeverity severity,IssueStatus status,
                                            Date fromDate,Date toDate) {
                                                
        Specification<Complaint> specification = Specification.unrestricted();
        specification.and(ComplaintSpecification.hasDate(fromDate, toDate))
        .and(ComplaintSpecification.hasSeverity(severity))
        .and(ComplaintSpecification.hasStatus(status));

        return specification;
    }

    public static Specification<Complaint> hasRejected(){
        return (root, query, cb) -> 
            cb.equal(root.get("rejected"), true);
    }
}
