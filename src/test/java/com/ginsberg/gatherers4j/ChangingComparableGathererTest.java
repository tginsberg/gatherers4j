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

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChangingComparableGathererTest {

    @Nested
    class Common {
        @SuppressWarnings("DataFlowIssue")
        @Test
        void operationMustNotBeNull() {
            assertThatThrownBy(() ->
                    new ChangingComparableGatherer<>(null)
            ).isExactlyInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void returnedListUnmodifiable() {
            // Arrange
            final Stream<Integer> input = Stream.of(1, 1, 1);

            // Act
            final List<List<Integer>> output = input
                    .gather(new ChangingComparableGatherer<>(ChangingOperation.Increasing))
                    .toList();

            // Assert
            assertThat(output).hasSize(3);
            output.forEach(it ->
                    assertThatThrownBy(() ->
                            it.add(1)
                    ).isInstanceOf(UnsupportedOperationException.class)
            );
        }
    }

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