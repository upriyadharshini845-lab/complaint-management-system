package com.cms.complaint.service;

import com.cms.complaint.enums.Priority;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PriorityCalculatorTest {

    private final PriorityCalculator calc = new PriorityCalculator("fire,shock,short circuit", "leak,broken,sparking");

    @Test
    void categoryDefaultWinsWhenUrgencyIsLower() {
        assertEquals(Priority.HIGH, calc.calculate(Priority.HIGH, Priority.LOW, "minor issue"));
    }

    @Test
    void urgencyWinsWhenHigherThanCategory() {
        assertEquals(Priority.MEDIUM, calc.calculate(Priority.LOW, Priority.MEDIUM, "paint peeling"));
    }

    @Test
    void criticalKeywordForcesCritical() {
        assertEquals(Priority.CRITICAL, calc.calculate(Priority.LOW, Priority.LOW, "There is FIRE near the lab"));
    }

    @Test
    void highKeywordRaisesLowToHigh() {
        assertEquals(Priority.HIGH, calc.calculate(Priority.LOW, Priority.LOW, "Pipe leak in corridor"));
    }

    @Test
    void highKeywordDoesNotLowerCritical() {
        assertEquals(Priority.CRITICAL, calc.calculate(Priority.CRITICAL, Priority.LOW, "small leak"));
    }
}
