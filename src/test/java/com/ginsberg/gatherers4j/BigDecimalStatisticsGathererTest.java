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

import com.ginsberg.gatherers4j.dto.StatisticalSummary;
import com.ginsberg.gatherers4j.dto.WithOriginal;
import com.ginsberg.gatherers4j.enums.Dataset;
import com.ginsberg.gatherers4j.util.TestValueHolder;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.ginsberg.gatherers4j.util.TestUtils.BIG_DECIMAL_RECURSIVE_COMPARISON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class BigDecimalStatisticsGathererTest {

    private static StatisticalSummary summary(
            final long count,
            final String sum,
            final String mean,
            final String min,
            final String max,
            final String populationVariance,
            final String populationStandardDeviation,
            final @Nullable String sampleVariance,
            final @Nullable String sampleStandardDeviation
    ) {
        return new StatisticalSummary(
                count,
                new BigDecimal(sum),
                new BigDecimal(mean),
                new BigDecimal(min),
                new BigDecimal(max),
                new BigDecimal(populationVariance),
                new BigDecimal(populationStandardDeviation),
                sampleVariance == null ? null : new BigDecimal(sampleVariance),
                sampleStandardDeviation == null ? null : new BigDecimal(sampleStandardDeviation)
        );
    }

    // Values with two decimal places in [-5.00, 5.00], so duplicates are common
    private static List<BigDecimal> randomValues(final long seed) {
        final Random random = new Random(seed);
        return IntStream.range(0, 200)
                .mapToObj(_ -> BigDecimal.valueOf(random.nextInt(-500, 501), 2))
                .toList();
    }

    private static List<BigDecimal> extract(
            final List<StatisticalSummary> summaries,
            final Function<StatisticalSummary, @Nullable BigDecimal> field
    ) {
        return summaries.stream().map(field).toList();
    }

    @Nested
    class Moving {

        @Test
        void emptyStream() {
            // Arrange
            final Stream<BigDecimal> input = Stream.empty();

            // Act
            final List<StatisticalSummary> output = input
                    .gather(Gatherers4j.movingStatistics(3))
                    .toList();

            // Assert
            assertThat(output).isEmpty();
        }

        @Test
        void excludePartialValues() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of(
                    new BigDecimal("1"),
                    new BigDecimal("2"),
                    new BigDecimal("3"),
                    new BigDecimal("6")
            );

            // Act
            final List<StatisticalSummary> output = input
                    .gather(Gatherers4j.movingStatistics(3).excludePartialValues())
                    .toList();

            // Assert
            assertThat(output)
                    .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                    .containsExactly(
                            summary(3, "6", "2", "1", "3", "0.6666666666666667", "0.8164965809277261", "1", "1"),
                            summary(3, "11", "3.666666666666667", "2", "6", "2.888888888888889", "1.699673171197595", "4.333333333333333", "2.081665999466133")
                    );
        }

        @Test
        void ignoresNulls() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of(null, BigDecimal.ONE, null, BigDecimal.TWO);

            // Act
            final List<StatisticalSummary> output = input
                    .gather(Gatherers4j.movingStatistics(2))
                    .toList();

            // Assert
            assertThat(output)
                    .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                    .containsExactly(
                            summary(1, "1", "1", "1", "1", "0", "0", null, null),
                            summary(2, "3", "1.5", "1", "2", "0.25", "0.5", "0.5", "0.7071067811865475")
                    );
        }

        @Test
        void mathContextChange() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of(
                    new BigDecimal("1.234"),
                    new BigDecimal("5.678")
            );

            // Act
            final List<StatisticalSummary> output = input
                    .gather(Gatherers4j.movingStatistics(2).withMathContext(new MathContext(3)))
                    .toList();

            // Assert
            assertThat(output)
                    .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                    .containsExactly(
                            summary(1, "1.23", "1.23", "1.23", "1.23", "0", "0", null, null),
                            summary(2, "6.91", "3.46", "1.23", "5.68", "4.95", "2.22", "9.90", "3.15")
                    );
        }

        @SuppressWarnings("DataFlowIssue")
        @Test
        void mappingFunctionMustNotBeNull() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Gatherers4j.movingStatisticsBy(2, null))
                    .withMessage("Mapping function must not be null");
        }

        @SuppressWarnings("DataFlowIssue")
        @Test
        void mathContextMustNotBeNull() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Gatherers4j.movingStatistics(2).withMathContext(null))
                    .withMessage("MathContext must not be null");
        }

        @Test
        void minAndMaxFollowTheWindow() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of(5, 1, 3, 4, 2, 2, 2).map(BigDecimal::valueOf);

            // Act
            final List<StatisticalSummary> output = input
                    .gather(Gatherers4j.movingStatistics(3))
                    .toList();

            // Assert
            assertThat(extract(output, StatisticalSummary::min))
                    .usingElementComparator(BigDecimal::compareTo)
                    .containsExactly(
                            BigDecimal.valueOf(5),
                            BigDecimal.valueOf(1),
                            BigDecimal.valueOf(1),
                            BigDecimal.valueOf(1),
                            BigDecimal.valueOf(2),
                            BigDecimal.valueOf(2),
                            BigDecimal.valueOf(2)
                    );
            assertThat(extract(output, StatisticalSummary::max))
                    .usingElementComparator(BigDecimal::compareTo)
                    .containsExactly(
                            BigDecimal.valueOf(5),
                            BigDecimal.valueOf(5),
                            BigDecimal.valueOf(5),
                            BigDecimal.valueOf(4),
                            BigDecimal.valueOf(4),
                            BigDecimal.valueOf(4),
                            BigDecimal.valueOf(2)
                    );
        }

        @Test
        void minAndMaxWithDuplicatesInTheWindow() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of(3, 3, 1, 1, 5).map(BigDecimal::valueOf);

            // Act
            final List<StatisticalSummary> output = input
                    .gather(Gatherers4j.movingStatistics(2))
                    .toList();

            // Assert
            assertThat(extract(output, StatisticalSummary::min))
                    .usingElementComparator(BigDecimal::compareTo)
                    .containsExactly(
                            BigDecimal.valueOf(3),
                            BigDecimal.valueOf(3),
                            BigDecimal.valueOf(1),
                            BigDecimal.valueOf(1),
                            BigDecimal.valueOf(1)
                    );
            assertThat(extract(output, StatisticalSummary::max))
                    .usingElementComparator(BigDecimal::compareTo)
                    .containsExactly(
                            BigDecimal.valueOf(3),
                            BigDecimal.valueOf(3),
                            BigDecimal.valueOf(3),
                            BigDecimal.valueOf(1),
                            BigDecimal.valueOf(5)
                    );
        }

        @Test
        void singleElement() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of(new BigDecimal("4.5"));

            // Act
            final List<StatisticalSummary> output = input
                    .gather(Gatherers4j.movingStatistics(3))
                    .toList();

            // Assert
            assertThat(output)
                    .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                    .containsExactly(
                            summary(1, "4.5", "4.5", "4.5", "4.5", "0", "0", null, null)
                    );
        }

        @Test
        void statistics() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of(
                    new BigDecimal("1"),
                    new BigDecimal("2"),
                    new BigDecimal("3"),
                    new BigDecimal("6")
            );

            // Act
            final List<StatisticalSummary> output = input
                    .gather(Gatherers4j.movingStatistics(3))
                    .toList();

            // Assert
            assertThat(output)
                    .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                    .containsExactly(
                            summary(1, "1", "1", "1", "1", "0", "0", null, null),
                            summary(2, "3", "1.5", "1", "2", "0.25", "0.5", "0.5", "0.7071067811865475"),
                            summary(3, "6", "2", "1", "3", "0.6666666666666667", "0.8164965809277261", "1", "1"),
                            summary(3, "11", "3.666666666666667", "2", "6", "2.888888888888889", "1.699673171197595", "4.333333333333333", "2.081665999466133")
                    );
        }

        @Test
        void statisticsBy() {
            // Arrange
            final List<TestValueHolder> input = List.of(
                    new TestValueHolder(1, new BigDecimal("1")),
                    new TestValueHolder(2, new BigDecimal("2")),
                    new TestValueHolder(3, new BigDecimal("3")),
                    new TestValueHolder(4, new BigDecimal("6"))
            );

            // Act
            final List<StatisticalSummary> output = input.stream()
                    .gather(Gatherers4j.movingStatisticsBy(3, TestValueHolder::value))
                    .toList();

            // Assert
            assertThat(output)
                    .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                    .containsExactly(
                            summary(1, "1", "1", "1", "1", "0", "0", null, null),
                            summary(2, "3", "1.5", "1", "2", "0.25", "0.5", "0.5", "0.7071067811865475"),
                            summary(3, "6", "2", "1", "3", "0.6666666666666667", "0.8164965809277261", "1", "1"),
                            summary(3, "11", "3.666666666666667", "2", "6", "2.888888888888889", "1.699673171197595", "4.333333333333333", "2.081665999466133")
                    );
        }

        @Test
        void treatNullAs() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of(null, BigDecimal.ONE);

            // Act
            final List<StatisticalSummary> output = input
                    .gather(Gatherers4j.movingStatistics(2).treatNullAs(BigDecimal.TEN))
                    .toList();

            // Assert
            assertThat(output)
                    .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                    .containsExactly(
                            summary(1, "10", "10", "10", "10", "0", "0", null, null),
                            summary(2, "11", "5.5", "1", "10", "20.25", "4.5", "40.5", "6.363961030678928")
                    );
        }

        @Test
        void treatNullAsOne() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of(null, BigDecimal.ONE);

            // Act
            final List<StatisticalSummary> output = input
                    .gather(Gatherers4j.movingStatistics(2).treatNullAsOne())
                    .toList();

            // Assert
            assertThat(output)
                    .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                    .containsExactly(
                            summary(1, "1", "1", "1", "1", "0", "0", null, null),
                            summary(2, "2", "1", "1", "1", "0", "0", "0", "0")
                    );
        }

        @Test
        void treatNullAsZero() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of(null, BigDecimal.ONE);

            // Act
            final List<StatisticalSummary> output = input
                    .gather(Gatherers4j.movingStatistics(2).treatNullAsZero())
                    .toList();

            // Assert
            assertThat(output)
                    .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                    .containsExactly(
                            summary(1, "0", "0", "0", "0", "0", "0", null, null),
                            summary(2, "1", "0.5", "0", "1", "0.25", "0.5", "0.5", "0.7071067811865475")
                    );
        }

        @Test
        void valuesMatchStandaloneGatherers() {
            // Arrange
            final int windowSize = 5;
            final List<BigDecimal> input = randomValues(42);

            // Act
            final List<StatisticalSummary> output = input.stream()
                    .gather(Gatherers4j.movingStatistics(windowSize))
                    .toList();

            // Assert
            assertThat(extract(output, StatisticalSummary::sum))
                    .usingElementComparator(BigDecimal::compareTo)
                    .containsExactlyElementsOf(input.stream().gather(Gatherers4j.movingSum(windowSize)).toList());
            assertThat(extract(output, StatisticalSummary::mean))
                    .usingElementComparator(BigDecimal::compareTo)
                    .containsExactlyElementsOf(input.stream().gather(Gatherers4j.movingMean(windowSize)).toList());
            assertThat(extract(output, StatisticalSummary::populationVariance))
                    .usingElementComparator(BigDecimal::compareTo)
                    .containsExactlyElementsOf(input.stream().gather(Gatherers4j.movingVariance(Dataset.Population, windowSize)).toList());
            assertThat(extract(output, StatisticalSummary::populationStandardDeviation))
                    .usingElementComparator(BigDecimal::compareTo)
                    .containsExactlyElementsOf(input.stream().gather(Gatherers4j.movingStandardDeviation(Dataset.Population, windowSize)).toList());
            assertThat(extract(output.subList(1, output.size()), StatisticalSummary::sampleVariance))
                    .usingElementComparator(BigDecimal::compareTo)
                    .containsExactlyElementsOf(input.stream().gather(Gatherers4j.movingVariance(Dataset.Sample, windowSize)).toList());
            assertThat(extract(output.subList(1, output.size()), StatisticalSummary::sampleStandardDeviation))
                    .usingElementComparator(BigDecimal::compareTo)
                    .containsExactlyElementsOf(input.stream().gather(Gatherers4j.movingStandardDeviation(Dataset.Sample, windowSize)).toList());

            for (int i = 0; i < input.size(); i++) {
                final List<BigDecimal> window = input.subList(Math.max(0, i - windowSize + 1), i + 1);
                assertThat(output.get(i).count()).isEqualTo(window.size());
                assertThat(output.get(i).min()).isEqualByComparingTo(Collections.min(window));
                assertThat(output.get(i).max()).isEqualByComparingTo(Collections.max(window));
            }
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, 0, 1})
        void windowSizeMustBeGreaterThanOne(final int windowSize) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Gatherers4j.movingStatistics(windowSize))
                    .withMessage("Window size must be greater than 1");
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Gatherers4j.movingStatisticsBy(windowSize, TestValueHolder::value))
                    .withMessage("Window size must be greater than 1");
        }

        @Test
        void withOriginal() {
            // Arrange
            final List<TestValueHolder> input = List.of(
                    new TestValueHolder(1, new BigDecimal("1")),
                    new TestValueHolder(2, new BigDecimal("2"))
            );

            // Act
            final List<WithOriginal<TestValueHolder, StatisticalSummary>> output = input.stream()
                    .gather(Gatherers4j.movingStatisticsBy(2, TestValueHolder::value).withOriginal())
                    .toList();

            // Assert
            assertThat(output)
                    .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                    .containsExactly(
                            new WithOriginal<>(input.get(0), summary(1, "1", "1", "1", "1", "0", "0", null, null)),
                            new WithOriginal<>(input.get(1), summary(2, "3", "1.5", "1", "2", "0.25", "0.5", "0.5", "0.7071067811865475"))
                    );
        }
    }

    @Nested
    class Running {

        @Test
        void emptyStream() {
            // Arrange
            final Stream<BigDecimal> input = Stream.empty();

            // Act
            final List<StatisticalSummary> output = input
                    .gather(Gatherers4j.runningStatistics())
                    .toList();

            // Assert
            assertThat(output).isEmpty();
        }

        @Test
        void ignoresNulls() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of(null, BigDecimal.ONE, null, BigDecimal.TWO);

            // Act
            final List<StatisticalSummary> output = input
                    .gather(Gatherers4j.runningStatistics())
                    .toList();

            // Assert
            assertThat(output)
                    .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                    .containsExactly(
                            summary(1, "1", "1", "1", "1", "0", "0", null, null),
                            summary(2, "3", "1.5", "1", "2", "0.25", "0.5", "0.5", "0.7071067811865475")
                    );
        }

        @SuppressWarnings("DataFlowIssue")
        @Test
        void mappingFunctionMustNotBeNull() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Gatherers4j.runningStatisticsBy(null))
                    .withMessage("Mapping function must not be null");
        }

        @Test
        void mathContextChange() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of(
                    new BigDecimal("1.234"),
                    new BigDecimal("5.678")
            );

            // Act
            final List<StatisticalSummary> output = input
                    .gather(Gatherers4j.runningStatistics().withMathContext(new MathContext(3)))
                    .toList();

            // Assert
            assertThat(output)
                    .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                    .containsExactly(
                            summary(1, "1.23", "1.23", "1.23", "1.23", "0", "0", null, null),
                            summary(2, "6.91", "3.46", "1.23", "5.68", "4.95", "2.22", "9.90", "3.15")
                    );
        }

        @SuppressWarnings("DataFlowIssue")
        @Test
        void mathContextMustNotBeNull() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Gatherers4j.runningStatistics().withMathContext(null))
                    .withMessage("MathContext must not be null");
        }

        @Test
        void minAndMax() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of(3, 5, 1, 4, 1, 6).map(BigDecimal::valueOf);

            // Act
            final List<StatisticalSummary> output = input
                    .gather(Gatherers4j.runningStatistics())
                    .toList();

            // Assert
            assertThat(extract(output, StatisticalSummary::min))
                    .usingElementComparator(BigDecimal::compareTo)
                    .containsExactly(
                            BigDecimal.valueOf(3),
                            BigDecimal.valueOf(3),
                            BigDecimal.valueOf(1),
                            BigDecimal.valueOf(1),
                            BigDecimal.valueOf(1),
                            BigDecimal.valueOf(1)
                    );
            assertThat(extract(output, StatisticalSummary::max))
                    .usingElementComparator(BigDecimal::compareTo)
                    .containsExactly(
                            BigDecimal.valueOf(3),
                            BigDecimal.valueOf(5),
                            BigDecimal.valueOf(5),
                            BigDecimal.valueOf(5),
                            BigDecimal.valueOf(5),
                            BigDecimal.valueOf(6)
                    );
        }

        @Test
        void singleElement() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of(new BigDecimal("4.5"));

            // Act
            final List<StatisticalSummary> output = input
                    .gather(Gatherers4j.runningStatistics())
                    .toList();

            // Assert
            assertThat(output)
                    .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                    .containsExactly(
                            summary(1, "4.5", "4.5", "4.5", "4.5", "0", "0", null, null)
                    );
        }

        @Test
        void statistics() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of(
                    new BigDecimal("1"),
                    new BigDecimal("2"),
                    new BigDecimal("3")
            );

            // Act
            final List<StatisticalSummary> output = input
                    .gather(Gatherers4j.runningStatistics())
                    .toList();

            // Assert
            assertThat(output)
                    .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                    .containsExactly(
                            summary(1, "1", "1", "1", "1", "0", "0", null, null),
                            summary(2, "3", "1.5", "1", "2", "0.25", "0.5", "0.5", "0.7071067811865475"),
                            summary(3, "6", "2", "1", "3", "0.6666666666666667", "0.8164965809277261", "1", "1")
                    );
        }

        @Test
        void statisticsBy() {
            // Arrange
            final List<TestValueHolder> input = List.of(
                    new TestValueHolder(1, new BigDecimal("1")),
                    new TestValueHolder(2, new BigDecimal("2")),
                    new TestValueHolder(3, new BigDecimal("3"))
            );

            // Act
            final List<StatisticalSummary> output = input.stream()
                    .gather(Gatherers4j.runningStatisticsBy(TestValueHolder::value))
                    .toList();

            // Assert
            assertThat(output)
                    .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                    .containsExactly(
                            summary(1, "1", "1", "1", "1", "0", "0", null, null),
                            summary(2, "3", "1.5", "1", "2", "0.25", "0.5", "0.5", "0.7071067811865475"),
                            summary(3, "6", "2", "1", "3", "0.6666666666666667", "0.8164965809277261", "1", "1")
                    );
        }

        @Test
        void treatNullAs() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of(null, BigDecimal.ONE);

            // Act
            final List<StatisticalSummary> output = input
                    .gather(Gatherers4j.runningStatistics().treatNullAs(BigDecimal.TEN))
                    .toList();

            // Assert
            assertThat(output)
                    .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                    .containsExactly(
                            summary(1, "10", "10", "10", "10", "0", "0", null, null),
                            summary(2, "11", "5.5", "1", "10", "20.25", "4.5", "40.5", "6.363961030678928")
                    );
        }

        @Test
        void treatNullAsOne() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of(null, BigDecimal.ONE);

            // Act
            final List<StatisticalSummary> output = input
                    .gather(Gatherers4j.runningStatistics().treatNullAsOne())
                    .toList();

            // Assert
            assertThat(output)
                    .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                    .containsExactly(
                            summary(1, "1", "1", "1", "1", "0", "0", null, null),
                            summary(2, "2", "1", "1", "1", "0", "0", "0", "0")
                    );
        }

        @Test
        void treatNullAsZero() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of(null, BigDecimal.ONE);

            // Act
            final List<StatisticalSummary> output = input
                    .gather(Gatherers4j.runningStatistics().treatNullAsZero())
                    .toList();

            // Assert
            assertThat(output)
                    .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                    .containsExactly(
                            summary(1, "0", "0", "0", "0", "0", "0", null, null),
                            summary(2, "1", "0.5", "0", "1", "0.25", "0.5", "0.5", "0.7071067811865475")
                    );
        }

        @Test
        void valuesMatchStandaloneGatherers() {
            // Arrange
            final List<BigDecimal> input = randomValues(17);

            // Act
            final List<StatisticalSummary> output = input.stream()
                    .gather(Gatherers4j.runningStatistics())
                    .toList();

            // Assert
            assertThat(extract(output, StatisticalSummary::sum))
                    .usingElementComparator(BigDecimal::compareTo)
                    .containsExactlyElementsOf(input.stream().gather(Gatherers4j.runningSum()).toList());
            assertThat(extract(output, StatisticalSummary::mean))
                    .usingElementComparator(BigDecimal::compareTo)
                    .containsExactlyElementsOf(input.stream().gather(Gatherers4j.runningMean()).toList());
            assertThat(extract(output, StatisticalSummary::populationVariance))
                    .usingElementComparator(BigDecimal::compareTo)
                    .containsExactlyElementsOf(input.stream().gather(Gatherers4j.runningVariance(Dataset.Population)).toList());
            assertThat(extract(output, StatisticalSummary::populationStandardDeviation))
                    .usingElementComparator(BigDecimal::compareTo)
                    .containsExactlyElementsOf(input.stream().gather(Gatherers4j.runningStandardDeviation(Dataset.Population)).toList());
            assertThat(extract(output.subList(1, output.size()), StatisticalSummary::sampleVariance))
                    .usingElementComparator(BigDecimal::compareTo)
                    .containsExactlyElementsOf(input.stream().gather(Gatherers4j.runningVariance(Dataset.Sample)).toList());
            assertThat(extract(output.subList(1, output.size()), StatisticalSummary::sampleStandardDeviation))
                    .usingElementComparator(BigDecimal::compareTo)
                    .containsExactlyElementsOf(input.stream().gather(Gatherers4j.runningStandardDeviation(Dataset.Sample)).toList());

            for (int i = 0; i < input.size(); i++) {
                final List<BigDecimal> seen = input.subList(0, i + 1);
                assertThat(output.get(i).count()).isEqualTo(seen.size());
                assertThat(output.get(i).min()).isEqualByComparingTo(Collections.min(seen));
                assertThat(output.get(i).max()).isEqualByComparingTo(Collections.max(seen));
            }
        }

        @Test
        void withOriginal() {
            // Arrange
            final List<TestValueHolder> input = List.of(
                    new TestValueHolder(1, new BigDecimal("1")),
                    new TestValueHolder(2, new BigDecimal("2"))
            );

            // Act
            final List<WithOriginal<TestValueHolder, StatisticalSummary>> output = input.stream()
                    .gather(Gatherers4j.runningStatisticsBy(TestValueHolder::value).withOriginal())
                    .toList();

            // Assert
            assertThat(output)
                    .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                    .containsExactly(
                            new WithOriginal<>(input.get(0), summary(1, "1", "1", "1", "1", "0", "0", null, null)),
                            new WithOriginal<>(input.get(1), summary(2, "3", "1.5", "1", "2", "0.25", "0.5", "0.5", "0.7071067811865475"))
                    );
        }
    }
}
