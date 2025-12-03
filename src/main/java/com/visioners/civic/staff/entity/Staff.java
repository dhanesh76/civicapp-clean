package com.visioners.civic.staff.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.visioners.civic.complaint.entity.Block;
import com.visioners.civic.complaint.entity.Department;
import com.visioners.civic.complaint.entity.District;
import com.visioners.civic.user.entity.Users;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

import lombok.Data;

@Entity
@Data
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonManagedReference
    private Users user;

    @ManyToOne
    @JoinColumn(name = "district_id", nullable = false)
    @JsonManagedReference
    private District district;

    @ManyToOne
    @JoinColumn(name = "block_id", nullable = true)   // DA allow null
    @JsonManagedReference
    private Block block;

    @ManyToOne
    @JoinColumn(name = "department_id", nullable = true) // OFFICER, WORKER need dept; BA_Admins no
    @JsonManagedReference
    private Department department;

    @CreationTimestamp
    private Instant createdAt;
}
