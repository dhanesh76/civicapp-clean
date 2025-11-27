package com.visioners.civic.community.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.visioners.civic.complaint.entity.Complaint;

@Repository
public interface CommunityComplaintRepository extends JpaRepository<Complaint, Long> {
    @Query(value = """
            SELECT * FROM complaint
            WHERE ST_DWithin(
                location_point,
                ST_SetSRID(ST_MakePoint(:lon, :lat), 4326),
                :radius
            )
            AND raised_by_id != :user_id
            
            """, countQuery = """
            SELECT count(*) FROM complaint
            WHERE ST_DWithin(
                location_point,
                ST_SetSRID(ST_MakePoint(:lon, :lat), 4326),
                :radius
            )
            """, nativeQuery = true)
    Page<Complaint> findNearby(
            @Param("userId") Long userId,
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("radius") double radius,
            Pageable pageable);
}
