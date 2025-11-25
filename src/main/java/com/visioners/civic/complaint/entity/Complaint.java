package com.visioners.civic.complaint.entity;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.locationtech.jts.geom.Point;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.visioners.civic.complaint.model.Category;
import com.visioners.civic.complaint.model.IssueSeverity;
import com.visioners.civic.complaint.model.IssueStatus;
import com.visioners.civic.complaint.model.Location;
import com.visioners.civic.complaint.model.SubCategory;
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

    /** Public complaint ID like C09H2 */
    @Column(unique = true, updatable = false, nullable = false)
    private String complaintId;

    @Column(nullable = false, length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    private IssueSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "sub_category")
    private SubCategory subCategory;
    
    /** Human-readable block/district/state */
    @Embedded
    private Location location;

    /** GIS coordinate for spatial search */
    @Column(columnDefinition = "geography(Point,4326)", nullable = false)
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.GEOMETRY)
    private Point locationPoint;

    /** Image of issue, mandatory */
    @Column(nullable = false, unique = true, name = "image_url")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssueStatus status;

    // -------------------- RELATIONS -------------------- //

    /** Citizen who raised the complaint */
    @ManyToOne
    @JoinColumn(name = "raised_by_id", nullable = false)
    @JsonManagedReference
    private Users raisedBy;

    /** District under which the complaint falls */
    @ManyToOne
    @JoinColumn(name = "district_id", nullable = false)
    private District district;

    /** Block under the district */
    @ManyToOne
    @JoinColumn(name = "block_id", nullable = false)
    private Block block;

    /** Department responsible for handling */
    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    /** Officer who assigned it */
    @ManyToOne
    @JoinColumn(name = "assigned_by_id")
    private Staff assignedBy;

    /** Field worker assigned to resolve it */
    @ManyToOne
    @JoinColumn(name = "assigned_to_id")
    private Staff assignedTo;

    /** Final image uploaded by field worker */
    @Column(name = "solution_image_url")
    private String solutionImageUrl;

    /** Final solution note */
    @Column(name = "solution_note")
    private String solutionNote;

    /** Officer who approved/rejected */
    @ManyToOne
    @JoinColumn(name = "actioned_by_id")
    private Staff actionedBy;

    /** When officer approved/rejected */
    private Instant actionedAt;

    /** Rejection flag */
    @Column(name = "is_rejected")
    private boolean rejected;

    @Column(name = "rejection_note")
    private String rejectionNote;

    // -------------------- TIMESTAMPS -------------------- //

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private Instant createdAt;

    private Instant assignedAt;

    private Instant resolvedAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}
