package com.artemk.schooltracking.domain;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "grades", indexes = {
        @Index(name = "idx_grades_date", columnList = "grade_date"),
        @Index(name = "idx_grades_subject", columnList = "subject_id"),
        @Index(name = "idx_grades_owner", columnList = "owner_id"),
        @Index(name = "idx_grades_user", columnList = "user_id")
})
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User child;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(name = "value", nullable = false)
    private int value;

    @Column(name = "grade_date", nullable = false)
    private LocalDate gradeDate;

    public Grade() {
    }

    public Grade(User owner, User child, Subject subject, int value, LocalDate gradeDate) {
        this.owner = owner;
        this.child = child;
        this.subject = subject;
        this.value = value;
        this.gradeDate = gradeDate;
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

    public User getChild() {
        return child;
    }

    public void setChild(User child) {
        this.child = child;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public LocalDate getGradeDate() {
        return gradeDate;
    }

    public void setGradeDate(LocalDate gradeDate) {
        this.gradeDate = gradeDate;
    }
}