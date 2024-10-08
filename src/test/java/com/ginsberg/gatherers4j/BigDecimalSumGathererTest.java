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

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BigDecimalSumGathererTest {

    @Test
    void ignoresNull() {
        // Arrange
        final Stream<BigDecimal> input = Stream.of(
                new BigDecimal("2"),
                new BigDecimal("3"),
                null,
                new BigDecimal("4")
        );

        // Act
        final List<BigDecimal> output = input
                .gather(Gatherers4j.runningSum())
                .toList();

        // Assert
        assertThat(output)
                .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .containsExactly(
                        new BigDecimal("2"),
                        new BigDecimal("5"),
                        new BigDecimal("9")
                );
    }

    @Test
    void mathContextCannotBeNull() {
        assertThatThrownBy(() ->
                Stream.of(BigDecimal.ONE).gather(Gatherers4j.runningSum().withMathContext(null))
        ).isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void runningSum() {
        // Arrange
        final Stream<BigDecimal> input = Stream.of("1", "2", "3").map(BigDecimal::new);

        // Act
        final List<BigDecimal> output = input.gather(Gatherers4j.runningSum()).toList();

        // Assert
        assertThat(output)
                .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .containsExactly(
                        BigDecimal.ONE,
                        new BigDecimal("3"),
                        new BigDecimal("6")
                );
    }

    @Test
    void runningSumBy() {
        // Arrange
        final Stream<TestValueHolder> input = Stream.of(
                new TestValueHolder(1, new BigDecimal("1.0")),
                new TestValueHolder(2, new BigDecimal("2.0")),
                new TestValueHolder(3, new BigDecimal("10.0")),
                new TestValueHolder(4, new BigDecimal("20.0")),
                new TestValueHolder(5, new BigDecimal("30.0"))
        );

        // Act
        final List<BigDecimal> output = input.gather(Gatherers4j.runningSumBy(TestValueHolder::value)).toList();

        // Assert
        assertThat(output)
                .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .containsExactly(
                        BigDecimal.ONE,
                        new BigDecimal("3"),
                        new BigDecimal("13"),
                        new BigDecimal("33"),
                        new BigDecimal("63")
                );
    }


    @Test
    void withOriginalBigDecimal() {
        // Arrange
        final Stream<BigDecimal> input = Stream.of("1", "2", "3").map(BigDecimal::new);

        // Act
        final List<WithOriginal<BigDecimal, BigDecimal>> output = input
                .gather(Gatherers4j.runningSum().withOriginal())
                .toList();

        // Assert
        assertThat(output)
                .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .containsExactly(
                        new WithOriginal<>(BigDecimal.ONE, BigDecimal.ONE),
                        new WithOriginal<>(new BigDecimal("2"), new BigDecimal("3")),
                        new WithOriginal<>(new BigDecimal("3"), new BigDecimal("6"))

                );
    }

}