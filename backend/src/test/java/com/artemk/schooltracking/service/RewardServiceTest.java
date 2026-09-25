package com.artemk.schooltracking.service;

import com.artemk.schooltracking.domain.Settings;
import com.artemk.schooltracking.domain.Subject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RewardServiceTest {

    private final RewardService rewardService = new RewardService(null);
    private final Settings settings = new Settings();

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    @Test
    void twoFivesFixATwoToOneFourReward() {
        Subject math = new Subject("Математика", true);

        BigDecimal two = rewardService.amountFor(2, math, settings);
        BigDecimal five = rewardService.amountFor(5, math, settings);

        assertEquals(bd("-100.00"), two);
        assertEquals(bd("75.00"), five);
        assertEquals(bd("50.00"), two.add(five).add(five));
    }

    @Test
    void coreAndOtherSubjectsHaveDifferentCoefficients() {
        Subject math = new Subject("Математика", true);
        Subject art = new Subject("ИЗО", false);

        assertEquals(bd("50.00"), rewardService.amountFor(4, math, settings));
        assertEquals(bd("35.00"), rewardService.amountFor(4, art, settings));
        assertEquals(bd("52.50"), rewardService.amountFor(5, art, settings));
        assertEquals(bd("-35.00"), rewardService.amountFor(3, art, settings));
        assertEquals(bd("-70.00"), rewardService.amountFor(2, art, settings));
    }
}