package com.visioners.civic.complaint.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.visioners.civic.complaint.entity.Department;
import java.util.List;
import com.visioners.civic.complaint.entity.Block;


@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long>{

    Optional<Department> findByName(String department);

    Optional<Department> findByNameAndBlockId(String string, Long id);
    
    List<Department> findByBlock(Block block);
}
