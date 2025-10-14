package com.visioners.civic.staff.repository;

import com.visioners.civic.staff.entity.Staff;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import com.visioners.civic.user.entity.Users;


@Repository
public interface StaffRepository extends JpaRepository<Staff, Long>, JpaSpecificationExecutor<Staff>{

    Optional<Staff> findByUser(Users user);

}
