package org.laoli.judge.service.comparator;

import org.springframework.stereotype.Component;

@Component
public class OutputComparator {

    private static final double DEFAULT_EPSILON = 1e-9;

    public boolean compare(String expected, String actual) {
        return compare(expected, actual, false, DEFAULT_EPSILON);
    }

    public boolean compare(String expected, String actual, boolean ignoreWhitespace) {
        return compare(expected, actual, ignoreWhitespace, DEFAULT_EPSILON);
    }

    public boolean compare(String expected, String actual, boolean ignoreWhitespace, double epsilon) {
        if (expected == null && actual == null) {
            return true;
        }
        if (expected == null || actual == null) {
            return false;
        }

        String normalizedExpected = normalize(expected, ignoreWhitespace);
        String normalizedActual = normalize(actual, ignoreWhitespace);

        if (normalizedExpected.equals(normalizedActual)) {
            return true;
        }

        if (isNumericOutput(normalizedExpected) && isNumericOutput(normalizedActual)) {
            return compareNumeric(normalizedExpected, normalizedActual, epsilon);
        }

        return false;
    }

    private String normalize(String output, boolean ignoreWhitespace) {
        if (output == null) {
            return "";
        }

        String result = output.replaceAll("\\r\\n", "\n").replaceAll("\\r", "\n");
        result = result.replaceAll("\n+", "\n");

        if (ignoreWhitespace) {
            result = result.replaceAll("\\s+", " ").trim();
        } else {
            result = result.trim();
        }

        return result;
    }

    private boolean isNumericOutput(String output) {
        if (output == null || output.isEmpty()) {
            return false;
        }

        String trimmed = output.trim();

        String[] lines = trimmed.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            try {
                Double.parseDouble(line);
            } catch (NumberFormatException e) {
                if (!line.matches("^[+\\-]?\\d*\\.?\\d*[eE][+\\-]?\\d+$")) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean compareNumeric(String expected, String actual, double epsilon) {
        String[] expectedLines = expected.split("\n");
        String[] actualLines = actual.split("\n");

        if (expectedLines.length != actualLines.length) {
            return false;
        }

        for (int i = 0; i < expectedLines.length; i++) {
            String expLine = expectedLines[i].trim();
            String actLine = actualLines[i].trim();

            if (expLine.isEmpty() && actLine.isEmpty()) {
                continue;
            }

            try {
                double expNum = Double.parseDouble(expLine);
                double actNum = Double.parseDouble(actLine);

                if (!isEqual(expNum, actNum, epsilon)) {
                    return false;
                }
            } catch (NumberFormatException e) {
                if (!expLine.equals(actLine)) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean isEqual(double expected, double actual, double epsilon) {
        if (Double.isNaN(expected) && Double.isNaN(actual)) {
            return true;
        }
        if (Double.isNaN(expected) || Double.isNaN(actual)) {
            return false;
        }
        if (Double.isInfinite(expected) && Double.isInfinite(actual)) {
            return expected == actual;
        }
        if (Double.isInfinite(expected) || Double.isInfinite(actual)) {
            return false;
        }

        return Math.abs(expected - actual) <= epsilon;
    }

    public CompareResult getCompareResult(String expected, String actual) {
        return getCompareResult(expected, actual, false, DEFAULT_EPSILON);
    }

    public CompareResult getCompareResult(String expected, String actual, boolean ignoreWhitespace, double epsilon) {
        boolean equal = compare(expected, actual, ignoreWhitespace, epsilon);

        String normalizedExpected = normalize(expected, ignoreWhitespace);
        String normalizedActual = normalize(actual, ignoreWhitespace);

        return new CompareResult(equal, normalizedExpected, normalizedActual);
    }

    public record CompareResult(boolean equal, String normalizedExpected, String normalizedActual) {
    }
}
