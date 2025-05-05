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

import com.ginsberg.gatherers4j.enums.Rotate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class RotateGathererTest {

    @Nested
    class RotateLeft {

        @Test
        void rotateEmpty() {
            // Arrange
            final Stream<String> input = Stream.empty();

            // Act
            final List<String> output = input
                    .gather(Gatherers4j.rotate(Rotate.Left, 1))
                    .toList();

            // Assert
            assertThat(output).isEmpty();
        }

        @ParameterizedTest(name = "Left Rotate {0}")
        @MethodSource(value = "rotateLeftArguments")
        void rotateLeft(final int rotateSize, final List<String> expected) {
            // Arrange
            final Stream<String> input = Stream.of("A", "B", "C", "D", "E");

            // Act
            final List<String> output = input
                    .gather(Gatherers4j.rotate(Rotate.Left,rotateSize))
                    .toList();

            // Assert
            assertThat(output).isEqualTo(expected);
        }

        private static Stream<Arguments> rotateLeftArguments() {
            return Stream.of(
                    Arguments.arguments(1, List.of("B", "C", "D", "E", "A")),
                    Arguments.arguments(2, List.of("C", "D", "E", "A", "B")),
                    Arguments.arguments(3, List.of("D", "E", "A", "B", "C")),
                    Arguments.arguments(4, List.of("E", "A", "B", "C", "D")),
                    Arguments.arguments(5, List.of("A", "B", "C", "D", "E")),
                    Arguments.arguments(6, List.of("B", "C", "D", "E", "A")),
                    Arguments.arguments(-1, List.of("E", "A", "B", "C", "D")),
                    Arguments.arguments(-2, List.of("D", "E", "A", "B", "C")),
                    Arguments.arguments(-3, List.of("C", "D", "E", "A", "B")),
                    Arguments.arguments(-4, List.of("B", "C", "D", "E", "A")),
                    Arguments.arguments(-5, List.of("A", "B", "C", "D", "E")),
                    Arguments.arguments(-6, List.of("E", "A", "B", "C", "D"))
            );
        }

        @Test
        void rotateZero() {
            // Arrange
            final List<String> input = List.of("A", "B", "C", "D", "E");

            // Act
            final List<String> output = input.stream()
                    .gather(Gatherers4j.rotate(Rotate.Left,0))
                    .toList();

            // Assert
            assertThat(output).isEqualTo(input);
        }
    }

    @Nested
    class RotateRight {

        private static Stream<Arguments> rotateRightArguments() {
            return Stream.of(
                    Arguments.arguments(1, List.of("E", "A", "B", "C", "D")),
                    Arguments.arguments(2, List.of("D", "E", "A", "B", "C")),
                    Arguments.arguments(3, List.of("C", "D", "E", "A", "B")),
                    Arguments.arguments(4, List.of("B", "C", "D", "E", "A")),
                    Arguments.arguments(5, List.of("A", "B", "C", "D", "E")),
                    Arguments.arguments(-1, List.of("B", "C", "D", "E", "A")),
                    Arguments.arguments(-2, List.of("C", "D", "E", "A", "B")),
                    Arguments.arguments(-3, List.of("D", "E", "A", "B", "C")),
                    Arguments.arguments(-4, List.of("E", "A", "B", "C", "D")),
                    Arguments.arguments(-5, List.of("A", "B", "C", "D", "E"))
            );
        }

        @Test
        void rotateEmpty() {
            // Arrange
            final Stream<String> input = Stream.empty();

            // Act
            final List<String> output = input
                    .gather(Gatherers4j.rotate(Rotate.Right, 1))
                    .toList();

            // Assert
            assertThat(output).isEmpty();
        }

        @ParameterizedTest(name = "Right Rotate {0}")
        @MethodSource(value = "rotateRightArguments")
        @DisplayName("Rotate Right")
        void rotateRight(final int rotateSize, final List<String> expected) {
            // Arrange
            final Stream<String> input = Stream.of("A", "B", "C", "D", "E");

            // Act
            final List<String> output = input
                    .gather(Gatherers4j.rotate(Rotate.Right, rotateSize))
                    .toList();

            // Assert
            assertThat(output).isEqualTo(expected);
        }

        @Test
        void rotateZero() {
            // Arrange
            final List<String> input = List.of("A", "B", "C", "D", "E");

            // Act
            final List<String> output = input.stream()
                    .gather(Gatherers4j.rotate(Rotate.Right, 0))
                    .toList();

            // Assert
            assertThat(output).isEqualTo(input);
        }
    }

}