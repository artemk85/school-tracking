package com.artemk.schooltracking.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "subjects")
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "core", nullable = false)
    private boolean core;

    public Subject() {
    }

    public Subject(User owner, String name, boolean core) {
        this.owner = owner;
        this.name = name;
        this.core = core;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isCore() {
        return core;
    }

    public void setCore(boolean core) {
        this.core = core;
    }
}