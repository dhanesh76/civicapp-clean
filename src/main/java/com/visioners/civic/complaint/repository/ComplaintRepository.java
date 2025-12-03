package com.visioners.civic.complaint.repository;

import com.visioners.civic.complaint.entity.Complaint;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    Optional<Complaint> findByComplaintId(String complaintId);   

    @Query(value = """
                SELECT ST_DWithin(
                    location_point,
                    ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography,
                    :radius
                )
                FROM complaint
                WHERE complaint_id = :complaintId
            """, nativeQuery = true)
    boolean isWorkerWithinDistance(@Param("complaintId") String complaintId,
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("radius") double radius);
    
    @Query("""
        SELECT c FROM Complaint c
        WHERE c.block.id = :blockId
        AND c.status = com.visioners.civic.complaint.model.IssueStatus.PENDING
        ORDER BY c.createdAt DESC
    """)
    Page<Complaint> findMLUnknownComplaints(Long blockId, Pageable pageable);


    @Query("""
        SELECT c FROM Complaint c 
        WHERE c.block.id = :blockId
        AND c.reopenCount >= 2
        ORDER BY c.createdAt DESC
    """)
    Page<Complaint> findReopenedComplaints(Long blockId, Pageable pageable);
}
