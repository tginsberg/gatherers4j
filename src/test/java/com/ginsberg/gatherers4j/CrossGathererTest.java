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

import static com.ginsberg.gatherers4j.Gatherers4j.crossWith;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import com.ginsberg.gatherers4j.dto.Pair;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CrossGathererTest {

    @Nested
    class FromIterable {
        @Test
        @SuppressWarnings("DataFlowIssue")
        void crossIterableMustNotBeNull() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> crossWith((Iterable<String>) null))
                .withMessage("source list must not be null");
        }

        @Test
        @SuppressWarnings("DataFlowIssue")
        void crossesMultipleIterables() {
            // Arrange
            final Stream<String> input = Stream.of("A", "B", "C");
            final Iterable<Integer> cross1 = List.of(1, 2, 3);
            final Iterable<String> cross2 = List.of("X", "Y");

            // Act
            final List<String> output = input
                    .gather(Gatherers4j.crossWith(cross1))
                    .gather(Gatherers4j.crossWith(cross2))
                    .map(pair -> pair.first().first() + pair.first().second() + pair.second())
                    .toList();

            // Assert
            assertThat(output).containsExactly(
                    "A1X", "A1Y", "A2X", "A2Y", "A3X", "A3Y",
                    "B1X", "B1Y", "B2X", "B2Y", "B3X", "B3Y",
                    "C1X", "C1Y", "C2X", "C2Y", "C3X", "C3Y"
            );
        }

        @Test
        void crossesSingleIterable() {
            // Arrange
            final Stream<String> input = Stream.of("A", "B", "C");
            final Iterable<Integer> cross = List.of(1, 2, 3);

            // Act
            final List<Pair<String, Integer>> output = input
                    .gather(Gatherers4j.crossWith(cross))
                    .toList();

            // Assert
            assertThat(output).containsExactly(
                    new Pair<>("A", 1), new Pair<>("A", 2), new Pair<>("A", 3),
                    new Pair<>("B", 1), new Pair<>("B", 2), new Pair<>("B", 3),
                    new Pair<>("C", 1), new Pair<>("C", 2), new Pair<>("C", 3)
            );
        }

        @Test
        void emptyCrossIterable() {
            // Arrange
            final Stream<String> input = Stream.of("A", "B", "C");
            final Iterable<Integer> cross = emptyList();

            // Act
            final List<Pair<String, Integer>> output = input
                    .gather(Gatherers4j.crossWith(cross))
                    .toList();

            // Assert
            assertThat(output).isEmpty();
        }
    }

    @Nested
    class FromIterator {
        @Test
        @SuppressWarnings("DataFlowIssue")
        void crossIteratorMustNotBeNull() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> crossWith((Iterator<String>) null))
                .withMessage("source iterator must not be null");
        }

        @Test
        @SuppressWarnings("DataFlowIssue")
        void crossesMultipleStreams() {
            // Arrange
            final Stream<String> input = Stream.of("A", "B", "C");
            final Iterator<Integer> cross1 = List.of(1, 2, 3).iterator();
            final Iterator<String> cross2 = List.of("X", "Y").iterator();

            // Act
            final List<String> output = input
                    .gather(Gatherers4j.crossWith(cross1))
                    .gather(Gatherers4j.crossWith(cross2))
                    .map(pair -> pair.first().first() + pair.first().second() + pair.second())
                    .toList();

            // Assert
            assertThat(output).containsExactly(
                    "A1X", "A1Y", "A2X", "A2Y", "A3X", "A3Y",
                    "B1X", "B1Y", "B2X", "B2Y", "B3X", "B3Y",
                    "C1X", "C1Y", "C2X", "C2Y", "C3X", "C3Y"
            );
        }

        @Test
        void crossesSingleStream() {
            // Arrange
            final Stream<String> input = Stream.of("A", "B", "C");
            final Iterator<Integer> cross = List.of(1, 2, 3).iterator();

            // Act
            final List<Pair<String, Integer>> output = input
                    .gather(Gatherers4j.crossWith(cross))
                    .toList();

            // Assert
            assertThat(output).containsExactly(
                    new Pair<>("A", 1), new Pair<>("A", 2), new Pair<>("A", 3),
                    new Pair<>("B", 1), new Pair<>("B", 2), new Pair<>("B", 3),
                    new Pair<>("C", 1), new Pair<>("C", 2), new Pair<>("C", 3)
            );
        }

        @Test
        void emptyCrossStream() {
            // Arrange
            final Stream<String> input = Stream.of("A", "B", "C");
            final Iterator<Integer> cross = Collections.emptyIterator();

            // Act
            final List<Pair<String, Integer>> output = input
                    .gather(Gatherers4j.crossWith(cross))
                    .toList();

            // Assert
            assertThat(output).isEmpty();
        }
    }

    @Nested
    class FromStream {
        @Test
        @SuppressWarnings("DataFlowIssue")
        void crossStreamMustNotBeNull() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> crossWith((Stream<String>) null))
                .withMessage("source stream must not be null");
        }

        @Test
        @SuppressWarnings("DataFlowIssue")
        void crossesMultipleStreams() {
            // Arrange
            final Stream<String> input = Stream.of("A", "B", "C");
            final Stream<Integer> cross1 = Stream.of(1, 2, 3);
            final Stream<String> cross2 = Stream.of("X", "Y");

            // Act
            final List<String> output = input
                    .gather(Gatherers4j.crossWith(cross1))
                    .gather(Gatherers4j.crossWith(cross2))
                    .map(pair -> pair.first().first() + pair.first().second() + pair.second())
                    .toList();

            // Assert
            assertThat(output).containsExactly(
                    "A1X", "A1Y", "A2X", "A2Y", "A3X", "A3Y",
                    "B1X", "B1Y", "B2X", "B2Y", "B3X", "B3Y",
                    "C1X", "C1Y", "C2X", "C2Y", "C3X", "C3Y"
            );
        }

        @Test
        void crossesSingleStream() {
            // Arrange
            final Stream<String> input = Stream.of("A", "B", "C");
            final Stream<Integer> cross = Stream.of(1, 2, 3);

            // Act
            final List<Pair<String, Integer>> output = input
                    .gather(Gatherers4j.crossWith(cross))
                    .toList();

            // Assert
            assertThat(output).containsExactly(
                    new Pair<>("A", 1), new Pair<>("A", 2), new Pair<>("A", 3),
                    new Pair<>("B", 1), new Pair<>("B", 2), new Pair<>("B", 3),
                    new Pair<>("C", 1), new Pair<>("C", 2), new Pair<>("C", 3)
            );
        }

        @Test
        void emptyCrossStream() {
            // Arrange
            final Stream<String> input = Stream.of("A", "B", "C");
            final Stream<Integer> cross = Stream.empty();

            // Act
            final List<Pair<String, Integer>> output = input
                    .gather(Gatherers4j.crossWith(cross))
                    .toList();

            // Assert
            assertThat(output).isEmpty();
        }
    }


    @Nested
    class FromVarArgs {
        @Test
        @SuppressWarnings("DataFlowIssue")
        void crossVarargsNotBeNull() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> crossWith((String[]) null))
                .withMessage("source must not be null");
        }

        @Test
        @SuppressWarnings("DataFlowIssue")
        void crossesMultipleVarargs() {
            // Arrange
            final Stream<String> input = Stream.of("A", "B", "C");

            // Act
            final List<String> output = input
                    .gather(Gatherers4j.crossWith(1, 2, 3))
                    .gather(Gatherers4j.crossWith("X", "Y"))
                    .map(pair -> pair.first().first() + pair.first().second() + pair.second())
                    .toList();

            // Assert
            assertThat(output).containsExactly(
                    "A1X", "A1Y", "A2X", "A2Y", "A3X", "A3Y",
                    "B1X", "B1Y", "B2X", "B2Y", "B3X", "B3Y",
                    "C1X", "C1Y", "C2X", "C2Y", "C3X", "C3Y"
            );
        }

        @Test
        void crossesSingleVararg() {
            // Arrange
            final Stream<String> input = Stream.of("A", "B", "C");

            // Act
            final List<Pair<String, Integer>> output = input
                    .gather(Gatherers4j.crossWith(1, 2, 3))
                    .toList();

            // Assert
            assertThat(output).containsExactly(
                    new Pair<>("A", 1), new Pair<>("A", 2), new Pair<>("A", 3),
                    new Pair<>("B", 1), new Pair<>("B", 2), new Pair<>("B", 3),
                    new Pair<>("C", 1), new Pair<>("C", 2), new Pair<>("C", 3)
            );
        }

        @Test
        void emptyCrossVararg() {
            // Arrange
            final Stream<String> input = Stream.of("A", "B", "C");
            final Integer[] cross = new Integer[] {};

            // Act
            final List<Pair<String, Integer>> output = input
                    .gather(Gatherers4j.crossWith(cross))
                    .toList();

            // Assert
            assertThat(output).isEmpty();
        }
    }
}
