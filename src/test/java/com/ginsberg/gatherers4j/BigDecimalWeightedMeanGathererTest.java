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
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.ginsberg.gatherers4j.util.TestUtils.BIG_DECIMAL_RECURSIVE_COMPARISON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class BigDecimalWeightedMeanGathererTest {

    private static Stream<BigDecimal> decimals(final String... values) {
        return Arrays.stream(values).map(it -> it == null ? null : new BigDecimal(it));
    }

    private static List<BigDecimal> weights(final String... values) {
        return Arrays.stream(values).map(BigDecimal::new).toList();
    }

    @Nested
    class Linear {

        @Test
        void emptyStream() {
            // Arrange
            final Stream<BigDecimal> input = Stream.empty();

            // Act
            final List<BigDecimal> output = input.gather(Gatherers4j.movingWeightedMean(3)).toList();

            // Assert
            assertThat(output).isEmpty();
        }

        @Test
        void singleElement() {
            // Arrange
            final Stream<BigDecimal> input = decimals("5");

            // Act
            final List<BigDecimal> output = input.gather(Gatherers4j.movingWeightedMean(3)).toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(new BigDecimal("5"));
        }

        @Test
        void partialAndFullWindows() {
            // Arrange
            final Stream<BigDecimal> input = decimals("10", "20", "30", "40");

            // Act
            final List<BigDecimal> output = input.gather(Gatherers4j.movingWeightedMean(3)).toList();

            // Assert
            // [10]         -> 10 (weight 3, the newest weight of 1..3)
            // [10, 20]     -> (10*2 + 20*3) / 5 = 16
            // [10, 20, 30] -> (10*1 + 20*2 + 30*3) / 6 = 140/6
            // [20, 30, 40] -> (20*1 + 30*2 + 40*3) / 6 = 200/6
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            new BigDecimal("10"),
                            new BigDecimal("16"),
                            new BigDecimal("23.33333333333333"),
                            new BigDecimal("33.33333333333333")
                    );
        }

        @Test
        void windowOfTwo() {
            // Arrange
            final Stream<BigDecimal> input = decimals("3", "6", "9", "0");

            // Act
            final List<BigDecimal> output = input.gather(Gatherers4j.movingWeightedMean(2)).toList();

            // Assert
            // [3] -> 3, [3, 6] -> 15/3 = 5, [6, 9] -> 24/3 = 8, [9, 0] -> 9/3 = 3
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            new BigDecimal("3"),
                            new BigDecimal("5"),
                            new BigDecimal("8"),
                            new BigDecimal("3")
                    );
        }

        @Test
        void negativeValues() {
            // Arrange
            final Stream<BigDecimal> input = decimals("-3", "3", "-6");

            // Act
            final List<BigDecimal> output = input.gather(Gatherers4j.movingWeightedMean(2)).toList();

            // Assert
            // [-3] -> -3, [-3, 3] -> (-3 + 6) / 3 = 1, [3, -6] -> (3 - 12) / 3 = -3
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            new BigDecimal("-3"),
                            new BigDecimal("1"),
                            new BigDecimal("-3")
                    );
        }

        @Test
        void ignoresNulls() {
            // Arrange
            final Stream<BigDecimal> input = decimals(null, "10", null, "20", "30");

            // Act
            final List<BigDecimal> output = input.gather(Gatherers4j.movingWeightedMean(3)).toList();

            // Assert
            // Nulls emit nothing and do not move the window.
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            new BigDecimal("10"),
                            new BigDecimal("16"),
                            new BigDecimal("23.33333333333333")
                    );
        }

        @Test
        void treatNullAsZero() {
            // Arrange
            final Stream<BigDecimal> input = decimals(null, "30", "60");

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.movingWeightedMean(3).treatNullAsZero())
                    .toList();

            // Assert
            // [0] -> 0, [0, 30] -> (0*2 + 30*3) / 5 = 18, [0, 30, 60] -> (0 + 60 + 180) / 6 = 40
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            new BigDecimal("0"),
                            new BigDecimal("18"),
                            new BigDecimal("40")
                    );
        }

        @Test
        void treatNullAsOne() {
            // Arrange
            final Stream<BigDecimal> input = decimals(null, "3");

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.movingWeightedMean(2).treatNullAsOne())
                    .toList();

            // Assert
            // [1] -> 1, [1, 3] -> (1 + 6) / 3
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            new BigDecimal("1"),
                            new BigDecimal("2.333333333333333")
                    );
        }

        @Test
        void treatNullAsValue() {
            // Arrange
            final Stream<BigDecimal> input = decimals(null, "4");

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.movingWeightedMean(2).treatNullAs(BigDecimal.TEN))
                    .toList();

            // Assert
            // [10] -> 10, [10, 4] -> (10 + 8) / 3 = 6
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            new BigDecimal("10"),
                            new BigDecimal("6")
                    );
        }

        @Test
        void excludePartialValues() {
            // Arrange
            final Stream<BigDecimal> input = decimals("10", "20", "30", "40");

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.movingWeightedMean(3).excludePartialValues())
                    .toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            new BigDecimal("23.33333333333333"),
                            new BigDecimal("33.33333333333333")
                    );
        }

        @Test
        void excludePartialValuesWithShortStream() {
            // Arrange
            final Stream<BigDecimal> input = decimals("10", "20");

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.movingWeightedMean(3).excludePartialValues())
                    .toList();

            // Assert
            assertThat(output).isEmpty();
        }

        @Test
        void withOriginal() {
            // Arrange
            final Stream<BigDecimal> input = decimals("10", "20", "30", "40");

            // Act
            final List<WithOriginal<BigDecimal, BigDecimal>> output = input
                    .gather(Gatherers4j
                            .movingWeightedMean(3)
                            .excludePartialValues()
                            .withOriginal()
                    ).toList();

            // Assert
            assertThat(output)
                    .map(WithOriginal::calculated)
                    .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                    .containsExactly(
                            new BigDecimal("23.33333333333333"),
                            new BigDecimal("33.33333333333333")
                    );
            assertThat(output)
                    .map(WithOriginal::original)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            new BigDecimal("30"),
                            new BigDecimal("40")
                    );
        }

        @Test
        void mathContextChange() {
            // Arrange
            final Stream<BigDecimal> input = decimals("10", "20", "30", "40");

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.movingWeightedMean(3).withMathContext(new MathContext(3)))
                    .toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            new BigDecimal("10"),
                            new BigDecimal("16"),
                            new BigDecimal("23.3"),
                            new BigDecimal("33.3")
                    );
        }

        @Test
        void inputsAreRoundedToMathContextOnEntry() {
            // Arrange
            final Stream<BigDecimal> input = decimals("1.004", "2.006", "2.006");

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.movingWeightedMean(2).withMathContext(new MathContext(3)))
                    .toList();

            // Assert
            // Inputs become 1.00, 2.01, 2.01.
            // [1.00] -> 1.00, [1.00, 2.01] -> 5.02/3 = 1.67, [2.01, 2.01] -> 6.03/3 = 2.01
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            new BigDecimal("1.00"),
                            new BigDecimal("1.67"),
                            new BigDecimal("2.01")
                    );
        }

        @SuppressWarnings("DataFlowIssue")
        @Test
        void mathContextCannotBeNull() {
            assertThatIllegalArgumentException().isThrownBy(() ->
                    Gatherers4j.movingWeightedMean(3).withMathContext(null)
            ).withMessage("MathContext must not be null");
        }

        @Test
        void by() {
            // Arrange
            final List<TestValueHolder> input = List.of(
                    new TestValueHolder(1, new BigDecimal("10")),
                    new TestValueHolder(2, new BigDecimal("20")),
                    new TestValueHolder(3, new BigDecimal("30")),
                    new TestValueHolder(4, new BigDecimal("40"))
            );

            // Act
            final List<BigDecimal> output = input.stream()
                    .gather(Gatherers4j.movingWeightedMeanBy(3, TestValueHolder::value))
                    .toList();

            // Assert
            assertThat(output)
                    .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                    .containsExactly(
                            new BigDecimal("10"),
                            new BigDecimal("16"),
                            new BigDecimal("23.33333333333333"),
                            new BigDecimal("33.33333333333333")
                    );
        }

        @Test
        void byWithOriginal() {
            // Arrange
            final List<TestValueHolder> input = List.of(
                    new TestValueHolder(1, new BigDecimal("10")),
                    new TestValueHolder(2, new BigDecimal("20")),
                    new TestValueHolder(3, new BigDecimal("30"))
            );

            // Act
            final List<WithOriginal<TestValueHolder, BigDecimal>> output = input.stream()
                    .gather(Gatherers4j.movingWeightedMeanBy(2, TestValueHolder::value).withOriginal())
                    .toList();

            // Assert
            assertThat(output)
                    .extracting(WithOriginal::calculated)
                    .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                    .containsExactly(
                            new BigDecimal("10"),
                            new BigDecimal("16.66666666666667"),
                            new BigDecimal("26.66666666666667")
                    );
            assertThat(output)
                    .map(WithOriginal::original)
                    .containsExactlyElementsOf(input);
        }

        @SuppressWarnings("DataFlowIssue")
        @Test
        void mappingFunctionCannotBeNull() {
            assertThatIllegalArgumentException().isThrownBy(() ->
                    Gatherers4j.movingWeightedMeanBy(3, (Function<BigDecimal, BigDecimal>) null)
            ).withMessage("Mapping function must not be null");
        }

        @ParameterizedTest(name = "windowSize of {0}")
        @ValueSource(ints = {-1, 0, 1})
        void windowSizeMustBeGreaterThanOne(final int windowSize) {
            assertThatIllegalArgumentException().isThrownBy(() ->
                    Gatherers4j.movingWeightedMean(windowSize)
            ).withMessage("Window size must be greater than 1");
            assertThatIllegalArgumentException().isThrownBy(() ->
                    Gatherers4j.movingWeightedMeanBy(windowSize, Function.<BigDecimal>identity())
            ).withMessage("Window size must be greater than 1");
        }

        @ParameterizedTest(name = "windowSize of {0}")
        @ValueSource(ints = {2, 3, 7, 50})
        void longStreamMatchesFromScratchCalculation(final int windowSize) {
            // Arrange
            final Random random = new Random(20260101L);
            final List<BigDecimal> input = IntStream.range(0, 3000)
                    .mapToObj(_ -> BigDecimal.valueOf(random.nextInt(2_000_001) - 1_000_000, 4))
                    .toList();

            // Act
            final List<BigDecimal> output = input.stream()
                    .gather(Gatherers4j.movingWeightedMean(windowSize))
                    .toList();

            // Assert
            assertThat(output).hasSameSizeAs(input);
            for (int i = 0; i < input.size(); i++) {
                final List<BigDecimal> window = input.subList(Math.max(0, i + 1 - windowSize), i + 1);
                final int k = window.size();
                BigDecimal weighted = BigDecimal.ZERO;
                // k values use the newest k of the weights 1..windowSize
                for (int j = 0; j < k; j++) {
                    weighted = weighted.add(window.get(j).multiply(BigDecimal.valueOf(windowSize - k + 1 + j)));
                }
                final BigDecimal expected = weighted.divide(
                        BigDecimal.valueOf(k * (2 * windowSize - k + 1) / 2), MathContext.DECIMAL64
                );
                assertThat(output.get(i)).as("index %d", i).isEqualByComparingTo(expected);
            }
        }

        @ParameterizedTest(name = "windowSize of {0}")
        @ValueSource(ints = {2, 3, 5, 10})
        void sameAsCustomWeightsOfOneThroughN(final int windowSize) {
            // Arrange
            final Random random = new Random(7L);
            final List<BigDecimal> input = IntStream.range(0, 500)
                    .mapToObj(i -> i % 17 == 0 ? null : BigDecimal.valueOf(random.nextInt(100_000), 3))
                    .toList();
            final List<BigDecimal> oneThroughN = IntStream.rangeClosed(1, windowSize)
                    .mapToObj(BigDecimal::valueOf)
                    .toList();

            // Act
            final List<BigDecimal> linear = input.stream()
                    .gather(Gatherers4j.movingWeightedMean(windowSize))
                    .toList();
            final List<BigDecimal> custom = input.stream()
                    .gather(Gatherers4j.movingWeightedMean(oneThroughN))
                    .toList();

            // Assert
            // Includes partial windows: k values use the newest k of the weights 1..n
            assertThat(linear).isNotEmpty();
            assertThat(custom)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactlyElementsOf(linear);
        }

        @ParameterizedTest(name = "windowSize of {0}")
        @ValueSource(ints = {2, 3, 5, 10})
        void byFormSameAsCustomWeightsOfOneThroughN(final int windowSize) {
            // Arrange
            final Random random = new Random(11L);
            final List<TestValueHolder> input = IntStream.range(0, 500)
                    .mapToObj(i -> new TestValueHolder(i, i % 13 == 0 ? null : BigDecimal.valueOf(random.nextInt(100_000), 3)))
                    .toList();
            final List<BigDecimal> oneThroughN = IntStream.rangeClosed(1, windowSize)
                    .mapToObj(BigDecimal::valueOf)
                    .toList();

            // Act
            final List<BigDecimal> linear = input.stream()
                    .gather(Gatherers4j.movingWeightedMeanBy(windowSize, TestValueHolder::value))
                    .toList();
            final List<BigDecimal> custom = input.stream()
                    .gather(Gatherers4j.movingWeightedMeanBy(oneThroughN, TestValueHolder::value))
                    .toList();

            // Assert
            assertThat(linear).isNotEmpty();
            assertThat(custom)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactlyElementsOf(linear);
        }
    }

    @Nested
    class CustomWeights {

        @Test
        void emptyStream() {
            // Arrange
            final Stream<BigDecimal> input = Stream.empty();

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.movingWeightedMean(weights("1", "1", "2")))
                    .toList();

            // Assert
            assertThat(output).isEmpty();
        }

        @Test
        void singleElement() {
            // Arrange
            final Stream<BigDecimal> input = decimals("5");

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.movingWeightedMean(weights("1", "1", "2")))
                    .toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(new BigDecimal("5"));
        }

        @Test
        void weightsAreOrderedOldestToNewest() {
            // Arrange
            final Stream<BigDecimal> input = decimals("10", "20", "30", "40");

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.movingWeightedMean(weights("1", "1", "2")))
                    .toList();

            // Assert
            // [10]         uses [2]       -> 20 / 2 = 10
            // [10, 20]     uses [1, 2]    -> (10 + 40) / 3
            // [10, 20, 30] uses [1, 1, 2] -> (10 + 20 + 60) / 4 = 22.5
            // [20, 30, 40] uses [1, 1, 2] -> (20 + 30 + 80) / 4 = 32.5
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            new BigDecimal("10"),
                            new BigDecimal("16.66666666666667"),
                            new BigDecimal("22.5"),
                            new BigDecimal("32.5")
                    );
        }

        @Test
        void weightsAreNotRequiredToSumToOne() {
            // Arrange
            final List<BigDecimal> input = List.of(
                    new BigDecimal("10"), new BigDecimal("20"), new BigDecimal("30"), new BigDecimal("40")
            );

            // Act
            final List<BigDecimal> whole = input.stream()
                    .gather(Gatherers4j.movingWeightedMean(weights("1", "1", "2")))
                    .toList();
            final List<BigDecimal> fractions = input.stream()
                    .gather(Gatherers4j.movingWeightedMean(weights("0.25", "0.25", "0.5")))
                    .toList();

            // Assert
            assertThat(fractions)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactlyElementsOf(whole);
        }

        @Test
        void forecastingExample() {
            // Arrange
            final Stream<BigDecimal> input = decimals("100", "120", "150");

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.movingWeightedMean(weights("0.2", "0.3", "0.5")))
                    .toList();

            // Assert
            // [100]           uses [0.5]       -> 100
            // [100, 120]      uses [0.3, 0.5]  -> (30 + 60) / 0.8 = 112.5
            // [100, 120, 150] uses all         -> 20 + 36 + 75 = 131
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            new BigDecimal("100"),
                            new BigDecimal("112.5"),
                            new BigDecimal("131")
                    );
        }

        @Test
        void zeroWeightsAreAllowed() {
            // Arrange
            final Stream<BigDecimal> input = decimals("10", "20", "30", "40");

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.movingWeightedMean(weights("1", "2", "0")))
                    .toList();

            // Assert
            // [10] uses [0], whose weights sum to zero, so nothing is emitted.
            // [10, 20]     uses [2, 0]    -> 20 / 2 = 10
            // [10, 20, 30] uses [1, 2, 0] -> 50 / 3
            // [20, 30, 40] uses [1, 2, 0] -> 80 / 3
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            new BigDecimal("10"),
                            new BigDecimal("16.66666666666667"),
                            new BigDecimal("26.66666666666667")
                    );
        }

        @Test
        void weightsAreUsedAsGivenAndInputsAreRoundedOnEntry() {
            // Arrange
            final Stream<BigDecimal> input = decimals("0", "10");

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j
                            .movingWeightedMean(weights("1.04", "1.06"))
                            .withMathContext(new MathContext(2))
                    )
                    .toList();

            // Assert
            // Rounding the weights to 2 digits would give (0 + 11) / 2.1 = 5.2.
            // Used exactly: (0 + 10.6) / 2.10 = 5.0476 -> 5.0
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            new BigDecimal("0"),
                            new BigDecimal("5.0")
                    );
        }

        @Test
        void weightsAreCopied() {
            // Arrange
            final List<BigDecimal> mutableWeights = new ArrayList<>(weights("1", "1", "2"));
            final var gatherer = Gatherers4j.movingWeightedMean(mutableWeights);
            mutableWeights.set(2, new BigDecimal("1000"));

            // Act
            final List<BigDecimal> output = decimals("10", "20", "30").gather(gatherer).toList();

            // Assert
            assertThat(output.getLast()).isEqualByComparingTo(new BigDecimal("22.5"));
        }

        @Test
        void gathererCanBeReused() {
            // Arrange
            final var gatherer = Gatherers4j.movingWeightedMean(weights("1", "1", "2"));

            // Act
            final List<BigDecimal> first = decimals("10", "20", "30").gather(gatherer).toList();
            final List<BigDecimal> second = decimals("10", "20", "30").gather(gatherer).toList();

            // Assert
            assertThat(second)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactlyElementsOf(first);
        }

        @Test
        void ignoresNulls() {
            // Arrange
            final Stream<BigDecimal> input = decimals(null, "10", null, "20", "30");

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.movingWeightedMean(weights("1", "1", "2")))
                    .toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            new BigDecimal("10"),
                            new BigDecimal("16.66666666666667"),
                            new BigDecimal("22.5")
                    );
        }

        @Test
        void treatNullAsZero() {
            // Arrange
            final Stream<BigDecimal> input = decimals(null, "30", "60");

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.movingWeightedMean(weights("1", "1", "2")).treatNullAsZero())
                    .toList();

            // Assert
            // [0] -> 0, [0, 30] -> 60/3 = 20, [0, 30, 60] -> (0 + 30 + 120) / 4 = 37.5
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            new BigDecimal("0"),
                            new BigDecimal("20"),
                            new BigDecimal("37.5")
                    );
        }

        @Test
        void excludePartialValues() {
            // Arrange
            final Stream<BigDecimal> input = decimals("10", "20", "30", "40");

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.movingWeightedMean(weights("1", "1", "2")).excludePartialValues())
                    .toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            new BigDecimal("22.5"),
                            new BigDecimal("32.5")
                    );
        }

        @Test
        void withOriginal() {
            // Arrange
            final Stream<BigDecimal> input = decimals("10", "20", "30", "40");

            // Act
            final List<WithOriginal<BigDecimal, BigDecimal>> output = input
                    .gather(Gatherers4j
                            .movingWeightedMean(weights("1", "1", "2"))
                            .excludePartialValues()
                            .withOriginal()
                    ).toList();

            // Assert
            assertThat(output)
                    .map(WithOriginal::calculated)
                    .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                    .containsExactly(new BigDecimal("22.5"), new BigDecimal("32.5"));
            assertThat(output)
                    .map(WithOriginal::original)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(new BigDecimal("30"), new BigDecimal("40"));
        }

        @Test
        void mathContextChange() {
            // Arrange
            final Stream<BigDecimal> input = decimals("10", "20", "30", "40");

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j
                            .movingWeightedMean(weights("1", "1", "3"))
                            .withMathContext(new MathContext(3))
                    )
                    .toList();

            // Assert
            // [10] -> 10, [10, 20] -> 70/4 = 17.5, [10, 20, 30] -> 120/5 = 24, [20, 30, 40] -> 170/5 = 34
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            new BigDecimal("10"),
                            new BigDecimal("17.5"),
                            new BigDecimal("24"),
                            new BigDecimal("34")
                    );
        }

        @SuppressWarnings("DataFlowIssue")
        @Test
        void mathContextCannotBeNull() {
            assertThatIllegalArgumentException().isThrownBy(() ->
                    Gatherers4j.movingWeightedMean(weights("1", "2")).withMathContext(null)
            ).withMessage("MathContext must not be null");
        }

        @Test
        void by() {
            // Arrange
            final List<TestValueHolder> input = List.of(
                    new TestValueHolder(1, new BigDecimal("10")),
                    new TestValueHolder(2, new BigDecimal("20")),
                    new TestValueHolder(3, new BigDecimal("30")),
                    new TestValueHolder(4, new BigDecimal("40"))
            );

            // Act
            final List<BigDecimal> output = input.stream()
                    .gather(Gatherers4j.movingWeightedMeanBy(weights("1", "1", "2"), TestValueHolder::value))
                    .toList();

            // Assert
            assertThat(output)
                    .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                    .containsExactly(
                            new BigDecimal("10"),
                            new BigDecimal("16.66666666666667"),
                            new BigDecimal("22.5"),
                            new BigDecimal("32.5")
                    );
        }

        @SuppressWarnings("DataFlowIssue")
        @Test
        void mappingFunctionCannotBeNull() {
            assertThatIllegalArgumentException().isThrownBy(() ->
                    Gatherers4j.movingWeightedMeanBy(weights("1", "2"), (Function<BigDecimal, BigDecimal>) null)
            ).withMessage("Mapping function must not be null");
        }

        @SuppressWarnings("DataFlowIssue")
        @Test
        void weightsCannotBeNull() {
            assertThatIllegalArgumentException().isThrownBy(() ->
                    Gatherers4j.movingWeightedMean((List<BigDecimal>) null)
            ).withMessage("Weights must not be null");
            assertThatIllegalArgumentException().isThrownBy(() ->
                    Gatherers4j.movingWeightedMeanBy((List<BigDecimal>) null, Function.<BigDecimal>identity())
            ).withMessage("Weights must not be null");
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidWeights")
        void invalidWeightsAreRejected(
                final String name,
                final List<BigDecimal> invalid,
                final String message
        ) {
            assertThatIllegalArgumentException().isThrownBy(() ->
                    Gatherers4j.movingWeightedMean(invalid)
            ).withMessage(message);
            assertThatIllegalArgumentException().isThrownBy(() ->
                    Gatherers4j.movingWeightedMeanBy(invalid, Function.<BigDecimal>identity())
            ).withMessage(message);
        }

        static Stream<Arguments> invalidWeights() {
            return Stream.of(
                    Arguments.of("empty", List.<BigDecimal>of(), "Weights must contain at least 2 values"),
                    Arguments.of("single weight", List.of(BigDecimal.ONE), "Weights must contain at least 2 values"),
                    Arguments.of("null weight", Arrays.asList(BigDecimal.ONE, null), "Weights must not contain null"),
                    Arguments.of("negative weight", List.of(BigDecimal.TWO, new BigDecimal("-1")), "Weights must not be negative"),
                    Arguments.of("negative cancelling positive", List.of(BigDecimal.ONE, new BigDecimal("-1")), "Weights must not be negative"),
                    Arguments.of("zero sum", List.of(BigDecimal.ZERO, BigDecimal.ZERO), "Weights must not sum to zero")
            );
        }

        @ParameterizedTest(name = "windowSize of {0}")
        @ValueSource(ints = {2, 3, 5, 12})
        void longStreamMatchesFromScratchCalculation(final int windowSize) {
            // Arrange
            final Random random = new Random(99L + windowSize);
            final List<BigDecimal> weights = new ArrayList<>(IntStream.range(0, windowSize)
                    .mapToObj(_ -> BigDecimal.valueOf(random.nextInt(10), 1))
                    .toList());
            weights.set(windowSize - 1, new BigDecimal("0.7"));
            final List<BigDecimal> input = IntStream.range(0, 3000)
                    .mapToObj(_ -> BigDecimal.valueOf(random.nextInt(2_000_001) - 1_000_000, 4))
                    .toList();

            // Act
            final List<BigDecimal> output = input.stream()
                    .gather(Gatherers4j.movingWeightedMean(weights))
                    .toList();

            // Assert
            assertThat(output).hasSameSizeAs(input);
            for (int i = 0; i < input.size(); i++) {
                final List<BigDecimal> window = input.subList(Math.max(0, i + 1 - windowSize), i + 1);
                final List<BigDecimal> used = weights.subList(windowSize - window.size(), windowSize);
                BigDecimal weighted = BigDecimal.ZERO;
                BigDecimal weightSum = BigDecimal.ZERO;
                for (int j = 0; j < window.size(); j++) {
                    weighted = weighted.add(window.get(j).multiply(used.get(j)));
                    weightSum = weightSum.add(used.get(j));
                }
                final BigDecimal expected = weighted.divide(weightSum, MathContext.DECIMAL64);
                assertThat(output.get(i)).as("index %d", i).isEqualByComparingTo(expected);
            }
        }
    }
}
