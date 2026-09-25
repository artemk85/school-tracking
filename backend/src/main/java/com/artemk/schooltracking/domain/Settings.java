package com.artemk.schooltracking.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "settings")
public class Settings {

    @Id
    private Long id;

    @Column(name = "five_reward", nullable = false, precision = 10, scale = 2)
    private java.math.BigDecimal fiveReward = new java.math.BigDecimal("75.00");

    @Column(name = "four_reward", nullable = false, precision = 10, scale = 2)
    private java.math.BigDecimal fourReward = new java.math.BigDecimal("50.00");

    @Column(name = "three_penalty", nullable = false, precision = 10, scale = 2)
    private java.math.BigDecimal threePenalty = new java.math.BigDecimal("-50.00");

    @Column(name = "two_penalty", nullable = false, precision = 10, scale = 2)
    private java.math.BigDecimal twoPenalty = new java.math.BigDecimal("-100.00");

    @Column(name = "core_coefficient", nullable = false, precision = 10, scale = 4)
    private java.math.BigDecimal coreCoefficient = new java.math.BigDecimal("1.0000");

    @Column(name = "other_coefficient", nullable = false, precision = 10, scale = 4)
    private java.math.BigDecimal otherCoefficient = new java.math.BigDecimal("0.7000");

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public java.math.BigDecimal getFiveReward() {
        return fiveReward;
    }

    public void setFiveReward(java.math.BigDecimal fiveReward) {
        this.fiveReward = fiveReward;
    }

    public java.math.BigDecimal getFourReward() {
        return fourReward;
    }

    public void setFourReward(java.math.BigDecimal fourReward) {
        this.fourReward = fourReward;
    }

    public java.math.BigDecimal getThreePenalty() {
        return threePenalty;
    }

    public void setThreePenalty(java.math.BigDecimal threePenalty) {
        this.threePenalty = threePenalty;
    }

    public java.math.BigDecimal getTwoPenalty() {
        return twoPenalty;
    }

    public void setTwoPenalty(java.math.BigDecimal twoPenalty) {
        this.twoPenalty = twoPenalty;
    }

    public java.math.BigDecimal getCoreCoefficient() {
        return coreCoefficient;
    }

    public void setCoreCoefficient(java.math.BigDecimal coreCoefficient) {
        this.coreCoefficient = coreCoefficient;
    }

    public java.math.BigDecimal getOtherCoefficient() {
        return otherCoefficient;
    }

    public void setOtherCoefficient(java.math.BigDecimal otherCoefficient) {
        this.otherCoefficient = otherCoefficient;
    }
}