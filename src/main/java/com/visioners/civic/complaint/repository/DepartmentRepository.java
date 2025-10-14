package com.visioners.civic.complaint.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.visioners.civic.complaint.entity.Department;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long>{

    Optional<Department> findByName(String department);
    
}
