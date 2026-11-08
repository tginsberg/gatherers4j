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
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;


class LastGathererTest {

    @Test
    void lastElements() {
        // Arrange
        final Stream<String> input = Stream.of("A", "B", "C", "D", "E", "F");
        final int last = 3;

        // Act
        final List<String> output = input
                .gather(Gatherers4j.takeLast(last))
                .toList();

        // Assert
        assertThat(output).containsExactly("D", "E", "F");
    }

    @Test
    void lastElementsNull() {
        // Arrange
        final Stream<String> input = Stream.of("A", "B", "C", "D", null, null);
        final int last = 3;

        // Act
        final List<String> output = input
                .gather(Gatherers4j.takeLast(last))
                .toList();

        // Assert
        assertThat(output).containsExactly("D", null, null);
    }

    @Test
    void lastElementsUnderflow() {
        // Arrange
        final Stream<String> input = Stream.of("A", "B", "C", "D", "E", "F");
        final int last = 30;

        // Act
        final List<String> output = input
                .gather(Gatherers4j.takeLast(last))
                .toList();

        // Assert
        assertThat(output).containsExactly("A", "B", "C", "D", "E", "F");
    }

    @Test
    void emptyInputStreamEmitsEmpty() {
        // Arrange / Act
        final List<Object> output = Stream.empty()
                .gather(Gatherers4j.takeLast(1))
                .toList();

        // Assert
        assertThat(output).isEmpty();
    }

    @Test
    void downstreamRejection() {
        // Arrange
        final Stream<String> input = Stream.of("A", "B", "C", "D", "E", "F");
        final int last = 2;

        // Act
        final List<String> output = input
                .gather(Gatherers4j.takeLast(last))
                .limit(1)
                .toList();

        // Assert
        assertThat(output).containsExactly("E");
    }

    @Test
    void takeNegativeNumberOfElements() {
        // Act/Assert
        assertThatIllegalArgumentException()
                .isThrownBy(() -> Gatherers4j.takeLast(-1))
                .withMessage("Last count must not be negative");
    }

    @Test
    void takeZero() {
        // Arrange
        final Stream<String> input = Stream.of("A", "B", "C", "D", "E", "F");

        // Act
        final List<String> output = input
                .gather(Gatherers4j.takeLast(0))
                .toList();

        // Assert
        assertThat(output).isEmpty();
    }

    @Test
    @Timeout(5)
    void takeZeroStopsReadingInfiniteStream() {
        // Arrange
        final Stream<Integer> input = Stream.iterate(0, i -> i + 1);

        // Act
        final List<Integer> output = input
                .gather(Gatherers4j.takeLast(0))
                .toList();

        // Assert
        assertThat(output).isEmpty();
    }
}
