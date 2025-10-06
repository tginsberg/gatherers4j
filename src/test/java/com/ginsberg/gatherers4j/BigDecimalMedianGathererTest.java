/*
 * Copyright 2025 Todd Ginsberg
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
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.ginsberg.gatherers4j.util.TestUtils.BIG_DECIMAL_RECURSIVE_COMPARISON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BigDecimalMedianGathererTest {

    @Nested
    class Moving {

        @ParameterizedTest(name = "windowSize of {0}")
        @ValueSource(ints = {-1, 0, 1})
        void byFunctionWindowSizeMustBeGreaterThanOne(final int size) {
            assertThatThrownBy(() ->
                    Gatherers4j.movingMedianBy(size, Function.identity())
            ).isExactlyInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void functionMustNotBeNull() {
            assertThatThrownBy(() ->
                    Gatherers4j.movingMedianBy(2, null)
            ).isExactlyInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void ignoresNulls() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of(null, BigDecimal.ONE, BigDecimal.TWO, BigDecimal.TEN);

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.movingMedian(3).includePartialValues())
                    .toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            BigDecimal.ONE,
                            new BigDecimal("1.5"),
                            BigDecimal.TWO
                    );
        }

        @Test
        @SuppressWarnings("DataFlowIssue")
        void mathContextCannotBeNull() {
            assertThatThrownBy(() ->
                    Stream.of(BigDecimal.ONE).gather(Gatherers4j.movingMedian(2).withMathContext(null))
            ).isExactlyInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void movingMedian() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of("1", "2", "3", "4").map(BigDecimal::new);

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.movingMedian(2).includePartialValues())
                    .toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            new BigDecimal("1"),
                            new BigDecimal("1.5"),
                            new BigDecimal("2.5"),
                            new BigDecimal("3.5")
                    );
        }

        @Test
        void movingMedianBy() {
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
                    .gather(Gatherers4j.movingMedianBy(2, TestValueHolder::value).includePartialValues())
                    .toList();

            // Assert
            assertThat(output)
                    .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                    .containsExactly(
                            new BigDecimal("1"),
                            new BigDecimal("1.5"),
                            new BigDecimal("6"),
                            new BigDecimal("15"),
                            new BigDecimal("25")
                    );
        }

        @Test
        void movingMedianWithDuplicateRemovals() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of("2", "2", "2", "2", "2").map(BigDecimal::new);

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.movingMedian(3).includePartialValues())
                    .toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            new BigDecimal("2"),
                            new BigDecimal("2"),
                            new BigDecimal("2"),
                            new BigDecimal("2"),
                            new BigDecimal("2")
                    );
        }

        @Test
        void movingMedianWithOriginal() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of("1", "2", "3", "4").map(BigDecimal::new);

            // Act
            final List<WithOriginal<BigDecimal, BigDecimal>> output = input
                    .gather(Gatherers4j.movingMedian(2).includePartialValues().withOriginal())
                    .toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            new WithOriginal<>(new BigDecimal("1"), new BigDecimal("1")),
                            new WithOriginal<>(new BigDecimal("2"), new BigDecimal("1.5")),
                            new WithOriginal<>(new BigDecimal("3"), new BigDecimal("2.5")),
                            new WithOriginal<>(new BigDecimal("4"), new BigDecimal("3.5"))
                    );
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
                    .gather(Gatherers4j.movingMedian(2).includePartialValues().treatNullAsZero())
                    .toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            new BigDecimal("2"),
                            new BigDecimal("2.5"),
                            new BigDecimal("1.5"),
                            new BigDecimal("2")
                    );
        }

        @ParameterizedTest(name = "windowSize of {0}")
        @ValueSource(ints = {-1, 0, 1})
        void windowSizeMustBeGreaterThanOne(final int size) {
            assertThatThrownBy(() ->
                    Gatherers4j.movingMedian(size)
            ).isExactlyInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class Running {
        @Test
        void functionMustNotBeNull() {
            assertThatThrownBy(() ->
                    Gatherers4j.runningMedianBy(null)
            ).isExactlyInstanceOf(IllegalArgumentException.class);
        }


        @Test
        void ignoresNulls() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of(null, BigDecimal.ONE, BigDecimal.TWO, BigDecimal.TEN);

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.runningMedian())
                    .toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            BigDecimal.ONE,
                            new BigDecimal("1.5"),
                            BigDecimal.TWO
                    );
        }

        @Test
        @SuppressWarnings("DataFlowIssue")
        void mathContextCannotBeNull() {
            assertThatThrownBy(() ->
                    Stream.of(BigDecimal.ONE).gather(Gatherers4j.runningMedian().withMathContext(null))
            ).isExactlyInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void runningMedian() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of("1", "2", "3", "4").map(BigDecimal::new);

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.runningMedian())
                    .toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            new BigDecimal("1"),
                            new BigDecimal("1.5"),
                            new BigDecimal("2"),
                            new BigDecimal("2.5")
                    );
        }

        @Test
        void runningMedianBy() {
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
                    .gather(Gatherers4j.runningMedianBy(TestValueHolder::value))
                    .toList();

            // Assert
            assertThat(output)
                    .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                    .containsExactly(
                            new BigDecimal("1"),
                            new BigDecimal("1.5"),
                            new BigDecimal("2"),
                            new BigDecimal("6"),
                            new BigDecimal("10")
                    );
        }

        @Test
        void runningMedianWithOriginal() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of("1", "2", "3", "4").map(BigDecimal::new);

            // Act
            final List<WithOriginal<BigDecimal, BigDecimal>> output = input
                    .gather(Gatherers4j.runningMedian().withOriginal())
                    .toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            new WithOriginal<>(new BigDecimal("1"), new BigDecimal("1")),
                            new WithOriginal<>(new BigDecimal("2"), new BigDecimal("1.5")),
                            new WithOriginal<>(new BigDecimal("3"), new BigDecimal("2")),
                            new WithOriginal<>(new BigDecimal("4"), new BigDecimal("2.5"))
                    );
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
                    .gather(Gatherers4j.runningMedian().treatNullAsZero())
                    .toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            new BigDecimal("2"),
                            new BigDecimal("2.5"),
                            new BigDecimal("2"),
                            new BigDecimal("2.5")
                    );
        }
    }

}