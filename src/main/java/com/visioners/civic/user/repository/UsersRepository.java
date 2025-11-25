package com.visioners.civic.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.visioners.civic.user.entity.Users;

@Repository
public interface UsersRepository extends JpaRepository<Users, Long> {

    
    boolean existsByMobileNumber(String mobileNumber);

    Optional<Users> findByMobileNumber(String mobileNumber);

    Optional<Users> findByEmail(String email);

    Optional<Users> findByMobileNumberOrEmail(String mobileNumber, String email);
}

