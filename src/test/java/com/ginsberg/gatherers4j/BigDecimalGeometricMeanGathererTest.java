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

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class BigDecimalGeometricMeanGathererTest {

    @Test
    void geometricRunningAverage() {
        // Arrange
        final Stream<BigDecimal> input = Stream.of(
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(8)
        );

        // Act
        final List<BigDecimal> output = input
                .gather(Gatherers4j.runningGeometricMean())
                .toList();

        // Assert
        assertThat(output).hasSize(2);
        assertThat(output.get(0)).isEqualByComparingTo("2");
        assertThat(output.get(1)).isEqualByComparingTo("4");
    }

    @Test
    void geometricRunningAverageWithMapping() {
        // Arrange
        final Stream<String> input = Stream.of("1", "4", "16");

        // Act
        final List<BigDecimal> output = input
                .gather(Gatherers4j.runningGeometricMeanBy(BigDecimal::new))
                .toList();

        // Assert
        assertThat(output).hasSize(3);
        assertThat(output.get(0)).isEqualByComparingTo("1");
        assertThat(output.get(1)).isEqualByComparingTo("2");
        assertThat(output.get(2)).isEqualByComparingTo("4");
    }

    @Test
    void geometricRunningAverageIgnoresNulls() {
        // Arrange
        final Stream<BigDecimal> input = Stream.of(
                BigDecimal.valueOf(2),
                null,
                BigDecimal.valueOf(8)
        );

        // Act
        final List<BigDecimal> output = input
                .gather(Gatherers4j.runningGeometricMean())
                .toList();

        // Assert
        assertThat(output).hasSize(2);
        assertThat(output.get(0)).isEqualByComparingTo("2");
        assertThat(output.get(1)).isEqualByComparingTo("4");
    }

    @Test
    void geometricRunningAverageTreatNullAsOne() {
        // Arrange
        final Stream<BigDecimal> input = Stream.of(
                BigDecimal.valueOf(2),
                null,
                BigDecimal.valueOf(8)
        );

        // Act
        final List<BigDecimal> output = input
                .gather(Gatherers4j.runningGeometricMean().treatNullAsOne())
                .toList();

        // Assert
        assertThat(output).hasSize(3);
        assertThat(output.get(0)).isEqualByComparingTo("2");
        assertThat(output.get(1)).isEqualByComparingTo("1.414213562373095");
        assertThat(output.get(2)).isEqualByComparingTo("2.519842099789746");
    }

    @Test
    void geometricRunningAverageWithMathContext() {
        // Arrange
        final Stream<BigDecimal> input = Stream.of(BigDecimal.valueOf(2));
        final MathContext mc = new MathContext(2);

        // Act
        final List<BigDecimal> output = input
                .gather(Gatherers4j.runningGeometricMean().withMathContext(mc))
                .toList();

        // Assert
        assertThat(output).hasSize(1);
        assertThat(output.getFirst()).isEqualByComparingTo("2.0");
    }
}
