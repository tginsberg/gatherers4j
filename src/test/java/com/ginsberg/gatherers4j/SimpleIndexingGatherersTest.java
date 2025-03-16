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

import com.ginsberg.gatherers4j.dto.WithIndex;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SimpleIndexingGatherersTest {

    @Nested
    class FilterIndexed {

        @Test
        void filterWithIndex() {
            // Arrange
            final Stream<String> input = Stream.of("A", "B", "C", "D");

            // Act
            final List<String> output = input
                    .gather(Gatherers4j.filterIndexed((index, element) ->
                            index % 2 == 0 || element.equals("D"))
                    )
                    .toList();

            // Assert
            assertThat(output).containsExactly("A", "C", "D");
        }

        @SuppressWarnings("DataFlowIssue")
        @Test
        void predicateMustNotBeNull() {
            assertThatThrownBy(() -> Stream.of("A").gather(Gatherers4j.filterIndexed(null)))
                    .isExactlyInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class MapIndexed {
        @SuppressWarnings("DataFlowIssue")
        @Test
        void mappingFunctionMustNotBeNull() {
            assertThatThrownBy(() ->
                    Gatherers4j.mapIndexed(null)
            ).isExactlyInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void mapIndexedEmpty() {
            // Arrange
            final Stream<String> input = Stream.empty();

            // Act
            final List<String> output = input.gather(Gatherers4j.mapIndexed((_, element) -> element)).toList();

            // Assert
            assertThat(output).isEmpty();
        }

        @Test
        void mapIndexed() {
            // Arrange
            final Stream<String> input = Stream.of("A", "B", "C");

            // Act
            final List<String> output = input.gather(Gatherers4j.mapIndexed((index, element) -> element + index)).toList();

            // Assert
            assertThat(output).containsExactly("A0", "B1", "C2");
        }

    }

    @Nested
    class PeekIndexed {
        @SuppressWarnings("DataFlowIssue")
        @Test
        void peekingFunctionMustNotBeNull() {
            assertThatThrownBy(() ->
                    Gatherers4j.peekIndexed(null)
            ).isExactlyInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void peekIndexedEmpty() {
            // Arrange
            final Stream<String> input = Stream.empty();
            final List<String> peeked = new ArrayList<>();

            // Act
            final List<String> output = input.gather(Gatherers4j.peekIndexed((index, element) -> peeked.add(element + index))).toList();

            // Assert
            assertThat(output).isEmpty();
            assertThat(peeked).isEmpty();
        }

        @Test
        void peekIndexed() {
            // Arrange
            final Stream<String> input = Stream.of("A", "B", "C");
            final List<String> peeked = new ArrayList<>();

            // Act
            final List<String> output = input.gather(Gatherers4j.peekIndexed((index, element) -> peeked.add(element + index))).toList();

            // Assert
            assertThat(output).containsExactly("A", "B", "C");
            assertThat(peeked).containsExactly("A0", "B1", "C2");
        }

    }

    @Nested
    class WithIndexes {
        @Test
        void objectWithIndex() {
            // Arrange
            final Stream<String> input = Stream.of("A", "B", "C");

            // Act
            final List<WithIndex<String>> output = input
                    .gather(Gatherers4j.withIndex())
                    .toList();

            // Assert
            assertThat(output)
                    .containsExactly(
                            new WithIndex<>(0, "A"),
                            new WithIndex<>(1, "B"),
                            new WithIndex<>(2, "C")
                    );
        }

        @Test
        void integerWithIndex() {
            // Arrange
            final Stream<Integer> input = Stream.of(1, 2, 3);

            // Act
            final List<WithIndex<Integer>> output = input
                    .gather(Gatherers4j.withIndex())
                    .toList();

            // Assert
            assertThat(output)
                    .containsExactly(
                            new WithIndex<>(0, 1),
                            new WithIndex<>(1, 2),
                            new WithIndex<>(2, 3)
                    );
        }
    }
}