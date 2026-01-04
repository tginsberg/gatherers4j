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
import com.ginsberg.gatherers4j.util.TestValueHolder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.stream.Stream;

import static com.ginsberg.gatherers4j.util.TestUtils.BIG_DECIMAL_RECURSIVE_COMPARISON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BigDecimalSimpleMovingAverageGathererTest {

    @Test
    void ignoresNulls() {
        // Arrange
        final Stream<BigDecimal> input = Stream.of(null, BigDecimal.ONE, BigDecimal.TWO, BigDecimal.TEN);

        // Act
        final List<BigDecimal> output = input
                .gather(Gatherers4j.simpleMovingAverage(2))
                .toList();

        // Assert
        assertThat(output)
                .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .containsExactly(
                        new BigDecimal("1"),
                        new BigDecimal("1.5"),
                        new BigDecimal("6.0")
                );
    }

    @Test
    void mathContextCannotBeNull() {
        assertThatThrownBy(() ->
                Stream.of(BigDecimal.ONE).gather(Gatherers4j.simpleMovingAverage(2).withMathContext(null))
        ).isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void mathContextChange() {
        // Arrange
        final Stream<BigDecimal> input = Stream.of(
                BigDecimal.ONE,
                BigDecimal.TWO,
                BigDecimal.TEN
        );

        // Act
        final List<BigDecimal> output = input
                .gather(Gatherers4j.simpleMovingAverage(3).withMathContext(new MathContext(3)))
                .toList();

        // Assert
        assertThat(output)
                .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .containsExactly(
                        new BigDecimal("1"),
                        new BigDecimal("1.5"),
                        new BigDecimal("4.33")
                );
    }

    @Test
    void movingAverageBy() {
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
                .gather(Gatherers4j.simpleMovingAverageBy(2, TestValueHolder::value))
                .toList();

        // Assert
        assertThat(output)
                .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                .containsExactly(
                        new BigDecimal("1.0"),
                        new BigDecimal("1.5"),
                        new BigDecimal("6.0"),
                        new BigDecimal("15.0"),
                        new BigDecimal("25.0")
                );
    }

    @Test
    void movingAverageOfBigDecimals() {
        // Arrange
        final Stream<BigDecimal> input = Stream.of(
                new BigDecimal("1.0"),
                new BigDecimal("2.0"),
                new BigDecimal("10.0"),
                new BigDecimal("2.0")
        );

        // Act
        final List<BigDecimal> output = input
                .gather(Gatherers4j.simpleMovingAverage(3))
                .toList();

        // Assert
        assertThat(output)
                .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                .containsExactly(
                        new BigDecimal("1.0"),
                        new BigDecimal("1.5"),
                        new BigDecimal("4.333333333333333"),
                        new BigDecimal("4.666666666666667")
                );
    }

    @Test
    void movingAverageOfZero() {
        // Arrange
        final Stream<BigDecimal> input = Stream.of(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

        // Act
        final List<BigDecimal> output = input
                .gather(Gatherers4j.simpleMovingAverage(2))
                .toList();

        // Assert
        assertThat(output)
                .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .containsExactly(
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                );
    }

    @Test
    void treatNullAsNonZero() {
        // Arrange
        final Stream<BigDecimal> input = Stream.of(null, BigDecimal.ONE, null, BigDecimal.ONE);

        // Act
        final List<BigDecimal> output = input
                .gather(Gatherers4j.simpleMovingAverage(2).treatNullAs(BigDecimal.TEN))
                .toList();

        // Assert
        assertThat(output)
                .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .containsExactly(
                        new BigDecimal("10"),
                        new BigDecimal("5.5"),
                        new BigDecimal("5.5"),
                        new BigDecimal("5.5")
                );
    }

    @Test
    void treatNullAsZero() {
        // Arrange
        final Stream<BigDecimal> input = Stream.of(null, BigDecimal.ONE, null, BigDecimal.ONE);

        // Act
        final List<BigDecimal> output = input
                .gather(Gatherers4j.simpleMovingAverage(2).treatNullAsZero())
                .toList();

        // Assert
        assertThat(output)
                .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .containsExactly(
                        new BigDecimal("0"),
                        new BigDecimal("0.5"),
                        new BigDecimal("0.5"),
                        new BigDecimal("0.5")
                );
    }

    @ParameterizedTest(name = "windowSize of {0}")
    @ValueSource(ints = {-1, 0, 1})
    void windowSizeMustBeGreaterThanOne(final int windowSize) {
        assertThatThrownBy(() ->
                Stream.of(BigDecimal.ONE).gather(Gatherers4j.simpleMovingAverage(windowSize))
        ).isExactlyInstanceOf(IllegalArgumentException.class);
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
                .gather(Gatherers4j
                        .simpleMovingAverage(2)
                        .excludePartialValues()
                        .withOriginal()
                ).toList();

        // Assert
        assertThat(output)
                .map(WithOriginal::calculated)
                .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                .contains(
                        new BigDecimal("1.5"),
                        new BigDecimal("6.0"),
                        new BigDecimal("15.0"),
                        new BigDecimal("25.0")
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
                .gather(Gatherers4j.simpleMovingAverageBy(2, TestValueHolder::value).withOriginal())
                .toList();

        // Assert
        assertThat(output)
                .extracting(WithOriginal::calculated)
                .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                .containsExactly(
                        new BigDecimal("1.0"),
                        new BigDecimal("1.5"),
                        new BigDecimal("6.0"),
                        new BigDecimal("15.0"),
                        new BigDecimal("25.0")
                );

        assertThat(output)
                .map(WithOriginal::original)
                .containsExactlyInAnyOrderElementsOf(input);
    }

}