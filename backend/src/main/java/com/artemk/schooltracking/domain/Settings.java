package com.artemk.schooltracking.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "settings")
public class Settings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "five_reward", nullable = false, precision = 10, scale = 2)
    private BigDecimal fiveReward = new BigDecimal("75.00");

    @Column(name = "four_reward", nullable = false, precision = 10, scale = 2)
    private BigDecimal fourReward = new BigDecimal("50.00");

    @Column(name = "three_penalty", nullable = false, precision = 10, scale = 2)
    private BigDecimal threePenalty = new BigDecimal("-50.00");

    @Column(name = "two_penalty", nullable = false, precision = 10, scale = 2)
    private BigDecimal twoPenalty = new BigDecimal("-100.00");

    @Column(name = "core_coefficient", nullable = false, precision = 10, scale = 4)
    private BigDecimal coreCoefficient = new BigDecimal("1.0000");

    @Column(name = "other_coefficient", nullable = false, precision = 10, scale = 4)
    private BigDecimal otherCoefficient = new BigDecimal("0.7000");

    public Settings() {
    }

    public Settings(User owner) {
        this.owner = owner;
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

    public BigDecimal getFiveReward() {
        return fiveReward;
    }

    public void setFiveReward(BigDecimal fiveReward) {
        this.fiveReward = fiveReward;
    }

    public BigDecimal getFourReward() {
        return fourReward;
    }

    public void setFourReward(BigDecimal fourReward) {
        this.fourReward = fourReward;
    }

    public BigDecimal getThreePenalty() {
        return threePenalty;
    }

    public void setThreePenalty(BigDecimal threePenalty) {
        this.threePenalty = threePenalty;
    }

    public BigDecimal getTwoPenalty() {
        return twoPenalty;
    }

    public void setTwoPenalty(BigDecimal twoPenalty) {
        this.twoPenalty = twoPenalty;
    }

    public BigDecimal getCoreCoefficient() {
        return coreCoefficient;
    }

    public void setCoreCoefficient(BigDecimal coreCoefficient) {
        this.coreCoefficient = coreCoefficient;
    }

    public BigDecimal getOtherCoefficient() {
        return otherCoefficient;
    }

    public void setOtherCoefficient(BigDecimal otherCoefficient) {
        this.otherCoefficient = otherCoefficient;
    }
}