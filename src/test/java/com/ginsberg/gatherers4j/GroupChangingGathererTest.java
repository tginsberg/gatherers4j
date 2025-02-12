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

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GroupChangingGathererTest {

    @Nested
    class WithComparable {
        @Nested
        class Decreasing {

            @Test
            void decreasing() {
                // Arrange
                final Stream<Integer> input = Stream.of(3, 2, 1, 2, 2, 1);

                // Act
                final List<List<Integer>> output = input
                        .gather(Gatherers4j.groupDecreasing())
                        .toList();

                // Assert
                assertThat(output)
                        .containsExactly(
                                List.of(3, 2, 1),
                                List.of(2),
                                List.of(2, 1)
                        );
            }

            @Test
            void emptyStream() {
                // Arrange
                final Stream<String> input = Stream.empty();

                // Act
                final List<List<String>> output = input
                        .gather(Gatherers4j.groupDecreasing())
                        .toList();

                // Assert
                assertThat(output).isEmpty();
            }

            @Test
            void ensureDecreasing() {
                // Arrange
                final Stream<Integer> input = Stream.of(4, 3, 2, 1);

                // Act
                final List<Integer> output = input.gather(Gatherers4j.ensureDecreasing()).toList();

                // Assert
                assertThat(output).containsExactly(4, 3, 2, 1);
            }

            @Test
            void ensureDecreasingFailureCase() {
                assertThatThrownBy(() ->
                        Stream.of(1, 1).gather(Gatherers4j.ensureDecreasing()).toList()
                ).isExactlyInstanceOf(IllegalStateException.class);
            }

            @Test
            void singleElementStream() {
                // Arrange
                final Stream<String> input = Stream.of("A");

                // Act
                final List<List<String>> output = input
                        .gather(Gatherers4j.groupDecreasing())
                        .toList();

                // Assert
                assertThat(output).containsExactly(List.of("A"));
            }
        }

        @Nested
        class Increasing {
            @Test
            void emptyStream() {
                // Arrange
                final Stream<String> input = Stream.empty();

                // Act
                final List<List<String>> output = input
                        .gather(Gatherers4j.groupIncreasing())
                        .toList();

                // Assert
                assertThat(output).isEmpty();
            }

            @Test
            void ensureIncreasing() {
                // Arrange
                final Stream<Integer> input = Stream.of(1, 2, 3, 4);

                // Act
                final List<Integer> output = input.gather(Gatherers4j.ensureIncreasing()).toList();

                // Assert
                assertThat(output).containsExactly(1, 2, 3, 4);
            }

            @Test
            void ensureIncreasingFailureCase() {
                assertThatThrownBy(() ->
                        Stream.of(1, 1).gather(Gatherers4j.ensureIncreasing()).toList()
                ).isExactlyInstanceOf(IllegalStateException.class);
            }

            @Test
            void increasing() {
                // Arrange
                final Stream<Integer> input = Stream.of(1, 2, 3, 3, 2, 3);

                // Act
                final List<List<Integer>> output = input
                        .gather(Gatherers4j.groupIncreasing())
                        .toList();

                // Assert
                assertThat(output)
                        .containsExactly(
                                List.of(1, 2, 3),
                                List.of(3),
                                List.of(2, 3)
                        );
            }

            @Test
            void singleElementStream() {
                // Arrange
                final Stream<String> input = Stream.of("A");

                // Act
                final List<List<String>> output = input
                        .gather(Gatherers4j.groupIncreasing())
                        .toList();

                // Assert
                assertThat(output).containsExactly(List.of("A"));
            }
        }

        @Nested
        class NonDecreasing {
            @Test
            void emptyStream() {
                // Arrange
                final Stream<String> input = Stream.empty();

                // Act
                final List<List<String>> output = input
                        .gather(Gatherers4j.groupNonDecreasing())
                        .toList();

                // Assert
                assertThat(output).isEmpty();
            }

            @Test
            void ensureNonDecreasing() {
                // Arrange
                final Stream<Integer> input = Stream.of(4, 4, 5, 6);

                // Act
                final List<Integer> output = input.gather(Gatherers4j.ensureNonDecreasing()).toList();

                // Assert
                assertThat(output).containsExactly(4, 4, 5, 6);
            }

            @Test
            void ensureNonDecreasingFailureCase() {
                assertThatThrownBy(() ->
                        Stream.of(1, 0).gather(Gatherers4j.ensureNonDecreasing()).toList()
                ).isExactlyInstanceOf(IllegalStateException.class);
            }

            @Test
            void nonDecreasing() {
                // Arrange
                final Stream<Integer> input = Stream.of(1, 2, 3, 3, 2, 3);

                // Act
                final List<List<Integer>> output = input
                        .gather(Gatherers4j.groupNonDecreasing())
                        .toList();

                // Assert
                assertThat(output)
                        .containsExactly(
                                List.of(1, 2, 3, 3),
                                List.of(2, 3)
                        );
            }

            @Test
            void singleElementStream() {
                // Arrange
                final Stream<String> input = Stream.of("A");

                // Act
                final List<List<String>> output = input
                        .gather(Gatherers4j.groupNonDecreasing())
                        .toList();

                // Assert
                assertThat(output).containsExactly(List.of("A"));
            }
        }

        @Nested
        class NonIncreasing {
            @Test
            void emptyStream() {
                // Arrange
                final Stream<String> input = Stream.empty();

                // Act
                final List<List<String>> output = input
                        .gather(Gatherers4j.groupNonIncreasing())
                        .toList();

                // Assert
                assertThat(output).isEmpty();
            }

            @Test
            void ensureNonIncreasing() {
                // Arrange
                final Stream<Integer> input = Stream.of(4, 3, 2, 2);

                // Act
                final List<Integer> output = input.gather(Gatherers4j.ensureNonIncreasing()).toList();

                // Assert
                assertThat(output).containsExactly(4, 3, 2, 2);
            }

            @Test
            void ensureNonIncreasingFailureCase() {
                assertThatThrownBy(() ->
                        Stream.of(1, 2).gather(Gatherers4j.ensureNonIncreasing()).toList()
                ).isExactlyInstanceOf(IllegalStateException.class);
            }

            @Test
            void nonIncreasing() {
                // Arrange
                final Stream<Integer> input = Stream.of(3, 2, 1, 2, 2, 1);

                // Act
                final List<List<Integer>> output = input
                        .gather(Gatherers4j.groupNonIncreasing())
                        .toList();

                // Assert
                assertThat(output)
                        .containsExactly(
                                List.of(3, 2, 1),
                                List.of(2, 2, 1)
                        );
            }

            @Test
            void singleElementStream() {
                // Arrange
                final Stream<String> input = Stream.of("A");

                // Act
                final List<List<String>> output = input
                        .gather(Gatherers4j.groupNonIncreasing())
                        .toList();

                // Assert
                assertThat(output).containsExactly(List.of("A"));
            }

        }
    }

    @Nested
    class WithComparator {

        @Nested
        class Common {
            @SuppressWarnings("DataFlowIssue")
            @Test
            void comparatorMustNotBeNull() {
                assertThatThrownBy(() ->
                        new GroupChangingGatherer<>(ChangingOperation.Increasing, null)
                ).isExactlyInstanceOf(IllegalArgumentException.class);
            }

            @SuppressWarnings("DataFlowIssue")
            @Test
            void operationMustNotBeNull() {
                assertThatThrownBy(() ->
                        new GroupChangingGatherer<>(null, (_, _) -> 0)
                ).isExactlyInstanceOf(IllegalArgumentException.class);
            }

            @Test
            void returnedListUnmodifiable() {
                // Arrange
                final Stream<String> input = Stream.of("A", "B", "C");

                // Act
                final List<List<String>> output = input
                        .gather(new GroupChangingGatherer<>(ChangingOperation.Increasing, Comparator.comparing(String::length)))
                        .toList();

                // Assert
                assertThat(output).hasSize(3);
                output.forEach(it ->
                        assertThatThrownBy(() ->
                                it.add("D")
                        ).isInstanceOf(UnsupportedOperationException.class)
                );
            }
        }

        @Nested
        class Decreasing {
            @Test
            void decreasing() {
                // Arrange
                final Stream<String> input = Stream.of("AAA", "AA", "A", "AA", "AA", "A");

                // Act
                final List<List<String>> output = input
                        .gather(Gatherers4j.groupDecreasing(Comparator.comparingInt(String::length)))
                        .toList();

                // Assert
                assertThat(output)
                        .containsExactly(
                                List.of("AAA", "AA", "A"),
                                List.of("AA"),
                                List.of("AA", "A")
                        );
            }

            @Test
            void emptyStream() {
                // Arrange
                final Stream<String> input = Stream.empty();

                // Act
                final List<List<String>> output = input
                        .gather(Gatherers4j.groupDecreasing(Comparator.comparingInt(String::length)))
                        .toList();

                // Assert
                assertThat(output).isEmpty();
            }

            @Test
            void ensureDecreasing() {
                // Arrange
                final Stream<String> input = Stream.of("AAA", "AA", "A");

                // Act
                final List<String> output = input
                        .gather(Gatherers4j.ensureDecreasing(Comparator.comparingInt(String::length)))
                        .toList();

                // Assert
                assertThat(output).containsExactly("AAA", "AA", "A");
            }

            @Test
            void ensureDecreasingFailureCase() {
                assertThatThrownBy(() ->
                        Stream.of("A", "AA")
                                .gather(Gatherers4j.ensureDecreasing(Comparator.comparingInt(String::length)))
                                .toList()
                ).isExactlyInstanceOf(IllegalStateException.class);
            }

            @Test
            void singleElementStream() {
                // Arrange
                final Stream<String> input = Stream.of("A");

                // Act
                final List<List<String>> output = input
                        .gather(Gatherers4j.groupDecreasing(Comparator.comparingInt(String::length)))
                        .toList();

                // Assert
                assertThat(output).containsExactly(List.of("A"));
            }
        }

        @Nested
        class Increasing {
            @Test
            void emptyStream() {
                // Arrange
                final Stream<String> input = Stream.empty();

                // Act
                final List<List<String>> output = input
                        .gather(Gatherers4j.groupIncreasing(Comparator.comparingInt(String::length)))
                        .toList();

                // Assert
                assertThat(output).isEmpty();
            }

            @Test
            void ensureIncreasing() {
                // Arrange
                final Stream<String> input = Stream.of("A", "AA", "AAA");

                // Act
                final List<String> output = input
                        .gather(Gatherers4j.ensureIncreasing(Comparator.comparingInt(String::length)))
                        .toList();

                // Assert
                assertThat(output).containsExactly("A", "AA", "AAA");
            }

            @Test
            void ensureIncreasingFailureCase() {
                assertThatThrownBy(() ->
                        Stream.of("AA", "A")
                                .gather(Gatherers4j.ensureIncreasing(Comparator.comparingInt(String::length)))
                                .toList()
                ).isExactlyInstanceOf(IllegalStateException.class);
            }

            @Test
            void increasing() {
                // Arrange
                final Stream<String> input = Stream.of("A", "AA", "AAA", "AAA", "AA", "AAA");

                // Act
                final List<List<String>> output = input
                        .gather(Gatherers4j.groupIncreasing(Comparator.comparingInt(String::length)))
                        .toList();

                // Assert
                assertThat(output)
                        .containsExactly(
                                List.of("A", "AA", "AAA"),
                                List.of("AAA"),
                                List.of("AA", "AAA")
                        );
            }

            @Test
            void singleElementStream() {
                // Arrange
                final Stream<String> input = Stream.of("A");

                // Act
                final List<List<String>> output = input
                        .gather(Gatherers4j.groupIncreasing(Comparator.comparingInt(String::length)))
                        .toList();

                // Assert
                assertThat(output).containsExactly(List.of("A"));
            }
        }

        @Nested
        class NonDecreasing {

            @Test
            void emptyStream() {
                // Arrange
                final Stream<String> input = Stream.empty();

                // Act
                final List<List<String>> output = input
                        .gather(Gatherers4j.groupNonDecreasing(Comparator.comparingInt(String::length)))
                        .toList();

                // Assert
                assertThat(output).isEmpty();
            }

            @Test
            void ensureNonDecreasing() {
                // Arrange
                final Stream<String> input = Stream.of("A", "A", "A");

                // Act
                final List<String> output = input
                        .gather(Gatherers4j.ensureNonDecreasing(Comparator.comparingInt(String::length)))
                        .toList();

                // Assert
                assertThat(output).containsExactly("A", "A", "A");
            }

            @Test
            void ensureNonDecreasingFailureCase() {
                assertThatThrownBy(() ->
                        Stream.of("AA", "A")
                                .gather(Gatherers4j.ensureNonDecreasing(Comparator.comparingInt(String::length)))
                                .toList()
                ).isExactlyInstanceOf(IllegalStateException.class);
            }

            @Test
            void nonDecreasing() {
                // Arrange
                final Stream<String> input = Stream.of("A", "AA", "AAA", "AAA", "AA", "AAA");

                // Act
                final List<List<String>> output = input
                        .gather(Gatherers4j.groupNonDecreasing(Comparator.comparingInt(String::length)))
                        .toList();

                // Assert
                assertThat(output)
                        .containsExactly(
                                List.of("A", "AA", "AAA", "AAA"),
                                List.of("AA", "AAA")
                        );
            }

            @Test
            void singleElementStream() {
                // Arrange
                final Stream<String> input = Stream.of("A");

                // Act
                final List<List<String>> output = input
                        .gather(Gatherers4j.groupNonDecreasing(Comparator.comparingInt(String::length)))
                        .toList();

                // Assert
                assertThat(output).containsExactly(List.of("A"));
            }
        }

        @Nested
        class NonIncreasing {
            @Test
            void emptyStream() {
                // Arrange
                final Stream<String> input = Stream.empty();

                // Act
                final List<List<String>> output = input
                        .gather(Gatherers4j.groupNonIncreasing(Comparator.comparingInt(String::length)))
                        .toList();

                // Assert
                assertThat(output).isEmpty();
            }

            @Test
            void ensureNonIncreasing() {
                // Arrange
                final Stream<String> input = Stream.of("A", "A", "A");

                // Act
                final List<String> output = input
                        .gather(Gatherers4j.ensureNonIncreasing(Comparator.comparingInt(String::length)))
                        .toList();

                // Assert
                assertThat(output).containsExactly("A", "A", "A");
            }

            @Test
            void ensureNonIncreasingFailureCase() {
                assertThatThrownBy(() ->
                        Stream.of("AA", "AAA")
                                .gather(Gatherers4j.ensureNonIncreasing(Comparator.comparingInt(String::length)))
                                .toList()
                ).isExactlyInstanceOf(IllegalStateException.class);
            }

            @Test
            void nonIncreasing() {
                // Arrange
                final Stream<String> input = Stream.of("AAA", "AA", "A", "AA", "AA", "A");

                // Act
                final List<List<String>> output = input
                        .gather(Gatherers4j.groupNonIncreasing(Comparator.comparingInt(String::length)))
                        .toList();

                // Assert
                assertThat(output)
                        .containsExactly(
                                List.of("AAA", "AA", "A"),
                                List.of("AA", "AA", "A")
                        );
            }

            @Test
            void singleElementStream() {
                // Arrange
                final Stream<String> input = Stream.of("A");

                // Act
                final List<List<String>> output = input
                        .gather(Gatherers4j.groupNonIncreasing(Comparator.comparingInt(String::length)))
                        .toList();

                // Assert
                assertThat(output).containsExactly(List.of("A"));
            }
        }
    }

}