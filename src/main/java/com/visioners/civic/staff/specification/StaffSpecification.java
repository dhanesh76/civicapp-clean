package com.visioners.civic.staff.specification;

import org.springframework.data.jpa.domain.Specification;


import com.visioners.civic.complaint.entity.Block;
import com.visioners.civic.complaint.entity.Department;
import com.visioners.civic.complaint.entity.District;
import com.visioners.civic.role.entity.Role;
import com.visioners.civic.staff.entity.Staff;

public class StaffSpecification {

     public static Specification<Staff> hasDistrict(District district){
        return (root, query, cb) -> 
            district == null ? null : 
            cb.equal(root.get("district"), district);   
    }

    public static Specification<Staff> hasBlock(Block block){
        return (root, query, cb) -> 
            block == null ? null : 
            cb.equal(root.get("block"), block);
    }

    public static Specification<Staff> hasDepartment(Department department){
         return (root, query, cb) -> 
            department == null ? null : 
            cb.equal(root.get("department"), department);
    }

    public static Specification<Staff> hasRole(Role role){
        return (root, query, cb) -> 
            role == null ? null : 
            cb.equal(root.get("user").get("role"), role);
    }

    public static Specification<Staff> hasName(String name){
        return (root, query, cb) -> 
            name == null ? null : 
            cb.like(root.get("user").get("username"), "%" + name + "%");
    }
}
