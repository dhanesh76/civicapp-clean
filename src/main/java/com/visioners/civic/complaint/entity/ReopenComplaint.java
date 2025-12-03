package com.visioners.civic.complaint.entity;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import com.visioners.civic.complaint.model.ReopenStatus;
import com.visioners.civic.staff.entity.Staff;
import com.visioners.civic.user.entity.Users;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "reopen_complaint")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReopenComplaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String reopenId; // public reopen id like C09H2_R1

    @ManyToOne
    @JoinColumn(name = "parent_complaint_id", nullable = false)
    private Complaint parentComplaint;

    @ManyToOne
    @JoinColumn(name = "raised_by_id", nullable = false)
    private Users raisedBy;

    @Column(name = "proof_url")
    private String proofUrl;

    @Column(length = 1000)
    private String note;

    private Double lat;
    private Double lon;

    @Enumerated(EnumType.STRING)
    private ReopenStatus status;

    @Column(name = "ba_decision_note", length = 1000)
    private String baDecisionNote;

    @ManyToOne
    @JoinColumn(name = "ba_decision_by_id")
    private Staff baDecisionBy;

    private Instant baDecisionAt;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private Instant createdAt;

    @Column(name = "reopen_number")
    private int reopenNumber;

}
