package com.visioners.civic.complaint.repository;

import com.visioners.civic.complaint.entity.Complaint;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Repository;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long>, JpaSpecificationExecutor<Complaint> {
    
    @SuppressWarnings("null")
    Page<Complaint> findAll(Specification<Complaint> specification,
    @PageableDefault(page = 0, size = 10) 
    @SortDefault(sort="createdAt", direction = Sort.Direction.DESC) 
    Pageable page);    
}
