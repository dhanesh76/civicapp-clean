package com.visioners.civic.complaint.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.visioners.civic.complaint.entity.Block;

@Repository
public interface BlockRepository extends JpaRepository<Block, Long>{

    Optional<Block> findByName(String locality);

    List<Block> findByDistrictId(Long districtId);    
}
