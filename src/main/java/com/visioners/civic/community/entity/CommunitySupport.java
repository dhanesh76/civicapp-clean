package com.visioners.civic.community.entity;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import com.visioners.civic.complaint.entity.Complaint;
import com.visioners.civic.user.entity.Users;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@Table(
    name = "community_support",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "complaint_id"})
    }
)
public class CommunitySupport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private Users user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "complaint_id")
    private Complaint complaint;

    @CreationTimestamp
    private Instant createdAt;
}
