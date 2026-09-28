package com.artemk.schooltracking.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "username"))
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, length = 60)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 120)
    private String passwordHash;

    @Column(name = "display_name", length = 120)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_id")
    private User parent;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public User() {
    }

    public User(String username, String passwordHash, String displayName, Role role, User parent) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.role = role;
        this.parent = parent;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public User getParent() {
        return parent;
    }

    public void setParent(User parent) {
        this.parent = parent;
    }

    public Long getParentId() {
        return parent == null ? null : parent.getId();
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}