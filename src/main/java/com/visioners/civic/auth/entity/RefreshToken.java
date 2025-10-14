package com.visioners.civic.auth.entity;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import com.visioners.civic.user.entity.Users;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true)
    private String token;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id", nullable=false)
    private Users user;

    @Column(nullable=false)
    private Instant expiresAt;

    @CreationTimestamp
    private Instant createdAt;
}

