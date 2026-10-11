/*
 * Copyright 2024-2026 Todd Ginsberg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ginsberg.gatherers4j;

import com.ginsberg.gatherers4j.dto.WithOriginal;
import com.ginsberg.gatherers4j.util.TestValueHolder;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.ginsberg.gatherers4j.util.TestUtils.BIG_DECIMAL_RECURSIVE_COMPARISON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class BigDecimalPercentileGathererTest {

    private static Stream<BigDecimal> zeroToOneHundred() {
        return IntStream.rangeClosed(0, 100).mapToObj(BigDecimal::valueOf);
    }

    @Nested
    class Moving {

        @ParameterizedTest(name = "percentile of {0}")
        @ValueSource(doubles = {-0.1, 100.1, Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY})
        void byFunctionPercentileMustBeBetweenZeroAndOneHundred(final double percentile) {
            assertThatIllegalArgumentException().isThrownBy(() ->
                    Gatherers4j.movingPercentileBy(percentile, 3, Function.identity())
            ).withMessage("Percentile must be between 0 and 100, inclusive");
        }

        @ParameterizedTest(name = "windowSize of {0}")
        @ValueSource(ints = {-1, 0, 1})
        void byFunctionWindowSizeMustBeGreaterThanOne(final int size) {
            assertThatIllegalArgumentException().isThrownBy(() ->
                    Gatherers4j.movingPercentileBy(50, size, Function.identity())
            );
        }

        @Test
        void emptyStream() {
            // Arrange
            final Stream<BigDecimal> input = Stream.empty();

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.movingPercentile(90, 3))
                    .toList();

            // Assert
            assertThat(output).isEmpty();
        }

        @Test
        void exactRank() {
            // Arrange
            final Stream<BigDecimal> input = zeroToOneHundred();

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.movingPercentile(7, 101).excludePartialValues())
                    .toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(new BigDecimal("7"));
        }

        @Test
        void exactRankWithMathContext() {
            // Arrange
            final Stream<BigDecimal> input = zeroToOneHundred();

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.movingPercentile(29, 101)
                            .excludePartialValues()
                            .withMathContext(MathContext.DECIMAL128))
                    .toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(new BigDecimal("29"));
        }

        @SuppressWarnings("DataFlowIssue")
        @Test
        void functionMustNotBeNull() {
            assertThatIllegalArgumentException().isThrownBy(() ->
                    Gatherers4j.movingPercentileBy(50, 2, null)
            );
        }

        @Test
        void ignoresNulls() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of(null, BigDecimal.ONE, BigDecimal.TWO, BigDecimal.TEN);

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.movingPercentile(90, 3))
                    .toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            BigDecimal.ONE,
                            new BigDecimal("1.9"),
                            new BigDecimal("8.4")
                    );
        }

        @Test
        @SuppressWarnings("DataFlowIssue")
        void mathContextCannotBeNull() {
            assertThatIllegalArgumentException().isThrownBy(() ->
                    Gatherers4j.movingPercentile(50, 2).withMathContext(null)
            );
        }

        @Test
        void movingPercentile() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of("1", "2", "3", "4", "5").map(BigDecimal::new);

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.movingPercentile(90, 3))
                    .toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            new BigDecimal("1"),
                            new BigDecimal("1.9"),
                            new BigDecimal("2.8"),
                            new BigDecimal("3.8"),
                            new BigDecimal("4.8")
                    );
        }

        @Test
        void movingPercentileBy() {
            // Arrange
            final List<TestValueHolder> input = List.of(
                    new TestValueHolder(1, new BigDecimal("1")),
                    new TestValueHolder(2, new BigDecimal("2")),
                    new TestValueHolder(3, new BigDecimal("10")),
                    new TestValueHolder(4, new BigDecimal("20")),
                    new TestValueHolder(5, new BigDecimal("30"))
            );

            // Act
            final List<BigDecimal> output = input.stream()
                    .gather(Gatherers4j.movingPercentileBy(90, 2, TestValueHolder::value))
                    .toList();

            // Assert
            assertThat(output)
                    .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                    .containsExactly(
                            new BigDecimal("1"),
                            new BigDecimal("1.9"),
                            new BigDecimal("9.2"),
                            new BigDecimal("19"),
                            new BigDecimal("29")
                    );
        }

        @Test
        void movingPercentileExcludingPartialValues() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of("1", "2", "3", "4", "5").map(BigDecimal::new);

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.movingPercentile(90, 3).excludePartialValues())
                    .toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            new BigDecimal("2.8"),
                            new BigDecimal("3.8"),
                            new BigDecimal("4.8")
                    );
        }

        @Test
        void movingPercentileWithOriginal() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of("1", "2", "3", "4").map(BigDecimal::new);

            // Act
            final List<WithOriginal<BigDecimal, BigDecimal>> output = input
                    .gather(Gatherers4j.movingPercentile(90, 2).withOriginal())
                    .toList();

            // Assert
            assertThat(output)
                    .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                    .containsExactly(
                            new WithOriginal<>(new BigDecimal("1"), new BigDecimal("1")),
                            new WithOriginal<>(new BigDecimal("2"), new BigDecimal("1.9")),
                            new WithOriginal<>(new BigDecimal("3"), new BigDecimal("2.9")),
                            new WithOriginal<>(new BigDecimal("4"), new BigDecimal("3.9"))
                    );
        }

        @ParameterizedTest(name = "percentile of {0}")
        @ValueSource(doubles = {-0.1, 100.1, Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY})
        void percentileMustBeBetweenZeroAndOneHundred(final double percentile) {
            assertThatIllegalArgumentException().isThrownBy(() ->
                    Gatherers4j.movingPercentile(percentile, 3)
            ).withMessage("Percentile must be between 0 and 100, inclusive");
        }

        @Test
        void percentileOfOneHundredIsMaximum() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of("5", "1", "3", "2").map(BigDecimal::new);

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.movingPercentile(100, 2))
                    .toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            new BigDecimal("5"),
                            new BigDecimal("5"),
                            new BigDecimal("3"),
                            new BigDecimal("3")
                    );
        }

        @Test
        void percentileOfZeroIsMinimum() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of("5", "1", "3", "2").map(BigDecimal::new);

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.movingPercentile(0, 2))
                    .toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            new BigDecimal("5"),
                            new BigDecimal("1"),
                            new BigDecimal("1"),
                            new BigDecimal("2")
                    );
        }

        @Test
        void singleElement() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of(new BigDecimal("42"));

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.movingPercentile(90, 3))
                    .toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(new BigDecimal("42"));
        }

        @Test
        void treatNullAsZero() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of(
                    new BigDecimal("2"),
                    new BigDecimal("3"),
                    null,
                    new BigDecimal("4")
            );

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.movingPercentile(90, 2).treatNullAsZero())
                    .toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            new BigDecimal("2"),
                            new BigDecimal("2.9"),
                            new BigDecimal("2.7"),
                            new BigDecimal("3.6")
                    );
        }

        @ParameterizedTest(name = "windowSize of {0}")
        @ValueSource(ints = {-1, 0, 1})
        void windowSizeMustBeGreaterThanOne(final int size) {
            assertThatIllegalArgumentException().isThrownBy(() ->
                    Gatherers4j.movingPercentile(50, size)
            );
        }
    }

    @Nested
    class InterpolatedScale {

        @ParameterizedTest(name = "P{2} of {0} and {1} is {3}")
        @CsvSource({
                "1.0,  2.0,  50, 1.5",
                "3.5,  3.5,  50, 3.5",
                "0,    0,    50, 0",
                "1.10, 1.10, 50, 1.10",
                "100,  200,  50, 150",
                "1.0,  2.0,  90, 1.9",
                "10.0, 2.42, 50, 6.21",
                "1,    2,    25, 1.25"
        })
        void interpolationAddsNoTrailingZeros(
                final String first,
                final String second,
                final double percentile,
                final String expected
        ) {
            // Arrange
            final Stream<BigDecimal> input = Stream.of(first, second).map(BigDecimal::new);

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.runningPercentile(percentile))
                    .toList();

            // Assert
            assertThat(output.getLast()).hasToString(expected);
        }
    }

    @Nested
    class Running {

        @ParameterizedTest(name = "percentile of {0}")
        @ValueSource(doubles = {-0.1, 100.1, Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY})
        void byFunctionPercentileMustBeBetweenZeroAndOneHundred(final double percentile) {
            assertThatIllegalArgumentException().isThrownBy(() ->
                    Gatherers4j.runningPercentileBy(percentile, Function.identity())
            ).withMessage("Percentile must be between 0 and 100, inclusive");
        }

        @Test
        void emptyStream() {
            // Arrange
            final Stream<BigDecimal> input = Stream.empty();

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.runningPercentile(90))
                    .toList();

            // Assert
            assertThat(output).isEmpty();
        }

        @Test
        void exactRank() {
            // Arrange
            final Stream<BigDecimal> input = zeroToOneHundred();

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.runningPercentile(7))
                    .toList();

            // Assert
            assertThat(output.getLast()).isEqualByComparingTo(new BigDecimal("7"));
        }

        @Test
        void exactRankWithMathContext() {
            // Arrange
            final Stream<BigDecimal> input = zeroToOneHundred();

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.runningPercentile(29).withMathContext(MathContext.DECIMAL128))
                    .toList();

            // Assert
            assertThat(output.getLast()).isEqualByComparingTo(new BigDecimal("29"));
        }

        @SuppressWarnings("DataFlowIssue")
        @Test
        void functionMustNotBeNull() {
            assertThatIllegalArgumentException().isThrownBy(() ->
                    Gatherers4j.runningPercentileBy(50, null)
            );
        }

        @Test
        void ignoresNulls() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of(null, BigDecimal.ONE, BigDecimal.TWO, BigDecimal.TEN);

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.runningPercentile(90))
                    .toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            BigDecimal.ONE,
                            new BigDecimal("1.9"),
                            new BigDecimal("8.4")
                    );
        }

        @Test
        @SuppressWarnings("DataFlowIssue")
        void mathContextCannotBeNull() {
            assertThatIllegalArgumentException().isThrownBy(() ->
                    Gatherers4j.runningPercentile(50).withMathContext(null)
            );
        }

        @ParameterizedTest(name = "percentile of {0}")
        @ValueSource(doubles = {-0.1, 100.1, Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY})
        void percentileMustBeBetweenZeroAndOneHundred(final double percentile) {
            assertThatIllegalArgumentException().isThrownBy(() ->
                    Gatherers4j.runningPercentile(percentile)
            ).withMessage("Percentile must be between 0 and 100, inclusive");
        }

        @Test
        void percentileOfOneHundredIsMaximum() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of("5", "1", "3", "7").map(BigDecimal::new);

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.runningPercentile(100))
                    .toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            new BigDecimal("5"),
                            new BigDecimal("5"),
                            new BigDecimal("5"),
                            new BigDecimal("7")
                    );
        }

        @Test
        void percentileOfZeroIsMinimum() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of("5", "1", "3", "0").map(BigDecimal::new);

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.runningPercentile(0))
                    .toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            new BigDecimal("5"),
                            new BigDecimal("1"),
                            new BigDecimal("1"),
                            new BigDecimal("0")
                    );
        }

        @Test
        void runningPercentile() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of("1", "2", "3", "4").map(BigDecimal::new);

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.runningPercentile(90))
                    .toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            new BigDecimal("1"),
                            new BigDecimal("1.9"),
                            new BigDecimal("2.8"),
                            new BigDecimal("3.7")
                    );
        }

        @Test
        void runningPercentileBy() {
            // Arrange
            final List<TestValueHolder> input = List.of(
                    new TestValueHolder(1, new BigDecimal("1")),
                    new TestValueHolder(2, new BigDecimal("2")),
                    new TestValueHolder(3, new BigDecimal("10")),
                    new TestValueHolder(4, new BigDecimal("20"))
            );

            // Act
            final List<BigDecimal> output = input.stream()
                    .gather(Gatherers4j.runningPercentileBy(90, TestValueHolder::value))
                    .toList();

            // Assert
            assertThat(output)
                    .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                    .containsExactly(
                            new BigDecimal("1"),
                            new BigDecimal("1.9"),
                            new BigDecimal("8.4"),
                            new BigDecimal("17")
                    );
        }

        @Test
        void runningPercentileWithOriginal() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of("1", "2", "3", "4").map(BigDecimal::new);

            // Act
            final List<WithOriginal<BigDecimal, BigDecimal>> output = input
                    .gather(Gatherers4j.runningPercentile(90).withOriginal())
                    .toList();

            // Assert
            assertThat(output)
                    .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                    .containsExactly(
                            new WithOriginal<>(new BigDecimal("1"), new BigDecimal("1")),
                            new WithOriginal<>(new BigDecimal("2"), new BigDecimal("1.9")),
                            new WithOriginal<>(new BigDecimal("3"), new BigDecimal("2.8")),
                            new WithOriginal<>(new BigDecimal("4"), new BigDecimal("3.7"))
                    );
        }

        @Test
        void singleElement() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of(new BigDecimal("42"));

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.runningPercentile(90))
                    .toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(new BigDecimal("42"));
        }

        @Test
        void treatNullAsZero() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of(
                    new BigDecimal("2"),
                    new BigDecimal("3"),
                    null,
                    new BigDecimal("4")
            );

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.runningPercentile(90).treatNullAsZero())
                    .toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            new BigDecimal("2"),
                            new BigDecimal("2.9"),
                            new BigDecimal("2.8"),
                            new BigDecimal("3.7")
                    );
        }
    }
}
