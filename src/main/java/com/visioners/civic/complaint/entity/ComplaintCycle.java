package com.visioners.civic.complaint.entity;

import java.time.Instant;

import com.visioners.civic.complaint.model.ActionType;
import com.visioners.civic.complaint.model.ActorType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;

import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "complaint_cycle")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintCycle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** internal complaint id (not a relation) */
    @Column(name = "complaint_id", nullable = false)
    private Long complaintId;

    @Column(name = "cycle_number", nullable = false)
    private int cycleNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private ActionType actionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "actor_type", nullable = false)
    private ActorType actorType;

    @Column(name = "actor_id")
    private Long actorId;

    @Column(name = "old_status")
    private String oldStatus;

    @Column(name = "new_status")
    private String newStatus;

    @Column(name = "note", length = 1000)
    private String note;

    @Column(name = "solution_image_url")
    private String solutionImageUrl;

    @Column(name = "proof_image_url")
    private String proofImageUrl;

    @Column(name = "coords_lat")
    private Double coordsLat;

    @Column(name = "coords_lon")
    private Double coordsLon;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
