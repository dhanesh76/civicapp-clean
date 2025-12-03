package com.visioners.civic.user.entity;

import java.time.Instant;
import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.visioners.civic.role.entity.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Users {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Column(unique = true)
    private String mobileNumber;
    
    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @ManyToOne
    @JsonBackReference
    Role role;

    @Column(nullable = false)
    boolean isVerified;

    @CreationTimestamp
    Instant createdAt;
}

