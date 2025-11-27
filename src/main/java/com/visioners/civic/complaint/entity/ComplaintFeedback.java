package com.visioners.civic.complaint.entity;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import com.visioners.civic.user.entity.Users;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Builder @Data
@AllArgsConstructor
@NoArgsConstructor
public class ComplaintFeedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    Users user; 

    @ManyToOne(optional = false)
    @JoinColumn(name="complaint_id", nullable = false)
    Complaint complaint;

    @Column(nullable = false)
    String comment;

    @Column(nullable = false)
    Double rating;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    Instant createdAt;
}
