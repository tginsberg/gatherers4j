/*
 * Copyright 2024 Todd Ginsberg
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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.stream.Stream;

import static com.ginsberg.gatherers4j.TestUtils.BIG_DECIMAL_RECURSIVE_COMPARISON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BigDecimalStandardDeviationGathererTest {

    @Nested
    class Common {
        @Test
        void mathContextCannotBeNull() {
            assertThatThrownBy(() ->
                    Stream.of(BigDecimal.ONE).gather(Gatherers4j.runningPopulationStandardDeviation().withMathContext(null))
            ).isExactlyInstanceOf(IllegalArgumentException.class);
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
                    .gather(Gatherers4j.runningPopulationStandardDeviation())
                    .toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            BigDecimal.ZERO,
                            new BigDecimal(".5")
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
                    .gather(Gatherers4j.runningPopulationStandardDeviation().withMathContext(new MathContext(3)))
                    .toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            BigDecimal.ZERO,
                            new BigDecimal(".5"),
                            new BigDecimal("4.02")
                    );
        }

        @Test
        void standardDeviation() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of(
                    new BigDecimal("1.0"),
                    new BigDecimal("2.0"),
                    new BigDecimal("10.0")
            );

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.runningPopulationStandardDeviation())
                    .toList();

            // Assert
            assertThat(output)
                    .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                    .containsExactly(
                            BigDecimal.ZERO,
                            new BigDecimal(".5"),
                            new BigDecimal("4.02768199119819")
                    );
        }

        @Test
        void standardDeviationBy() {
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
                    .gather(Gatherers4j.runningPopulationStandardDeviationBy(TestValueHolder::value))
                    .toList();

            // Assert
            assertThat(output)
                    .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                    .containsExactly(
                            BigDecimal.ZERO,
                            new BigDecimal(".5"),
                            new BigDecimal("4.02768199119819"),
                            new BigDecimal("7.628073151196179"),
                            new BigDecimal("11.0562199688682")
                    );
        }

        @Test
        void treatNullAsNonZero() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of(null, BigDecimal.ONE, null, BigDecimal.ONE);

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.runningPopulationStandardDeviation().treatNullAs(BigDecimal.TEN))
                    .toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            BigDecimal.ZERO,
                            new BigDecimal("4.5"),
                            new BigDecimal("4.242640687119285"),
                            new BigDecimal("4.5")
                    );
        }

        @Test
        void treatNullAsZero() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of(null, BigDecimal.ONE, null, BigDecimal.ONE);

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.runningPopulationStandardDeviation().treatNullAsZero())
                    .toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            BigDecimal.ZERO,
                            new BigDecimal("0.5"),
                            new BigDecimal("0.4714045207910317"),
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
                    .gather(Gatherers4j.runningPopulationStandardDeviation().withOriginal())
                    .toList();

            // Assert
            assertThat(output)
                    .map(WithOriginal::calculated)
                    .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                    .contains(
                            BigDecimal.ZERO,
                            new BigDecimal(".5"),
                            new BigDecimal("4.02768199119819"),
                            new BigDecimal("7.628073151196179"),
                            new BigDecimal("11.0562199688682")
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
                    .gather(Gatherers4j.runningPopulationStandardDeviationBy(TestValueHolder::value).withOriginal())
                    .toList();

            // Assert
            assertThat(output)
                    .extracting(WithOriginal::calculated)
                    .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                    .containsExactly(
                            BigDecimal.ZERO,
                            new BigDecimal(".5"),
                            new BigDecimal("4.02768199119819"),
                            new BigDecimal("7.628073151196179"),
                            new BigDecimal("11.0562199688682")
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
                    .gather(Gatherers4j.runningSampleStandardDeviation())
                    .toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            BigDecimal.ZERO,
                            new BigDecimal("0.7071067811865475")
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
                    .gather(Gatherers4j.runningSampleStandardDeviation().withMathContext(new MathContext(3)))
                    .toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            BigDecimal.ZERO,
                            new BigDecimal("0.707"),
                            new BigDecimal("4.93")
                    );
        }

        @Test
        void standardDeviation() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of(
                    new BigDecimal("1.0"),
                    new BigDecimal("2.0"),
                    new BigDecimal("10.0")
            );

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.runningSampleStandardDeviation())
                    .toList();

            // Assert
            assertThat(output)
                    .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                    .containsExactly(
                            BigDecimal.ZERO,
                            new BigDecimal("0.7071067811865475"),
                            new BigDecimal("4.932882862316247")
                    );
        }

        @Test
        void standardDeviationBy() {
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
                    .gather(Gatherers4j.runningSampleStandardDeviationBy(TestValueHolder::value))
                    .toList();

            // Assert
            assertThat(output)
                    .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                    .containsExactly(
                            BigDecimal.ZERO,
                            new BigDecimal("0.7071067811865475"),
                            new BigDecimal("4.932882862316247"),
                            new BigDecimal("8.808140174482542"),
                            new BigDecimal("12.36122971228995")
                    );
        }

        @Test
        void treatNullAsNonZero() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of(null, BigDecimal.ONE, null, BigDecimal.ONE);

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.runningSampleStandardDeviation().treatNullAs(BigDecimal.TEN))
                    .toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            BigDecimal.ZERO,
                            new BigDecimal("6.363961030678928"),
                            new BigDecimal("5.196152422706632"),
                            new BigDecimal("5.196152422706632")
                    );
        }

        @Test
        void treatNullAsZero() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of(null, BigDecimal.ONE, null, BigDecimal.ONE);

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.runningSampleStandardDeviation().treatNullAsZero())
                    .toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            BigDecimal.ZERO,
                            new BigDecimal("0.7071067811865475"),
                            new BigDecimal("0.5773502691896257"),
                            new BigDecimal("0.5773502691896257")
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
                    .gather(Gatherers4j.runningSampleStandardDeviation().withOriginal())
                    .toList();

            // Assert
            assertThat(output)
                    .map(WithOriginal::calculated)
                    .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                    .contains(
                            BigDecimal.ZERO,
                            new BigDecimal("0.7071067811865475"),
                            new BigDecimal("4.932882862316247"),
                            new BigDecimal("8.808140174482542"),
                            new BigDecimal("12.36122971228995")
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
                    .gather(Gatherers4j.runningSampleStandardDeviationBy(TestValueHolder::value).withOriginal())
                    .toList();

            // Assert
            assertThat(output)
                    .extracting(WithOriginal::calculated)
                    .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                    .containsExactly(
                            BigDecimal.ZERO,
                            new BigDecimal("0.7071067811865475"),
                            new BigDecimal("4.932882862316247"),
                            new BigDecimal("8.808140174482542"),
                            new BigDecimal("12.36122971228995")
                    );

            assertThat(output)
                    .map(WithOriginal::original)
                    .containsExactlyInAnyOrderElementsOf(input);
        }
    }

}