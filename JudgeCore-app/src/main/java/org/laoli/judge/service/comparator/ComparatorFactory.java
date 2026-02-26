package org.laoli.judge.service.comparator;

import org.laoli.judge.service.comparator.impl.ExactOutputComparator;

import java.util.HashMap;
import java.util.Map;

public class ComparatorFactory {
    private static final Map<String, OutputComparator> COMPARATORS = new HashMap<>();

    static {
        COMPARATORS.put("exact", new ExactOutputComparator());
    }

    private ComparatorFactory() {
    }

    public static OutputComparator getComparator(String type) {
        return COMPARATORS.getOrDefault(type, COMPARATORS.get("exact"));
    }
}
