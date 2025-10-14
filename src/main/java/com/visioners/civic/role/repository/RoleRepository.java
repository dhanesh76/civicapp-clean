package com.visioners.civic.role.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.visioners.civic.role.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
    
    Optional<Role> findByName(String name);

}
