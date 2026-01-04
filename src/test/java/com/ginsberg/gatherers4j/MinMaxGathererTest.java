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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.ginsberg.gatherers4j.dto.WithOriginal;

import static java.util.Comparator.comparing;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MinMaxGathererTest {

    @Nested
    class Max {
        @Nested
        class Moving {

            @Test
            void ignoresNulls() {
                // Arrange
                final Stream<Integer> input = Stream.of(null, 1, null, 3, null, 5);

                // Act
                final List<Integer> output = input
                        .gather(Gatherers4j.movingMax(3))
                        .toList();

                // Assert
                assertThat(output).containsExactly(1, 3, 5);
            }

            @Test
            void movingMaxByWithOriginal() {
                // Arrange
                final Stream<Integer> input = IntStream.range(1, 10).boxed().gather(Gatherers4j.reverse());

                // Act
                final List<WithOriginal<Integer, Integer>> output = input
                        .gather(Gatherers4j.<Integer>movingMaxBy(3, comparing(String::valueOf)).withOriginal())
                        .toList();

                // Assert
                assertThat(output).containsExactly(
                        new WithOriginal<>(9, 9),
                        new WithOriginal<>(8, 9),
                        new WithOriginal<>(7, 9),
                        new WithOriginal<>(6, 8),
                        new WithOriginal<>(5, 7),
                        new WithOriginal<>(4, 6),
                        new WithOriginal<>(3, 5),
                        new WithOriginal<>(2, 4),
                        new WithOriginal<>(1, 3)
                );
            }

            @Test
            void movingMaxComparable() {
                // Arrange
                final Stream<String> input = IntStream.range(1, 10).mapToObj(String::valueOf).gather(Gatherers4j.reverse());

                // Act
                final List<String> output = input
                        .gather(Gatherers4j.movingMax(3))
                        .toList();

                // Assert
                assertThat(output).containsExactly("9", "9", "9", "8", "7", "6", "5", "4", "3");
            }

            @Test
            void movingMaxComparableExcludingPartials() {
                // Arrange
                final Stream<String> input = IntStream.range(1, 10).mapToObj(String::valueOf).gather(Gatherers4j.reverse());

                // Act
                final List<String> output = input
                        .gather(Gatherers4j.<String>movingMax(3).excludePartialValues())
                        .toList();

                // Assert
                assertThat(output).containsExactly("9", "8", "7", "6", "5", "4", "3");
            }

            @Test
            void movingMaxComparator() {
                // Arrange
                final Stream<Integer> input = IntStream.range(1, 10).boxed().gather(Gatherers4j.reverse());

                // Act
                final List<Integer> output = input
                        .gather(Gatherers4j.movingMaxBy(3, comparing(String::valueOf)))
                        .toList();

                // Assert
                assertThat(output).containsExactly(9, 9, 9, 8, 7, 6, 5, 4, 3);
            }

            @Test
            void movingMaxComparatorExcludingPartials() {
                // Arrange
                final Stream<Integer> input = IntStream.range(1, 10).boxed().gather(Gatherers4j.reverse());

                // Act
                final List<Integer> output = input
                        .gather(Gatherers4j.<Integer>movingMaxBy(3, comparing(String::valueOf)).excludePartialValues())
                        .toList();

                // Assert
                assertThat(output).containsExactly(9, 8, 7, 6, 5, 4, 3);
            }

            @Test
            void movingMaxWithOriginal() {
                // Arrange
                final Stream<String> input = IntStream.range(1, 10).mapToObj(String::valueOf).gather(Gatherers4j.reverse());

                // Act
                final List<WithOriginal<String, String>> output = input
                        .gather(Gatherers4j.<String>movingMax(3).withOriginal())
                        .toList();

                // Assert
                assertThat(output).containsExactly(
                        new WithOriginal<>("9", "9"),
                        new WithOriginal<>("8", "9"),
                        new WithOriginal<>("7", "9"),
                        new WithOriginal<>("6", "8"),
                        new WithOriginal<>("5", "7"),
                        new WithOriginal<>("4", "6"),
                        new WithOriginal<>("3", "5"),
                        new WithOriginal<>("2", "4"),
                        new WithOriginal<>("1", "3")
                );
            }

            @ParameterizedTest(name = "windowSize of {0}")
            @ValueSource(ints = {-1, 0, 1})
            void windowSizeMustBeAtLeast2Comparable(final int value) {
                assertThatThrownBy(() ->
                        MinMaxGatherer.movingUsingComparable(value, true)
                ).isExactlyInstanceOf(IllegalArgumentException.class);
            }

            @ParameterizedTest(name = "windowSize of {0}")
            @ValueSource(ints = {-1, 0, 1})
            void windowSizeMustBeAtLeast2Comparator(final int value) {
                assertThatThrownBy(() ->
                        MinMaxGatherer.movingUsingComparator(value, true, String::compareTo)
                ).isExactlyInstanceOf(IllegalArgumentException.class);
            }
        }

        @Nested
        class Running {

            @Test
            void ascendingEmitsAllComparable() {
                // Arrange
                final Stream<Integer> input = IntStream.range(1, 10).boxed();

                // Act
                final List<Integer> output = input.gather(Gatherers4j.runningMax()).toList();

                // Assert
                assertThat(output).containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9);
            }

            @Test
            void ascendingEmitsAllComparator() {
                // Arrange
                final Stream<String> input = IntStream.range(1, 10).mapToObj(String::valueOf);

                // Act
                final List<String> output = input
                        .gather(Gatherers4j.runningMaxBy(comparing(Integer::valueOf)))
                        .toList();

                // Assert
                assertThat(output).containsExactly("1", "2", "3", "4", "5", "6", "7", "8", "9");
            }

            @Test
            void forceEvictions() {
                // Arrange
                final Stream<Integer> input = Stream.of(5, 4, 3, 2, 20);

                // Act
                final List<Integer> output = input
                        .gather(MinMaxGatherer.movingUsingComparator(3, false, comparing(Integer::valueOf)))
                        .toList();

                // Assert
                assertThat(output).containsExactly(5, 5, 5, 4, 20);
            }

            @Test
            void ignoresNulls() {
                // Arrange
                final Stream<Integer> input = Stream.of(null, 5, null, 3, null, 1);

                // Act
                final List<Integer> output = input.gather(Gatherers4j.runningMax()).toList();

                // Assert
                assertThat(output).containsExactly(5, 5, 5);
            }

            @Test
            void runningMaxByWithOriginal() {
                // Arrange
                final Stream<String> input = IntStream.range(1, 10).mapToObj(String::valueOf);

                // Act
                final List<WithOriginal<String, String>> output = input
                        .gather(Gatherers4j.<String>runningMaxBy(comparing(Integer::valueOf)).withOriginal())
                        .toList();

                // Assert
                assertThat(output).containsExactly(
                        new WithOriginal<>("1", "1"),
                        new WithOriginal<>("2", "2"),
                        new WithOriginal<>("3", "3"),
                        new WithOriginal<>("4", "4"),
                        new WithOriginal<>("5", "5"),
                        new WithOriginal<>("6", "6"),
                        new WithOriginal<>("7", "7"),
                        new WithOriginal<>("8", "8"),
                        new WithOriginal<>("9", "9")
                );
            }

            @Test
            void runningMaxWithOriginal() {
                // Arrange
                final Stream<Integer> input = IntStream.range(1, 10).boxed();

                // Act
                final List<WithOriginal<Integer, Integer>> output = input
                        .gather(Gatherers4j.<Integer>runningMax().withOriginal())
                        .toList();

                // Assert
                assertThat(output).containsExactly(
                        new WithOriginal<>(1, 1),
                        new WithOriginal<>(2, 2),
                        new WithOriginal<>(3, 3),
                        new WithOriginal<>(4, 4),
                        new WithOriginal<>(5, 5),
                        new WithOriginal<>(6, 6),
                        new WithOriginal<>(7, 7),
                        new WithOriginal<>(8, 8),
                        new WithOriginal<>(9, 9)
                );
            }

            @Test
            void unchangingIsConstantComparable() {
                // Arrange
                final Stream<Integer> input = IntStream.range(1, 10).map(_ -> 1).boxed().gather(Gatherers4j.reverse());

                // Act
                final List<Integer> output = input.gather(Gatherers4j.runningMax()).toList();

                // Assert
                assertThat(output).containsExactly(1, 1, 1, 1, 1, 1, 1, 1, 1);
            }

            @Test
            void unchangingIsConstantComparator() {
                // Arrange
                final Stream<String> input = IntStream.range(1, 10).mapToObj(_ -> "1").gather(Gatherers4j.reverse());

                // Act
                final List<String> output = input
                        .gather(Gatherers4j.runningMaxBy(comparing(Integer::valueOf)))
                        .toList();

                // Assert
                assertThat(output).containsExactly("1", "1", "1", "1", "1", "1", "1", "1", "1");
            }
        }
    }

    @Nested
    class Min {
        @Nested
        class Moving {

            @Test
            void ascendingComparable() {
                // Arrange
                final Stream<Integer> input = IntStream.range(1, 10).boxed();

                // Act
                final List<Integer> output = input
                        .gather(Gatherers4j.movingMinBy(3, comparing(Integer::valueOf)))
                        .toList();

                // Assert
                assertThat(output).containsExactly(1, 1, 1, 2, 3, 4, 5, 6, 7);
            }

            @Test
            void ascendingComparator() {
                // Arrange
                final Stream<String> input = IntStream.range(1, 10).mapToObj(String::valueOf);

                // Act
                final List<String> output = input
                        .gather(Gatherers4j.movingMinBy(3, comparing(Integer::valueOf)))
                        .toList();

                // Assert
                assertThat(output).containsExactly("1", "1", "1", "2", "3", "4", "5", "6", "7");
            }

            @Test
            void forceEvictions() {
                // Arrange
                final Stream<Integer> input = Stream.of(5, 3, 7, 2, 8, 1);

                // Act
                final List<Integer> output = input
                        .gather(MinMaxGatherer.movingUsingComparator(3, true, comparing(Integer::valueOf)))
                        .toList();

                // Assert
                assertThat(output).containsExactly(5, 3, 3, 2, 2, 1);
            }

            @Test
            void ignoresNulls() {
                // Arrange
                final Stream<Integer> input = Stream.of(null, 5, null, 3, null, 1);

                // Act
                final List<Integer> output = input
                        .gather(Gatherers4j.movingMin(3))
                        .toList();

                // Assert
                assertThat(output).containsExactly(5, 3, 1);
            }

            @Test
            void movingMinByWithOriginal() {
                // Arrange
                final Stream<String> input = IntStream.range(1, 10).mapToObj(String::valueOf);

                // Act
                final List<WithOriginal<String, String>> output = input
                        .gather(Gatherers4j.<String>movingMinBy(3, comparing(Integer::valueOf)).withOriginal())
                        .toList();

                // Assert
                assertThat(output).containsExactly(
                        new WithOriginal<>("1", "1"),
                        new WithOriginal<>("2", "1"),
                        new WithOriginal<>("3", "1"),
                        new WithOriginal<>("4", "2"),
                        new WithOriginal<>("5", "3"),
                        new WithOriginal<>("6", "4"),
                        new WithOriginal<>("7", "5"),
                        new WithOriginal<>("8", "6"),
                        new WithOriginal<>("9", "7")
                );
            }

            @Test
            void movingMinWithOriginal() {
                // Arrange
                final Stream<Integer> input = IntStream.range(1, 10).boxed();

                // Act
                final List<WithOriginal<Integer, Integer>> output = input
                        .gather(Gatherers4j.<Integer>movingMin(3).withOriginal())
                        .toList();

                // Assert
                assertThat(output).containsExactly(
                        new WithOriginal<>(1, 1),
                        new WithOriginal<>(2, 1),
                        new WithOriginal<>(3, 1),
                        new WithOriginal<>(4, 2),
                        new WithOriginal<>(5, 3),
                        new WithOriginal<>(6, 4),
                        new WithOriginal<>(7, 5),
                        new WithOriginal<>(8, 6),
                        new WithOriginal<>(9, 7)
                );
            }

            @ParameterizedTest(name = "windowSize of {0}")
            @ValueSource(ints = {-1, 0, 1})
            void windowSizeMustBeAtLeast2Comparable(final int value) {
                assertThatThrownBy(() ->
                        MinMaxGatherer.movingUsingComparable(value, true)
                ).isExactlyInstanceOf(IllegalArgumentException.class);
            }

            @ParameterizedTest(name = "windowSize of {0}")
            @ValueSource(ints = {-1, 0, 1})
            void windowSizeMustBeAtLeast2Comparator(final int value) {
                assertThatThrownBy(() ->
                        MinMaxGatherer.movingUsingComparator(value, true, String::compareTo)
                ).isExactlyInstanceOf(IllegalArgumentException.class);
            }

        }

        @Nested
        class Running {

            @Test
            void descendingEmitsAllComparable() {
                // Arrange
                final Stream<Integer> input = IntStream.range(1, 10).boxed().gather(Gatherers4j.reverse());

                // Act
                final List<Integer> output = input.gather(Gatherers4j.runningMin()).toList();

                // Assert
                assertThat(output).containsExactly(9, 8, 7, 6, 5, 4, 3, 2, 1);
            }

            @Test
            void descendingEmitsAllComparator() {
                // Arrange
                final Stream<String> input = IntStream.range(1, 10).mapToObj(String::valueOf).gather(Gatherers4j.reverse());

                // Act
                final List<String> output = input
                        .gather(Gatherers4j.runningMinBy(comparing(Integer::valueOf)))
                        .toList();

                // Assert
                assertThat(output).containsExactly("9", "8", "7", "6", "5", "4", "3", "2", "1");
            }

            @Test
            void ignoresNulls() {
                // Arrange
                final Stream<Integer> input = Stream.of(null, 5, null, 3, null, 1);

                // Act
                final List<Integer> output = input.gather(Gatherers4j.runningMin()).toList();

                // Assert
                assertThat(output).containsExactly(5, 3, 1);
            }

            @Test
            void runningMinByWithOriginal() {
                // Arrange
                final Stream<String> input = IntStream.range(1, 10).mapToObj(String::valueOf).gather(Gatherers4j.reverse());

                // Act
                final List<WithOriginal<String, String>> output = input
                        .gather(Gatherers4j.<String>runningMinBy(comparing(Integer::valueOf)).withOriginal())
                        .toList();

                // Assert
                assertThat(output).containsExactly(
                        new WithOriginal<>("9", "9"),
                        new WithOriginal<>("8", "8"),
                        new WithOriginal<>("7", "7"),
                        new WithOriginal<>("6", "6"),
                        new WithOriginal<>("5", "5"),
                        new WithOriginal<>("4", "4"),
                        new WithOriginal<>("3", "3"),
                        new WithOriginal<>("2", "2"),
                        new WithOriginal<>("1", "1")
                );
            }

            @Test
            void runningMinWithOriginal() {
                // Arrange
                final Stream<Integer> input = IntStream.range(1, 10).boxed().gather(Gatherers4j.reverse());

                // Act
                final List<WithOriginal<Integer, Integer>> output = input
                        .gather(Gatherers4j.<Integer>runningMin().withOriginal())
                        .toList();

                // Assert
                assertThat(output).containsExactly(
                        new WithOriginal<>(9, 9),
                        new WithOriginal<>(8, 8),
                        new WithOriginal<>(7, 7),
                        new WithOriginal<>(6, 6),
                        new WithOriginal<>(5, 5),
                        new WithOriginal<>(4, 4),
                        new WithOriginal<>(3, 3),
                        new WithOriginal<>(2, 2),
                        new WithOriginal<>(1, 1)
                );
            }

            @Test
            void unchangingIsConstantComparable() {
                // Arrange
                final Stream<Integer> input = IntStream.range(1, 10).map(_ -> 1).boxed().gather(Gatherers4j.reverse());

                // Act
                final List<Integer> output = input.gather(Gatherers4j.runningMin()).toList();

                // Assert
                assertThat(output).containsExactly(1, 1, 1, 1, 1, 1, 1, 1, 1);
            }

            @Test
            void unchangingIsConstantComparator() {
                // Arrange
                final Stream<String> input = IntStream.range(1, 10).mapToObj(_ -> "1").gather(Gatherers4j.reverse());

                // Act
                final List<String> output = input
                        .gather(Gatherers4j.runningMinBy(comparing(Integer::valueOf)))
                        .toList();

                // Assert
                assertThat(output).containsExactly("1", "1", "1", "1", "1", "1", "1", "1", "1");
            }
        }

    }
}
