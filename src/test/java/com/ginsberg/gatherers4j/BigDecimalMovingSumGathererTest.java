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
import java.util.List;
import java.util.stream.Stream;

import static com.ginsberg.gatherers4j.util.TestUtils.BIG_DECIMAL_RECURSIVE_COMPARISON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BigDecimalMovingSumGathererTest {


    @Test
    void ignoresNulls() {
        // Arrange
        final Stream<BigDecimal> input = Stream.of(null, BigDecimal.TWO, BigDecimal.TWO, BigDecimal.TEN);

        // Act
        final List<BigDecimal> output = input
                .gather(Gatherers4j.movingSum(2))
                .toList();

        // Assert
        assertThat(output)
                .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .containsExactly(
                        new BigDecimal("4"),
                        new BigDecimal("12")
                );
    }

    @Test
    @SuppressWarnings("DataFlowIssue")
    void mathContextCannotBeNull() {
        assertThatThrownBy(() ->
                Stream.of(BigDecimal.ONE).gather(Gatherers4j.movingSum(2).withMathContext(null))
        ).isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void movingSum() {
        // Arrange
        final Stream<BigDecimal> input = Stream.of("1", "2", "3", "4").map(BigDecimal::new);

        // Act
        final List<BigDecimal> output = input.gather(Gatherers4j.movingSum(2)).toList();

        // Assert
        assertThat(output)
                .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .containsExactly(
                        new BigDecimal("3"),
                        new BigDecimal("5"),
                        new BigDecimal("7")
                );
    }


    @Test
    void movingSumBy() {
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
                .gather(Gatherers4j.movingSumBy(2, TestValueHolder::value))
                .toList();

        // Assert
        assertThat(output)
                .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                .containsExactly(
                        new BigDecimal("3"),
                        new BigDecimal("12"),
                        new BigDecimal("30"),
                        new BigDecimal("50")
                );
    }


    @Test
    void movingSumWithPartials() {
        // Arrange
        final Stream<BigDecimal> input = Stream.of("1", "2", "3", "4").map(BigDecimal::new);

        // Act
        final List<BigDecimal> output = input
                .gather(Gatherers4j.movingSum(2).includePartialValues())
                .toList();

        // Assert
        assertThat(output)
                .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .containsExactly(
                        new BigDecimal("1"),
                        new BigDecimal("3"),
                        new BigDecimal("5"),
                        new BigDecimal("7")
                );
    }

    @Test
    void movingSumWithPartialsWithOriginal() {
        // Arrange
        final Stream<BigDecimal> input = Stream.of("1", "2", "3", "4").map(BigDecimal::new);

        // Act
        final List<WithOriginal<BigDecimal, BigDecimal>> output = input
                .gather(Gatherers4j.movingSum(2)
                        .includePartialValues()
                        .withOriginal())
                .toList();

        // Assert
        assertThat(output)
                .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .containsExactly(
                        new WithOriginal<>(new BigDecimal("1"), new BigDecimal("1")),
                        new WithOriginal<>(new BigDecimal("2"), new BigDecimal("3")),
                        new WithOriginal<>(new BigDecimal("3"), new BigDecimal("5")),
                        new WithOriginal<>(new BigDecimal("4"), new BigDecimal("7"))
                );
    }

    @Test
    void treatNullAsOne() {
        // Arrange
        final Stream<BigDecimal> input = Stream.of(
                new BigDecimal("2"),
                new BigDecimal("3"),
                null,
                new BigDecimal("4")
        );

        // Act
        final List<BigDecimal> output = input
                .gather(Gatherers4j.movingSum(2).treatNullAsZero())
                .toList();

        // Assert
        assertThat(output)
                .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .containsExactly(
                        new BigDecimal("5"),
                        new BigDecimal("3"),
                        new BigDecimal("4")
                );
    }


    @ParameterizedTest(name = "windowSize of {0}")
    @ValueSource(ints = {-1, 0, 1})
    void windowSizeMustBeGreaterThanOne(final int windowSize) {
        assertThatThrownBy(() ->
                Stream.of(BigDecimal.ONE).gather(Gatherers4j.movingSum(windowSize))
        ).isExactlyInstanceOf(IllegalArgumentException.class);
    }

}