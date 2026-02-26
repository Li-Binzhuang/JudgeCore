package org.laoli.judge.service.comparator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.laoli.judge.service.comparator.OutputComparator.CompareResult;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OutputComparator Tests")
class OutputComparatorTest {

    private OutputComparator comparator;

    @BeforeEach
    void setUp() {
        comparator = new OutputComparator();
    }

    @Nested
    @DisplayName("Basic String Comparison")
    class BasicComparison {

        @Test
        @DisplayName("Should return true for identical strings")
        void shouldReturnTrueForIdenticalStrings() {
            assertTrue(comparator.compare("Hello World", "Hello World"));
        }

        @Test
        @DisplayName("Should return true for strings with trailing whitespace")
        void shouldReturnTrueForStringsWithTrailingWhitespace() {
            assertTrue(comparator.compare("Hello World  ", "Hello World"));
        }

        @Test
        @DisplayName("Should return true for strings with leading whitespace")
        void shouldReturnTrueForStringsWithLeadingWhitespace() {
            assertTrue(comparator.compare("  Hello World", "Hello World"));
        }

        @Test
        @DisplayName("Should return false for different strings")
        void shouldReturnFalseForDifferentStrings() {
            assertFalse(comparator.compare("Hello World", "Hello"));
        }

        @Test
        @DisplayName("Should handle null inputs")
        void shouldHandleNullInputs() {
            assertTrue(comparator.compare(null, null));
            assertFalse(comparator.compare(null, "test"));
            assertFalse(comparator.compare("test", null));
        }
    }

    @Nested
    @DisplayName("Newline Normalization")
    class NewlineNormalization {

        @Test
        @DisplayName("Should normalize Windows newlines")
        void shouldNormalizeWindowsNewlines() {
            assertTrue(comparator.compare("Line1\r\nLine2", "Line1\nLine2"));
        }

        @Test
        @DisplayName("Should normalize old Mac newlines")
        void shouldNormalizeOldMacNewlines() {
            assertTrue(comparator.compare("Line1\rLine2", "Line1\nLine2"));
        }

        @Test
        @DisplayName("Should handle multiple newlines")
        void shouldHandleMultipleNewlines() {
            assertTrue(comparator.compare("Line1\n\n\nLine2", "Line1\nLine2"));
        }
    }

    @Nested
    @DisplayName("Numeric Comparison")
    class NumericComparison {

        @Test
        @DisplayName("Should compare integers correctly")
        void shouldCompareIntegersCorrectly() {
            assertTrue(comparator.compare("42", "42"));
            assertFalse(comparator.compare("42", "43"));
        }

        @Test
        @DisplayName("Should compare floating point numbers with epsilon")
        void shouldCompareFloatingPointWithEpsilon() {
            assertTrue(comparator.compare("3.14159", "3.14159", false, 1e-5));
            assertTrue(comparator.compare("3.14159", "3.14160", false, 1e-3));
            assertFalse(comparator.compare("3.14159", "3.14160", false, 1e-10));
        }

        @Test
        @DisplayName("Should handle scientific notation")
        void shouldHandleScientificNotation() {
            assertTrue(comparator.compare("1e10", "1e10"));
            assertTrue(comparator.compare("1.5e-5", "1.5e-5"));
        }

        @Test
        @DisplayName("Should handle NaN and Infinity")
        void shouldHandleNaNAndInfinity() {
            assertTrue(comparator.compare("NaN", "NaN"));
            assertTrue(comparator.compare("Infinity", "Infinity"));
            assertTrue(comparator.compare("-Infinity", "-Infinity"));
        }

        @Test
        @DisplayName("Should compare multi-line numeric output")
        void shouldCompareMultiLineNumericOutput() {
            assertTrue(comparator.compare("1\n2\n3", "1\n2\n3"));
            assertFalse(comparator.compare("1\n2\n3", "1\n2\n4"));
        }
    }

    @Nested
    @DisplayName("Whitespace Ignoring Mode")
    class WhitespaceIgnoringMode {

        @Test
        @DisplayName("Should ignore extra whitespace when enabled")
        void shouldIgnoreExtraWhitespace() {
            assertTrue(comparator.compare("Hello    World", "Hello World", true));
            assertTrue(comparator.compare("Hello\n\tWorld", "Hello World", true));
        }

        @Test
        @DisplayName("Should still detect differences when whitespace ignored")
        void shouldStillDetectDifferences() {
            assertFalse(comparator.compare("Hello World", "Hello", true));
        }
    }

    @Nested
    @DisplayName("CompareResult Tests")
    class CompareResultTests {

        @Test
        @DisplayName("Should return correct CompareResult for matching strings")
        void shouldReturnCorrectCompareResultForMatching() {
            CompareResult result = comparator.getCompareResult("  Hello  ", "Hello");
            assertTrue(result.equal());
            assertEquals("Hello", result.normalizedExpected());
            assertEquals("Hello", result.normalizedActual());
        }

        @Test
        @DisplayName("Should return correct CompareResult for non-matching strings")
        void shouldReturnCorrectCompareResultForNonMatching() {
            CompareResult result = comparator.getCompareResult("Hello", "World");
            assertFalse(result.equal());
            assertEquals("Hello", result.normalizedExpected());
            assertEquals("World", result.normalizedActual());
        }

        @Test
        @DisplayName("Should return correct CompareResult with whitespace ignore")
        void shouldReturnCorrectCompareResultWithWhitespaceIgnore() {
            CompareResult result = comparator.getCompareResult("  Hello  World  ", "Hello World", true, 1e-9);
            assertTrue(result.equal());
            assertEquals("Hello World", result.normalizedExpected());
            assertEquals("Hello World", result.normalizedActual());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should handle empty strings")
        void shouldHandleEmptyStrings() {
            assertTrue(comparator.compare("", ""));
            assertFalse(comparator.compare("", "test"));
        }

        @Test
        @DisplayName("Should handle whitespace only strings")
        void shouldHandleWhitespaceOnlyStrings() {
            assertTrue(comparator.compare("   ", ""));
            assertTrue(comparator.compare("\t\n\r", ""));
        }

        @Test
        @DisplayName("Should handle special characters")
        void shouldHandleSpecialCharacters() {
            assertTrue(comparator.compare("!@#$%^&*()", "!@#$%^&*()"));
            assertTrue(comparator.compare("中文测试", "中文测试"));
            assertTrue(comparator.compare("emoji 😀", "emoji 😀"));
        }

        @Test
        @DisplayName("Should handle Unicode correctly")
        void shouldHandleUnicode() {
            assertTrue(comparator.compare("Привет мир", "Привет мир"));
            assertTrue(comparator.compare("🎉🎊🎈", "🎉🎊🎈"));
        }
    }
}
