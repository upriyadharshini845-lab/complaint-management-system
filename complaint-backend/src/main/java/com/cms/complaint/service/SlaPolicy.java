package com.cms.complaint.service;

import com.cms.complaint.enums.Priority;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

/** Resolution deadline (hours) per priority, configurable in application.properties (app.sla.*). */
@Component
public class SlaPolicy {

    private final Map<Priority, Integer> hours = new EnumMap<>(Priority.class);

    public SlaPolicy(@Value("${app.sla.critical:24}") int critical,
                     @Value("${app.sla.high:72}") int high,
                     @Value("${app.sla.medium:120}") int medium,
                     @Value("${app.sla.low:168}") int low) {
        hours.put(Priority.CRITICAL, critical);
        hours.put(Priority.HIGH, high);
        hours.put(Priority.MEDIUM, medium);
        hours.put(Priority.LOW, low);
    }

    public int hoursFor(Priority p) {
        return hours.get(p);
    }
}
