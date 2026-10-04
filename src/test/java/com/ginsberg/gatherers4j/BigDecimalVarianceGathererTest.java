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
import com.ginsberg.gatherers4j.enums.Dataset;
import com.ginsberg.gatherers4j.util.TestValueHolder;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.stream.Stream;

import static com.ginsberg.gatherers4j.util.TestUtils.BIG_DECIMAL_RECURSIVE_COMPARISON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class BigDecimalVarianceGathererTest {

    @Nested
    class Moving {
        @Nested
        class Common {
            @SuppressWarnings("DataFlowIssue")
            @Test
            void datasetCannotBeNull() {
                assertThatIllegalArgumentException().isThrownBy(() ->
                        Gatherers4j.movingVariance(null, 2)
                );
                assertThatIllegalArgumentException().isThrownBy(() ->
                        Gatherers4j.movingVarianceBy(null, 2, TestValueHolder::value)
                );
            }

            @SuppressWarnings("DataFlowIssue")
            @Test
            void mathContextCannotBeNull() {
                assertThatIllegalArgumentException().isThrownBy(() ->
                        Gatherers4j.movingVariance(Dataset.Population, 2).withMathContext(null)
                );
            }

            @Test
            void windowSizeMustBeGreaterThanOne() {
                assertThatIllegalArgumentException().isThrownBy(() ->
                        Gatherers4j.movingVariance(Dataset.Population, 1)
                );
                assertThatIllegalArgumentException().isThrownBy(() ->
                        Gatherers4j.movingVarianceBy(Dataset.Population, 1, TestValueHolder::value)
                );
                assertThatIllegalArgumentException().isThrownBy(() ->
                        Gatherers4j.movingVariance(Dataset.Sample, 1)
                );
                assertThatIllegalArgumentException().isThrownBy(() ->
                        Gatherers4j.movingVarianceBy(Dataset.Sample, 1, TestValueHolder::value)
                );
            }
        }

        @Nested
        class Population {

            @Test
            void excludePartialValues() {
                // Arrange
                final Stream<BigDecimal> input = Stream.of(
                        new BigDecimal("1.0"),
                        new BigDecimal("2.0"),
                        new BigDecimal("10.0"),
                        new BigDecimal("20.0"),
                        new BigDecimal("30.0")
                );

                // Act
                final List<BigDecimal> output = input
                        .gather(Gatherers4j.movingVariance(Dataset.Population, 3).excludePartialValues())
                        .toList();

                // Assert
                assertThat(output)
                        .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                        .containsExactly(
                                new BigDecimal("16.22222222222222"),
                                new BigDecimal("54.22222222222222"),
                                new BigDecimal("66.66666666666667")
                        );
            }

            @Test
            void ignoresNulls() {
                // Arrange
                final Stream<BigDecimal> input = Stream.of(null, BigDecimal.ONE, BigDecimal.TWO);

                // Act
                final List<BigDecimal> output = input
                        .gather(Gatherers4j.movingVariance(Dataset.Population, 2))
                        .toList();

                // Assert
                assertThat(output)
                        .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                        .containsExactly(
                                BigDecimal.ZERO,
                                new BigDecimal(".25")
                        );
            }

            @Test
            void mathContextChange() {
                // Arrange
                final Stream<BigDecimal> input = Stream.of(
                        new BigDecimal("1.0"),
                        new BigDecimal("2.0"),
                        new BigDecimal("10.0"),
                        new BigDecimal("20.0")
                );

                // Act
                final List<BigDecimal> output = input
                        .gather(Gatherers4j.movingVariance(Dataset.Population, 2).withMathContext(new MathContext(3)))
                        .toList();

                // Assert
                assertThat(output)
                        .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                        .containsExactly(
                                BigDecimal.ZERO,
                                new BigDecimal(".25"),
                                new BigDecimal("16.0"),
                                new BigDecimal("25.0")
                        );
            }

            @Test
            void variance() {
                // Arrange
                final Stream<BigDecimal> input = Stream.of(
                        new BigDecimal("1.0"),
                        new BigDecimal("2.0"),
                        new BigDecimal("10.0"),
                        new BigDecimal("20.0"),
                        new BigDecimal("30.0")
                );

                // Act
                final List<BigDecimal> output = input
                        .gather(Gatherers4j.movingVariance(Dataset.Population, 3))
                        .toList();

                // Assert
                assertThat(output)
                        .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                        .containsExactly(
                                BigDecimal.ZERO,
                                new BigDecimal(".25"),
                                new BigDecimal("16.22222222222222"),
                                new BigDecimal("54.22222222222222"),
                                new BigDecimal("66.66666666666667")
                        );
            }

            @Test
            void varianceBy() {
                // Arrange
                final List<TestValueHolder> input = List.of(
                        new TestValueHolder(1, new BigDecimal("1.0")),
                        new TestValueHolder(2, new BigDecimal("2.0")),
                        new TestValueHolder(3, new BigDecimal("10.0")),
                        new TestValueHolder(4, new BigDecimal("20.0")),
                        new TestValueHolder(5, new BigDecimal("30.0"))
                );

                // Act
                final List<BigDecimal> output = input.stream()
                        .gather(Gatherers4j.movingVarianceBy(Dataset.Population, 3, TestValueHolder::value))
                        .toList();

                // Assert
                assertThat(output)
                        .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                        .containsExactly(
                                BigDecimal.ZERO,
                                new BigDecimal(".25"),
                                new BigDecimal("16.22222222222222"),
                                new BigDecimal("54.22222222222222"),
                                new BigDecimal("66.66666666666667")
                        );
            }

            @Test
            void treatNullAsNonZero() {
                // Arrange
                final Stream<BigDecimal> input = Stream.of(null, BigDecimal.ONE, null, BigDecimal.ONE);

                // Act
                final List<BigDecimal> output = input
                        .gather(Gatherers4j.movingVariance(Dataset.Population, 2).treatNullAs(BigDecimal.TEN))
                        .toList();

                // Assert
                assertThat(output)
                        .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                        .containsExactly(
                                BigDecimal.ZERO,
                                new BigDecimal("20.25"),
                                new BigDecimal("20.25"),
                                new BigDecimal("20.25")
                        );
            }

            @Test
            void treatNullAsZero() {
                // Arrange
                final Stream<BigDecimal> input = Stream.of(null, BigDecimal.ONE, null, BigDecimal.ONE);

                // Act
                final List<BigDecimal> output = input
                        .gather(Gatherers4j.movingVariance(Dataset.Population, 2).treatNullAsZero())
                        .toList();

                // Assert
                assertThat(output)
                        .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                        .containsExactly(
                                BigDecimal.ZERO,
                                new BigDecimal("0.25"),
                                new BigDecimal("0.25"),
                                new BigDecimal("0.25")
                        );
            }

            @Test
            void withOriginalBigDecimal() {
                // Arrange
                final Stream<BigDecimal> input = Stream.of(
                        new BigDecimal("1.0"),
                        new BigDecimal("2.0"),
                        new BigDecimal("10.0"),
                        new BigDecimal("20.0"),
                        new BigDecimal("30.0")
                );

                // Act
                final List<WithOriginal<BigDecimal, BigDecimal>> output = input
                        .gather(Gatherers4j.movingVariance(Dataset.Population, 3).withOriginal())
                        .toList();

                // Assert
                assertThat(output)
                        .map(WithOriginal::calculated)
                        .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                        .containsExactly(
                                BigDecimal.ZERO,
                                new BigDecimal(".25"),
                                new BigDecimal("16.22222222222222"),
                                new BigDecimal("54.22222222222222"),
                                new BigDecimal("66.66666666666667")
                        );

                assertThat(output)
                        .map(WithOriginal::original)
                        .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                        .containsExactly(
                                new BigDecimal("1.0"),
                                new BigDecimal("2.0"),
                                new BigDecimal("10.0"),
                                new BigDecimal("20.0"),
                                new BigDecimal("30.0")
                        );
            }

            @Test
            void withOriginalRecordByMappedField() {
                // Arrange
                final List<TestValueHolder> input = List.of(
                        new TestValueHolder(1, new BigDecimal("1.0")),
                        new TestValueHolder(2, new BigDecimal("2.0")),
                        new TestValueHolder(3, new BigDecimal("10.0")),
                        new TestValueHolder(4, new BigDecimal("20.0")),
                        new TestValueHolder(5, new BigDecimal("30.0"))
                );

                // Act
                final List<WithOriginal<TestValueHolder, BigDecimal>> output = input.stream()
                        .gather(Gatherers4j.movingVarianceBy(Dataset.Population, 3, TestValueHolder::value).withOriginal())
                        .toList();

                // Assert
                assertThat(output)
                        .extracting(WithOriginal::calculated)
                        .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                        .containsExactly(
                                BigDecimal.ZERO,
                                new BigDecimal(".25"),
                                new BigDecimal("16.22222222222222"),
                                new BigDecimal("54.22222222222222"),
                                new BigDecimal("66.66666666666667")
                        );

                assertThat(output)
                        .map(WithOriginal::original)
                        .containsExactlyInAnyOrderElementsOf(input);
            }
        }

        @Nested
        class Sample {

            @Test
            void excludePartialValues() {
                // Arrange
                final Stream<BigDecimal> input = Stream.of(
                        new BigDecimal("1.0"),
                        new BigDecimal("2.0"),
                        new BigDecimal("10.0"),
                        new BigDecimal("20.0"),
                        new BigDecimal("30.0")
                );

                // Act
                final List<BigDecimal> output = input
                        .gather(Gatherers4j.movingVariance(Dataset.Sample, 3).excludePartialValues())
                        .toList();

                // Assert
                assertThat(output)
                        .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                        .containsExactly(
                                new BigDecimal("24.33333333333333"),
                                new BigDecimal("81.33333333333333"),
                                new BigDecimal("100.00")
                        );
            }

            @Test
            void ignoresNulls() {
                // Arrange
                final Stream<BigDecimal> input = Stream.of(null, BigDecimal.ONE, BigDecimal.TWO);

                // Act
                final List<BigDecimal> output = input
                        .gather(Gatherers4j.movingVariance(Dataset.Sample, 2))
                        .toList();

                // Assert
                assertThat(output)
                        .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                        .containsExactly(
                                new BigDecimal("0.5")
                        );
            }

            @Test
            void mathContextChange() {
                // Arrange
                final Stream<BigDecimal> input = Stream.of(
                        new BigDecimal("1.0"),
                        new BigDecimal("2.0"),
                        new BigDecimal("10.0"),
                        new BigDecimal("20.0")
                );

                // Act
                final List<BigDecimal> output = input
                        .gather(Gatherers4j.movingVariance(Dataset.Sample, 2).withMathContext(new MathContext(3)))
                        .toList();

                // Assert
                assertThat(output)
                        .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                        .containsExactly(
                                new BigDecimal("0.50"),
                                new BigDecimal("32.0"),
                                new BigDecimal("50.0")
                        );
            }

            @Test
            void variance() {
                // Arrange
                final Stream<BigDecimal> input = Stream.of(
                        new BigDecimal("1.0"),
                        new BigDecimal("2.0"),
                        new BigDecimal("10.0"),
                        new BigDecimal("20.0"),
                        new BigDecimal("30.0")
                );

                // Act
                final List<BigDecimal> output = input
                        .gather(Gatherers4j.movingVariance(Dataset.Sample, 3))
                        .toList();

                // Assert
                assertThat(output)
                        .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                        .containsExactly(
                                new BigDecimal("0.5"),
                                new BigDecimal("24.33333333333333"),
                                new BigDecimal("81.33333333333333"),
                                new BigDecimal("100.00")
                        );
            }

            @Test
            void varianceBy() {
                // Arrange
                final List<TestValueHolder> input = List.of(
                        new TestValueHolder(1, new BigDecimal("1.0")),
                        new TestValueHolder(2, new BigDecimal("2.0")),
                        new TestValueHolder(3, new BigDecimal("10.0")),
                        new TestValueHolder(4, new BigDecimal("20.0")),
                        new TestValueHolder(5, new BigDecimal("30.0"))
                );

                // Act
                final List<BigDecimal> output = input.stream()
                        .gather(Gatherers4j.movingVarianceBy(Dataset.Sample, 3, TestValueHolder::value))
                        .toList();

                // Assert
                assertThat(output)
                        .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                        .containsExactly(
                                new BigDecimal("0.5"),
                                new BigDecimal("24.33333333333333"),
                                new BigDecimal("81.33333333333333"),
                                new BigDecimal("100.00")
                        );
            }

            @Test
            void treatNullAsNonZero() {
                // Arrange
                final Stream<BigDecimal> input = Stream.of(null, BigDecimal.ONE, null, BigDecimal.ONE);

                // Act
                final List<BigDecimal> output = input
                        .gather(Gatherers4j.movingVariance(Dataset.Sample, 2).treatNullAs(BigDecimal.TEN))
                        .toList();

                // Assert
                assertThat(output)
                        .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                        .containsExactly(
                                new BigDecimal("40.5"),
                                new BigDecimal("40.5"),
                                new BigDecimal("40.5")
                        );
            }

            @Test
            void treatNullAsZero() {
                // Arrange
                final Stream<BigDecimal> input = Stream.of(null, BigDecimal.ONE, null, BigDecimal.ONE);

                // Act
                final List<BigDecimal> output = input
                        .gather(Gatherers4j.movingVariance(Dataset.Sample, 2).treatNullAsZero())
                        .toList();

                // Assert
                assertThat(output)
                        .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                        .containsExactly(
                                new BigDecimal("0.5"),
                                new BigDecimal("0.5"),
                                new BigDecimal("0.5")
                        );
            }

            @Test
            void withOriginalBigDecimal() {
                // Arrange
                final Stream<BigDecimal> input = Stream.of(
                        new BigDecimal("1.0"),
                        new BigDecimal("2.0"),
                        new BigDecimal("10.0"),
                        new BigDecimal("20.0"),
                        new BigDecimal("30.0")
                );

                // Act
                final List<WithOriginal<BigDecimal, BigDecimal>> output = input
                        .gather(Gatherers4j.movingVariance(Dataset.Sample, 3).withOriginal())
                        .toList();

                // Assert
                assertThat(output)
                        .map(WithOriginal::calculated)
                        .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                        .containsExactly(
                                new BigDecimal("0.5"),
                                new BigDecimal("24.33333333333333"),
                                new BigDecimal("81.33333333333333"),
                                new BigDecimal("100.00")
                        );

                assertThat(output)
                        .map(WithOriginal::original)
                        .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                        .containsExactly(
                                new BigDecimal("2.0"),
                                new BigDecimal("10.0"),
                                new BigDecimal("20.0"),
                                new BigDecimal("30.0")
                        );
            }

            @Test
            void withOriginalRecordByMappedField() {
                // Arrange
                final List<TestValueHolder> input = List.of(
                        new TestValueHolder(1, new BigDecimal("1.0")),
                        new TestValueHolder(2, new BigDecimal("2.0")),
                        new TestValueHolder(3, new BigDecimal("10.0")),
                        new TestValueHolder(4, new BigDecimal("20.0")),
                        new TestValueHolder(5, new BigDecimal("30.0"))
                );

                // Act
                final List<WithOriginal<TestValueHolder, BigDecimal>> output = input.stream()
                        .gather(Gatherers4j.movingVarianceBy(Dataset.Sample, 3, TestValueHolder::value).withOriginal())
                        .toList();

                // Assert
                assertThat(output)
                        .extracting(WithOriginal::calculated)
                        .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                        .containsExactly(
                                new BigDecimal("0.5"),
                                new BigDecimal("24.33333333333333"),
                                new BigDecimal("81.33333333333333"),
                                new BigDecimal("100.00")
                        );

                assertThat(output)
                        .map(WithOriginal::original)
                        .containsExactlyElementsOf(input.subList(1, input.size()));
            }
        }
    }

    @Nested
    class Running {
        @Nested
        class Common {
            @SuppressWarnings("DataFlowIssue")
            @Test
            void datasetCannotBeNull() {
                assertThatIllegalArgumentException().isThrownBy(() ->
                        Gatherers4j.runningVariance(null)
                );
                assertThatIllegalArgumentException().isThrownBy(() ->
                        Gatherers4j.runningVarianceBy(null, TestValueHolder::value)
                );
            }

            @SuppressWarnings("DataFlowIssue")
            @Test
            void mathContextCannotBeNull() {
                assertThatIllegalArgumentException().isThrownBy(() ->
                        Gatherers4j.runningVariance(Dataset.Population).withMathContext(null)
                );
            }
        }

        @Nested
        class Population {

            @Test
            void ignoresNulls() {
                // Arrange
                final Stream<BigDecimal> input = Stream.of(null, BigDecimal.ONE, BigDecimal.TWO);

                // Act
                final List<BigDecimal> output = input
                        .gather(Gatherers4j.runningVariance(Dataset.Population))
                        .toList();

                // Assert
                assertThat(output)
                        .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                        .containsExactly(
                                BigDecimal.ZERO,
                                new BigDecimal(".25")
                        );
            }

            @Test
            void mathContextChange() {
                // Arrange
                final Stream<BigDecimal> input = Stream.of(
                        new BigDecimal("1.0"),
                        new BigDecimal("2.0"),
                        new BigDecimal("10.0")
                );

                // Act
                final List<BigDecimal> output = input
                        .gather(Gatherers4j.runningVariance(Dataset.Population).withMathContext(new MathContext(3)))
                        .toList();

                // Assert
                assertThat(output)
                        .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                        .containsExactly(
                                BigDecimal.ZERO,
                                new BigDecimal(".25"),
                                new BigDecimal("16.2")
                        );
            }

            @Test
            void variance() {
                // Arrange
                final Stream<BigDecimal> input = Stream.of(
                        new BigDecimal("1.0"),
                        new BigDecimal("2.0"),
                        new BigDecimal("10.0")
                );

                // Act
                final List<BigDecimal> output = input
                        .gather(Gatherers4j.runningVariance(Dataset.Population))
                        .toList();

                // Assert
                assertThat(output)
                        .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                        .containsExactly(
                                BigDecimal.ZERO,
                                new BigDecimal(".25"),
                                new BigDecimal("16.22222222222222")
                        );
            }

            @Test
            void varianceBy() {
                // Arrange
                final List<TestValueHolder> input = List.of(
                        new TestValueHolder(1, new BigDecimal("1.0")),
                        new TestValueHolder(2, new BigDecimal("2.0")),
                        new TestValueHolder(3, new BigDecimal("10.0")),
                        new TestValueHolder(4, new BigDecimal("20.0")),
                        new TestValueHolder(5, new BigDecimal("30.0"))
                );

                // Act
                final List<BigDecimal> output = input.stream()
                        .gather(Gatherers4j.runningVarianceBy(Dataset.Population, TestValueHolder::value))
                        .toList();

                // Assert
                assertThat(output)
                        .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                        .containsExactly(
                                BigDecimal.ZERO,
                                new BigDecimal(".25"),
                                new BigDecimal("16.22222222222222"),
                                new BigDecimal("58.1875"),
                                new BigDecimal("122.24")
                        );
            }

            @Test
            void treatNullAsNonZero() {
                // Arrange
                final Stream<BigDecimal> input = Stream.of(null, BigDecimal.ONE, null, BigDecimal.ONE);

                // Act
                final List<BigDecimal> output = input
                        .gather(Gatherers4j.runningVariance(Dataset.Population).treatNullAs(BigDecimal.TEN))
                        .toList();

                // Assert
                assertThat(output)
                        .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                        .containsExactly(
                                BigDecimal.ZERO,
                                new BigDecimal("20.25"),
                                new BigDecimal("18"),
                                new BigDecimal("20.25")
                        );
            }

            @Test
            void treatNullAsZero() {
                // Arrange
                final Stream<BigDecimal> input = Stream.of(null, BigDecimal.ONE, null, BigDecimal.ONE);

                // Act
                final List<BigDecimal> output = input
                        .gather(Gatherers4j.runningVariance(Dataset.Population).treatNullAsZero())
                        .toList();

                // Assert
                assertThat(output)
                        .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                        .containsExactly(
                                BigDecimal.ZERO,
                                new BigDecimal("0.25"),
                                new BigDecimal("0.2222222222222222"),
                                new BigDecimal("0.25")
                        );
            }

            @Test
            void withOriginalBigDecimal() {
                // Arrange
                final Stream<BigDecimal> input = Stream.of(
                        new BigDecimal("1.0"),
                        new BigDecimal("2.0"),
                        new BigDecimal("10.0"),
                        new BigDecimal("20.0"),
                        new BigDecimal("30.0")
                );

                // Act
                final List<WithOriginal<BigDecimal, BigDecimal>> output = input
                        .gather(Gatherers4j.runningVariance(Dataset.Population).withOriginal())
                        .toList();

                // Assert
                assertThat(output)
                        .map(WithOriginal::calculated)
                        .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                        .contains(
                                BigDecimal.ZERO,
                                new BigDecimal(".25"),
                                new BigDecimal("16.22222222222222"),
                                new BigDecimal("58.1875"),
                                new BigDecimal("122.24")
                        );

                assertThat(output)
                        .map(WithOriginal::original)
                        .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                        .containsExactly(
                                new BigDecimal("1.0"),
                                new BigDecimal("2.0"),
                                new BigDecimal("10.0"),
                                new BigDecimal("20.0"),
                                new BigDecimal("30.0")
                        );
            }

            @Test
            void withOriginalRecordByMappedField() {
                // Arrange
                final List<TestValueHolder> input = List.of(
                        new TestValueHolder(1, new BigDecimal("1.0")),
                        new TestValueHolder(2, new BigDecimal("2.0")),
                        new TestValueHolder(3, new BigDecimal("10.0")),
                        new TestValueHolder(4, new BigDecimal("20.0")),
                        new TestValueHolder(5, new BigDecimal("30.0"))
                );

                // Act
                final List<WithOriginal<TestValueHolder, BigDecimal>> output = input.stream()
                        .gather(Gatherers4j.runningVarianceBy(Dataset.Population, TestValueHolder::value).withOriginal())
                        .toList();

                // Assert
                assertThat(output)
                        .extracting(WithOriginal::calculated)
                        .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                        .containsExactly(
                                BigDecimal.ZERO,
                                new BigDecimal(".25"),
                                new BigDecimal("16.22222222222222"),
                                new BigDecimal("58.1875"),
                                new BigDecimal("122.24")
                        );

                assertThat(output)
                        .map(WithOriginal::original)
                        .containsExactlyInAnyOrderElementsOf(input);
            }
        }

        @Nested
        class Sample {

            @Test
            void ignoresNulls() {
                // Arrange
                final Stream<BigDecimal> input = Stream.of(null, BigDecimal.ONE, BigDecimal.TWO);

                // Act
                final List<BigDecimal> output = input
                        .gather(Gatherers4j.runningVariance(Dataset.Sample))
                        .toList();

                // Assert
                assertThat(output)
                        .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                        .containsExactly(
                                new BigDecimal("0.5")
                        );
            }

            @Test
            void mathContextChange() {
                // Arrange
                final Stream<BigDecimal> input = Stream.of(
                        new BigDecimal("1.0"),
                        new BigDecimal("2.0"),
                        new BigDecimal("10.0")
                );

                // Act
                final List<BigDecimal> output = input
                        .gather(Gatherers4j.runningVariance(Dataset.Sample).withMathContext(new MathContext(3)))
                        .toList();

                // Assert
                assertThat(output)
                        .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                        .containsExactly(
                                new BigDecimal("0.50"),
                                new BigDecimal("24.3")
                        );
            }

            @Test
            void variance() {
                // Arrange
                final Stream<BigDecimal> input = Stream.of(
                        new BigDecimal("1.0"),
                        new BigDecimal("2.0"),
                        new BigDecimal("10.0")
                );

                // Act
                final List<BigDecimal> output = input
                        .gather(Gatherers4j.runningVariance(Dataset.Sample))
                        .toList();

                // Assert
                assertThat(output)
                        .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                        .containsExactly(
                                new BigDecimal("0.5"),
                                new BigDecimal("24.33333333333333")
                        );
            }

            @Test
            void varianceBy() {
                // Arrange
                final List<TestValueHolder> input = List.of(
                        new TestValueHolder(1, new BigDecimal("1.0")),
                        new TestValueHolder(2, new BigDecimal("2.0")),
                        new TestValueHolder(3, new BigDecimal("10.0")),
                        new TestValueHolder(4, new BigDecimal("20.0")),
                        new TestValueHolder(5, new BigDecimal("30.0"))
                );

                // Act
                final List<BigDecimal> output = input.stream()
                        .gather(Gatherers4j.runningVarianceBy(Dataset.Sample, TestValueHolder::value))
                        .toList();

                // Assert
                assertThat(output)
                        .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                        .containsExactly(
                                new BigDecimal("0.5"),
                                new BigDecimal("24.33333333333333"),
                                new BigDecimal("77.58333333333333"),
                                new BigDecimal("152.80")
                        );
            }

            @Test
            void treatNullAsNonZero() {
                // Arrange
                final Stream<BigDecimal> input = Stream.of(null, BigDecimal.ONE, null, BigDecimal.ONE);

                // Act
                final List<BigDecimal> output = input
                        .gather(Gatherers4j.runningVariance(Dataset.Sample).treatNullAs(BigDecimal.TEN))
                        .toList();

                // Assert
                assertThat(output)
                        .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                        .containsExactly(
                                new BigDecimal("40.5"),
                                new BigDecimal("27"),
                                new BigDecimal("27")
                        );
            }

            @Test
            void treatNullAsZero() {
                // Arrange
                final Stream<BigDecimal> input = Stream.of(null, BigDecimal.ONE, null, BigDecimal.ONE);

                // Act
                final List<BigDecimal> output = input
                        .gather(Gatherers4j.runningVariance(Dataset.Sample).treatNullAsZero())
                        .toList();

                // Assert
                assertThat(output)
                        .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                        .containsExactly(
                                new BigDecimal("0.5"),
                                new BigDecimal("0.3333333333333333"),
                                new BigDecimal("0.3333333333333333")
                        );
            }

            @Test
            void withOriginalBigDecimal() {
                // Arrange
                final Stream<BigDecimal> input = Stream.of(
                        new BigDecimal("1.0"),
                        new BigDecimal("2.0"),
                        new BigDecimal("10.0"),
                        new BigDecimal("20.0"),
                        new BigDecimal("30.0")
                );

                // Act
                final List<WithOriginal<BigDecimal, BigDecimal>> output = input
                        .gather(Gatherers4j.runningVariance(Dataset.Sample).withOriginal())
                        .toList();

                // Assert
                assertThat(output)
                        .map(WithOriginal::calculated)
                        .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                        .containsExactly(
                                new BigDecimal("0.5"),
                                new BigDecimal("24.33333333333333"),
                                new BigDecimal("77.58333333333333"),
                                new BigDecimal("152.80")
                        );

                assertThat(output)
                        .map(WithOriginal::original)
                        .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                        .containsExactly(
                                new BigDecimal("2.0"),
                                new BigDecimal("10.0"),
                                new BigDecimal("20.0"),
                                new BigDecimal("30.0")
                        );
            }

            @Test
            void withOriginalRecordByMappedField() {
                // Arrange
                final List<TestValueHolder> input = List.of(
                        new TestValueHolder(1, new BigDecimal("1.0")),
                        new TestValueHolder(2, new BigDecimal("2.0")),
                        new TestValueHolder(3, new BigDecimal("10.0")),
                        new TestValueHolder(4, new BigDecimal("20.0")),
                        new TestValueHolder(5, new BigDecimal("30.0"))
                );

                // Act
                final List<WithOriginal<TestValueHolder, BigDecimal>> output = input.stream()
                        .gather(Gatherers4j.runningVarianceBy(Dataset.Sample, TestValueHolder::value).withOriginal())
                        .toList();

                // Assert
                assertThat(output)
                        .extracting(WithOriginal::calculated)
                        .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                        .containsExactly(
                                new BigDecimal("0.5"),
                                new BigDecimal("24.33333333333333"),
                                new BigDecimal("77.58333333333333"),
                                new BigDecimal("152.80")
                        );

                assertThat(output)
                        .map(WithOriginal::original)
                        .containsExactlyElementsOf(input.subList(1, input.size()));
            }
        }
    }

    /// Inputs whose squares need more digits than `DECIMAL64` provides, with a spread that differs only in the
    /// last place. Rounding the squares to the `MathContext` produced negative variances for these.
    @Nested
    class Precision {

        @Test
        void movingPopulationNearLimitOfPrecision() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of("99999999.99999999", "99999999.99999998", "99999999.99999999", "99999999.99999997")
                    .map(BigDecimal::new);

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.movingVariance(Dataset.Population, 2))
                    .toList();

            // Assert
            assertThat(output)
                    .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                    .containsExactly(BigDecimal.ZERO, new BigDecimal("2.5E-17"), new BigDecimal("2.5E-17"), new BigDecimal("1E-16"));
        }

        @Test
        void movingSampleNearLimitOfPrecision() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of("99999999.99999999", "99999999.99999998", "99999999.99999999", "99999999.99999997")
                    .map(BigDecimal::new);

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.movingVariance(Dataset.Sample, 2))
                    .toList();

            // Assert
            assertThat(output)
                    .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                    .containsExactly(new BigDecimal("5E-17"), new BigDecimal("5E-17"), new BigDecimal("2E-16"));
        }

        @Test
        void runningPopulationNearLimitOfPrecision() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of("1.000000000000001", "1.000000000000002", "1.000000000000003")
                    .map(BigDecimal::new);

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.runningVariance(Dataset.Population))
                    .toList();

            // Assert
            assertThat(output)
                    .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                    .containsExactly(BigDecimal.ZERO, new BigDecimal("2.5E-31"), new BigDecimal("6.666666666666667E-31"));
        }

        @Test
        void runningSampleNearLimitOfPrecision() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of("1.000000000000001", "1.000000000000002", "1.000000000000003")
                    .map(BigDecimal::new);

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.runningVariance(Dataset.Sample))
                    .toList();

            // Assert
            assertThat(output)
                    .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                    .containsExactly(new BigDecimal("5E-31"), new BigDecimal("1E-30"));
        }

        @Test
        void runningNeverNegativeWithLargeOffset() {
            // Arrange
            final List<BigDecimal> input = Stream.of("12345678.12345678", "12345678.12345679", "12345678.12345677")
                    .map(BigDecimal::new)
                    .toList();

            // Act
            final List<BigDecimal> population = input.stream()
                    .gather(Gatherers4j.runningVariance(Dataset.Population))
                    .toList();
            final List<BigDecimal> sample = input.stream()
                    .gather(Gatherers4j.runningVariance(Dataset.Sample))
                    .toList();

            // Assert
            assertThat(population).hasSize(3).allMatch(it -> it.signum() >= 0);
            assertThat(sample).hasSize(2).allMatch(it -> it.signum() >= 0);
        }
    }

    /// Sample variance divides by n - 1, so it is undefined for a single element.
    @Nested
    class SingleElement {

        @Test
        void movingPopulationEmitsZero() {
            assertThat(Stream.of(BigDecimal.TEN).gather(Gatherers4j.movingVariance(Dataset.Population, 2)).toList())
                    .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                    .containsExactly(BigDecimal.ZERO);
        }

        @Test
        void movingSampleEmitsNothing() {
            assertThat(Stream.of(BigDecimal.TEN).gather(Gatherers4j.movingVariance(Dataset.Sample, 2)).toList())
                    .isEmpty();
        }

        @Test
        void runningPopulationEmitsZero() {
            assertThat(Stream.of(BigDecimal.TEN).gather(Gatherers4j.runningVariance(Dataset.Population)).toList())
                    .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                    .containsExactly(BigDecimal.ZERO);
        }

        @Test
        void runningSampleEmitsNothing() {
            assertThat(Stream.of(BigDecimal.TEN).gather(Gatherers4j.runningVariance(Dataset.Sample)).toList())
                    .isEmpty();
        }
    }
}
