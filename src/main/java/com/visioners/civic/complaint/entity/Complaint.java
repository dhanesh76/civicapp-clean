package com.visioners.civic.complaint.entity;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.locationtech.jts.geom.Point;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.visioners.civic.complaint.model.IssueSeverity;
import com.visioners.civic.complaint.model.IssueStatus;
import com.visioners.civic.complaint.model.Location;
import com.visioners.civic.staff.entity.Staff;
import com.visioners.civic.user.entity.Users;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Complaint entity for managing citizen complaints.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique=true, updatable=false, nullable=false)
    private String complaintId; 

    @Column(nullable = false, length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    private IssueSeverity severity;

    @Embedded
    private Location location;
    @Column(columnDefinition="geography(Point,4326)", nullable=false)
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.GEOMETRY)
    private Point locationPoint;

    @Column(unique = true, nullable = false, name = "image_url")
    private String imageUrl;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private IssueStatus status;

    @ManyToOne
    @JoinColumn(name = "raised_by_id", nullable = false)
    @JsonManagedReference
    private Users raisedBy;

    @ManyToOne
    @JoinColumn(name = "district_id", nullable = false)
    @JsonManagedReference
    private District district;

    @ManyToOne
    @JoinColumn(name = "block_id", nullable = false)
    @JsonManagedReference
    private Block block;

    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false)
    @JsonManagedReference
    private Department department;

    // Staff who assigned the complaint
    @ManyToOne
    @JoinColumn(name = "assigned_by_staff_id")
    @JsonManagedReference
    private Staff assignedBy;

    // Field worker assigned to resolve the complaint
    @ManyToOne
    @JoinColumn(name = "assigned_staff_id")
    @JsonManagedReference
    private Staff assignedTo;

    // Image uploaded by field worker as solution
    @Column(unique = true)
    private String solutionImageUrl;

    // Note or description of solution by field worker
    private String solutionNote;

    // Officer who approves or rejects
    @ManyToOne
    @JoinColumn(name = "actioned_by_id")
    @JsonManagedReference
    private Staff actionedBy;

    // Timestamp when officer approved/rejected
    private Instant actionedAt;

    // Flag and note for rejected complaints
    private boolean isRejected;
    private String rejectionNote;

    // Timestamp when complaint was created
    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private Instant createdAt;

    // Timestamp when complaint was assigned to a worker
    private Instant assignedAt;

    // Timestamp when complaint was resolved by field worker
    private Instant resolvedAt;

    // Last updated timestamp
    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}
