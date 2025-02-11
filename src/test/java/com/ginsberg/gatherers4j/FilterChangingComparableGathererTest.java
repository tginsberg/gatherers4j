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

class FilterChangingComparableGathererTest {

    @Nested
    class Common {
        @SuppressWarnings("DataFlowIssue")
        @Test
        void operationMustNotBeNull() {
            assertThatThrownBy(() ->
                    new FilterChangingComparableGatherer<>(null)
            ).isExactlyInstanceOf(IllegalArgumentException.class);
        }

    }

    @Nested
    class Decreasing {
        @Test
        void decreasing() {
            // Arrange
            final Stream<Integer> input = Stream.of(3, 2, 2, 1);

            // Act
            final List<Integer> output = input
                    .gather(Gatherers4j.filterDecreasing())
                    .toList();

            // Assert
            assertThat(output).containsExactly(3, 2, 1);
        }

        @Test
        void emptyStream() {
            // Arrange
            final Stream<Integer> input = Stream.empty();

            // Act
            final List<Integer> output = input
                    .gather(Gatherers4j.filterDecreasing())
                    .toList();

            // Assert
            assertThat(output).isEmpty();
        }

        @Test
        void singleElementStream() {
            // Arrange
            final Stream<Integer> input = Stream.of(1);

            // Act
            final List<Integer> output = input
                    .gather(Gatherers4j.filterDecreasing())
                    .toList();

            // Assert
            assertThat(output).containsExactly(1);
        }
    }

    @Nested
    class Increasing {
        @Test
        void emptyStream() {
            // Arrange
            final Stream<Integer> input = Stream.empty();

            // Act
            final List<Integer> output = input
                    .gather(Gatherers4j.filterIncreasing())
                    .toList();

            // Assert
            assertThat(output).isEmpty();
        }

        @Test
        void increasing() {
            // Arrange
            final Stream<Integer> input = Stream.of(1, 2, 2, 3);

            // Act
            final List<Integer> output = input
                    .gather(Gatherers4j.filterIncreasing())
                    .toList();

            // Assert
            assertThat(output).containsExactly(1, 2, 3);
        }

        @Test
        void singleElementStream() {
            // Arrange
            final Stream<Integer> input = Stream.of(1);

            // Act
            final List<Integer> output = input
                    .gather(Gatherers4j.filterIncreasing())
                    .toList();

            // Assert
            assertThat(output).containsExactly(1);
        }
    }

    @Nested
    class NonDecreasing {
        @Test
        void emptyStream() {
            // Arrange
            final Stream<Integer> input = Stream.empty();

            // Act
            final List<Integer> output = input
                    .gather(Gatherers4j.filterNonDecreasing())
                    .toList();

            // Assert
            assertThat(output).isEmpty();
        }

        @Test
        void nonDecreasing() {
            // Arrange
            final Stream<Integer> input = Stream.of(1, 2, 2, 3, 2);

            // Act
            final List<Integer> output = input
                    .gather(Gatherers4j.filterNonDecreasing())
                    .toList();

            // Assert
            assertThat(output).containsExactly(1, 2, 2, 3);
        }

        @Test
        void singleElementStream() {
            // Arrange
            final Stream<Integer> input = Stream.of(1);

            // Act
            final List<Integer> output = input
                    .gather(Gatherers4j.filterNonDecreasing())
                    .toList();

            // Assert
            assertThat(output).containsExactly(1);
        }
    }

    @Nested
    class NonIncreasing {
        @Test
        void emptyStream() {
            // Arrange
            final Stream<Integer> input = Stream.empty();

            // Act
            final List<Integer> output = input
                    .gather(Gatherers4j.filterNonIncreasing())
                    .toList();

            // Assert
            assertThat(output).isEmpty();
        }

        @Test
        void nonIncreasing() {
            // Arrange
            final Stream<Integer> input = Stream.of(3, 2, 2, 1, 2);

            // Act
            final List<Integer> output = input
                    .gather(Gatherers4j.filterNonIncreasing())
                    .toList();

            // Assert
            assertThat(output).containsExactly(3, 2, 2, 1);
        }

        @Test
        void singleElementStream() {
            // Arrange
            final Stream<Integer> input = Stream.of(1);

            // Act
            final List<Integer> output = input
                    .gather(Gatherers4j.filterNonIncreasing())
                    .toList();

            // Assert
            assertThat(output).containsExactly(1);
        }
    }
}