package com.artemk.schooltracking.dto;

import com.artemk.schooltracking.domain.Settings;

import java.math.BigDecimal;

public record SettingsDto(
        BigDecimal fiveReward,
        BigDecimal fourReward,
        BigDecimal threePenalty,
        BigDecimal twoPenalty,
        BigDecimal coreCoefficient,
        BigDecimal otherCoefficient
) {
    public static SettingsDto from(Settings s) {
        return new SettingsDto(
                s.getFiveReward(),
                s.getFourReward(),
                s.getThreePenalty(),
                s.getTwoPenalty(),
                s.getCoreCoefficient(),
                s.getOtherCoefficient()
        );
    }

    public void applyTo(Settings s) {
        s.setFiveReward(fiveReward);
        s.setFourReward(fourReward);
        s.setThreePenalty(threePenalty);
        s.setTwoPenalty(twoPenalty);
        s.setCoreCoefficient(coreCoefficient);
        s.setOtherCoefficient(otherCoefficient);
    }
}