package com.cms.complaint.service;

import com.cms.complaint.enums.Priority;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Smart priority rules:
 * 1. Start with the higher of (category default priority, urgency chosen by user).
 * 2. Any critical keyword in title/description -> CRITICAL.
 * 3. Any high keyword -> at least HIGH.
 * Keywords are configurable: app.priority.critical-keywords / app.priority.high-keywords
 */
@Component
public class PriorityCalculator {

    private final List<String> criticalWords;
    private final List<String> highWords;

    public PriorityCalculator(
            @Value("${app.priority.critical-keywords:fire,shock,short circuit,gas leak,collapse,electrocut,flood}") String critical,
            @Value("${app.priority.high-keywords:leak,broken,danger,sparking,no water,power cut,accident,urgent}") String high) {
        this.criticalWords = split(critical);
        this.highWords = split(high);
    }

    public Priority calculate(Priority categoryDefault, Priority urgency, String text) {
        Priority result = categoryDefault.ordinal() >= urgency.ordinal() ? categoryDefault : urgency;
        String t = text == null ? "" : text.toLowerCase();
        if (criticalWords.stream().anyMatch(t::contains)) {
            return Priority.CRITICAL;
        }
        if (highWords.stream().anyMatch(t::contains) && result.ordinal() < Priority.HIGH.ordinal()) {
            result = Priority.HIGH;
        }
        return result;
    }

    private static List<String> split(String csv) {
        return Arrays.stream(csv.split(",")).map(String::trim).map(String::toLowerCase)
                .filter(s -> !s.isEmpty()).toList();
    }
}
