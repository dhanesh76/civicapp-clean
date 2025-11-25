package com.visioners.civic.complaint.entity;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ComplaintAudio {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)  
    private Long id;

    @OneToOne
    @JoinColumn(name = "complaint_id", referencedColumnName = "id", nullable = false, unique = true)
    private Complaint complaint;

    @Column(nullable = false, unique = true)
    private String audioUrl;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private Instant uploadedAt;
}
