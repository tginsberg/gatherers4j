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

class BigDecimalMovingGeometricMeanGathererTest {

    @Test
    void ignoresNulls() {
        // Arrange
        final Stream<BigDecimal> input = Stream.of(null, BigDecimal.valueOf(1), BigDecimal.valueOf(4), BigDecimal.valueOf(16));

        // Act
        final List<BigDecimal> output = input
                .gather(Gatherers4j.movingGeometricMean(2))
                .toList();

        // Assert
        assertThat(output)
                .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .containsExactly(
                        new BigDecimal("1"),
                        new BigDecimal("2"),
                        new BigDecimal("8")
                );
    }

    @Test
    void movingGeometricMean() {
        // Arrange
        final Stream<BigDecimal> input = Stream.of("1", "4", "16", "64").map(BigDecimal::new);

        // Act
        final List<BigDecimal> output = input
                .gather(Gatherers4j.movingGeometricMean(2))
                .toList();

        // Assert
        assertThat(output)
                .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .containsExactly(
                        new BigDecimal("1"),
                        new BigDecimal("2"),
                        new BigDecimal("8"),
                        new BigDecimal("32")
                );
    }

    @Test
    void movingGeometricMeanWithZero() {
        // Arrange
        final Stream<BigDecimal> input = Stream.of("1", "0", "16", "64").map(BigDecimal::new);

        // Act
        final List<BigDecimal> output = input
                .gather(Gatherers4j.movingGeometricMean(2))
                .toList();

        // Assert
        assertThat(output)
                .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .containsExactly(
                        new BigDecimal("1"),
                        new BigDecimal("0"),
                        new BigDecimal("0"),
                        new BigDecimal("32")
                );
    }

    @Test
    void movingGeometricMeanExcludingPartialValues() {
        // Arrange
        final Stream<BigDecimal> input = Stream.of("1", "4", "16", "64").map(BigDecimal::new);

        // Act
        final List<BigDecimal> output = input
                .gather(Gatherers4j.movingGeometricMean(2).excludePartialValues())
                .toList();

        // Assert
        assertThat(output)
                .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .containsExactly(
                        new BigDecimal("2"),
                        new BigDecimal("8"),
                        new BigDecimal("32")
                );
    }

    @Test
    void movingGeometricMeanBy() {
        // Arrange
        final List<TestValueHolder> input = List.of(
                new TestValueHolder(1, new BigDecimal("1")),
                new TestValueHolder(2, new BigDecimal("4")),
                new TestValueHolder(3, new BigDecimal("16")),
                new TestValueHolder(4, new BigDecimal("64"))
        );

        // Act
        final List<BigDecimal> output = input.stream()
                .gather(Gatherers4j.movingGeometricMeanBy(2, TestValueHolder::value))
                .toList();

        // Assert
        assertThat(output)
                .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                .containsExactly(
                        new BigDecimal("1"),
                        new BigDecimal("2"),
                        new BigDecimal("8"),
                        new BigDecimal("32")
                );
    }

    @Test
    void treatNullAsOne() {
        // Arrange
        final Stream<BigDecimal> input = Stream.of(
                new BigDecimal("1"),
                null,
                new BigDecimal("4")
        );

        // Act
        final List<BigDecimal> output = input
                .gather(Gatherers4j.movingGeometricMean(2).treatNullAsOne())
                .toList();

        // Assert
        assertThat(output)
                .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .containsExactly(
                        new BigDecimal("1"),
                        new BigDecimal("1"),
                        new BigDecimal("2")
                );
    }

    @ParameterizedTest(name = "windowSize of {0}")
    @ValueSource(ints = {-1, 0, 1})
    void windowSizeMustBeGreaterThanOne(final int windowSize) {
        assertThatThrownBy(() ->
                Stream.of(BigDecimal.ONE).gather(Gatherers4j.movingGeometricMean(windowSize))
        ).isExactlyInstanceOf(IllegalArgumentException.class);
    }
}
