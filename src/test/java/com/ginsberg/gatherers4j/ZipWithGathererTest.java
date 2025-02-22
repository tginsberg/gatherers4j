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

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ZipWithGathererTest {

    @Test
    void argumentIterableMustNotBeNull() {
        assertThatThrownBy(() -> Stream.of("A")
                .gather(Gatherers4j.zipWith((Iterable<String>) null)).toList()
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void argumentIteratorMustNotBeNull() {
        assertThatThrownBy(() -> Stream.of("A")
                .gather(Gatherers4j.zipWith((Iterator<String>) null)).toList()
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void argumentStreamMustNotBeNull() {
        assertThatThrownBy(() -> Stream.of("A")
                .gather(Gatherers4j.zipWith((Stream<String>) null)).toList()
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void argumentVarargsMustNotBeNull() {
        assertThatThrownBy(() -> Stream.of("A")
                .gather(Gatherers4j.zipWith((String[]) null)).toList()
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void argumentWhenSourceLongerFunctionMustNotBeNull() {
        assertThatThrownBy(() -> Stream.of("A")
                .gather(Gatherers4j.zipWith(List.of("A")).argumentWhenSourceLonger(null)).toList()
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void sourceWhenArgumentLongerFunctionMustNotBeNull() {
        assertThatThrownBy(() -> Stream.of("A")
                .gather(Gatherers4j.zipWith(List.of("A")).sourceWhenArgumentLonger(null)).toList()
        ).isInstanceOf(IllegalArgumentException.class);
    }


    @Test
    void zipWhenArgumentIsLongerFromFunction() {
        // Arrange
        final Stream<String> left = Stream.of("A");
        final Stream<Integer> right = Stream.of(1, 2, 3, 4);

        // Act
        final List<Pair<String, Integer>> output = left
                .gather(Gatherers4j.<String, Integer>zipWith(right).sourceWhenArgumentLonger(String::valueOf))
                .toList();

        // Assert
        assertThat(output)
                .hasSize(4)
                .containsExactly(
                        new Pair<>("A", 1),
                        new Pair<>("2", 2),
                        new Pair<>("3", 3),
                        new Pair<>("4", 4)
                );
    }

    @Test
    void zipWhenArgumentIsLongerNull() {
        // Arrange
        final Stream<String> left = Stream.of("A");
        final Stream<Integer> right = Stream.of(1, 2, 3, 4);

        // Act
        final List<Pair<String, Integer>> output = left
                .gather(Gatherers4j.<String, Integer>zipWith(right).nullSourceWhenArgumentLonger())
                .toList();

        // Assert
        assertThat(output)
                .hasSize(4)
                .containsExactly(
                        new Pair<>("A", 1),
                        new Pair<>(null, 2),
                        new Pair<>(null, 3),
                        new Pair<>(null, 4)
                );
    }

    @Test
    void zipWhenOtherIsEmpty() {
        // Arrange
        final Stream<String> left = Stream.of("A", "B", "C");
        final Stream<Integer> right = Stream.empty();

        // Act
        final List<Pair<String, Integer>> output = left
                .gather(Gatherers4j.zipWith(right))
                .toList();

        // Assert
        assertThat(output).isEmpty();
    }

    @Test
    void zipWhenSourceIsLongerFromFunction() {
        // Arrange
        final Stream<String> left = Stream.of("A", "Bb", "Ccc", "Dddd");
        final Stream<Integer> right = Stream.of(1);

        // Act
        final List<Pair<String, Integer>> output = left
                .gather(Gatherers4j.<String, Integer>zipWith(right).argumentWhenSourceLonger(String::length))
                .toList();

        // Assert
        assertThat(output)
                .hasSize(4)
                .containsExactly(
                        new Pair<>("A", 1),
                        new Pair<>("Bb", 2),
                        new Pair<>("Ccc", 3),
                        new Pair<>("Dddd", 4)
                );
    }

    @Test
    void zipWhenSourceIsLongerNull() {
        // Arrange
        final Stream<String> left = Stream.of("A", "B", "C", "D");
        final Stream<Integer> right = Stream.of(1);

        // Act
        final List<Pair<String, Integer>> output = left
                .gather(Gatherers4j.<String, Integer>zipWith(right).nullArgumentWhenSourceLonger())
                .toList();

        // Assert
        assertThat(output)
                .hasSize(4)
                .containsExactly(
                        new Pair<>("A", 1),
                        new Pair<>("B", null),
                        new Pair<>("C", null),
                        new Pair<>("D", null)
                );
    }

    @Test
    void zipWhenThisIsEmpty() {
        // Arrange
        final Stream<String> left = Stream.empty();
        final Stream<Integer> right = Stream.of(1, 2, 3);

        // Act
        final List<Pair<String, Integer>> output = left
                .gather(Gatherers4j.zipWith(right))
                .toList();

        // Assert
        assertThat(output).isEmpty();
    }

    @Test
    void zipWithIterableGatherer() {
        // Arrange
        final Stream<String> left = Stream.of("A", "B", "C");
        final Iterable<Integer> right = List.of(1, 2, 3);

        // Act
        final List<Pair<String, Integer>> output = left
                .gather(Gatherers4j.zipWith(right))
                .toList();

        // Assert
        assertThat(output)
                .containsExactly(
                        new Pair<>("A", 1),
                        new Pair<>("B", 2),
                        new Pair<>("C", 3)
                );
    }

    @Test
    void zipWithIteratorGatherer() {
        // Arrange
        final Stream<String> left = Stream.of("A", "B", "C");
        final Iterator<Integer> right = List.of(1, 2, 3).iterator();

        // Act
        final List<Pair<String, Integer>> output = left
                .gather(Gatherers4j.zipWith(right))
                .toList();

        // Assert
        assertThat(output)
                .containsExactly(
                        new Pair<>("A", 1),
                        new Pair<>("B", 2),
                        new Pair<>("C", 3)
                );
    }

    @Test
    void zipWithStreamGatherer() {
        // Arrange
        final Stream<String> left = Stream.of("A", "B", "C");
        final Stream<Integer> right = Stream.of(1, 2, 3);

        // Act
        final List<Pair<String, Integer>> output = left
                .gather(Gatherers4j.zipWith(right))
                .toList();

        // Assert
        assertThat(output)
                .containsExactly(
                        new Pair<>("A", 1),
                        new Pair<>("B", 2),
                        new Pair<>("C", 3)
                );
    }

    @Test
    void zipWithVarargs() {
        // Arrange
        final Stream<String> left = Stream.of("A", "B", "C");

        // Act
        final List<Pair<String, Integer>> output = left
                .gather(Gatherers4j.zipWith(1, 2, 3))
                .toList();

        // Assert
        assertThat(output)
                .containsExactly(
                        new Pair<>("A", 1),
                        new Pair<>("B", 2),
                        new Pair<>("C", 3)
                );
    }
}