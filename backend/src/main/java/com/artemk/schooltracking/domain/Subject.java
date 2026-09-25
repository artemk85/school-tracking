package com.artemk.schooltracking.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "subjects", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "core", nullable = false)
    private boolean core;

    public Subject() {
    }

    public Subject(String name, boolean core) {
        this.name = name;
        this.core = core;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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