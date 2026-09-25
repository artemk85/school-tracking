package com.artemk.schooltracking.domain;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "grades", indexes = {
        @Index(name = "idx_grades_date", columnList = "grade_date"),
        @Index(name = "idx_grades_subject", columnList = "subject_id")
})
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(name = "value", nullable = false)
    private int value;

    @Column(name = "grade_date", nullable = false)
    private LocalDate gradeDate;

    public Grade() {
    }

    public Grade(Subject subject, int value, LocalDate gradeDate) {
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