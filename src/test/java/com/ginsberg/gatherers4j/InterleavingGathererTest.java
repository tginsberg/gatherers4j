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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InterleavingGathererTest {

    @Test
    void argumentIterableMustNotBeNull() {
        assertThatThrownBy(() -> Stream.of("A")
                .gather(Gatherers4j.interleave((Iterable<String>) null))
                .toList()
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void argumentIteratorMustNotBeNull() {
        assertThatThrownBy(() -> Stream.of("A")
                .gather(Gatherers4j.interleave((Iterator<String>) null))
                .toList()
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void argumentStreamMustNotBeNull() {
        assertThatThrownBy(() -> Stream.of("A")
                .gather(Gatherers4j.interleave((Stream<String>) null))
                .toList()
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void interleavingGathererIterable() {
        // Arrange
        final Stream<String> left = Stream.of("A", "B", "C");
        final Collection<String> right = List.of("D", "E", "F");

        // Act
        final List<String> output = left
                .gather(Gatherers4j.interleave(right))
                .toList();

        // Assert
        assertThat(output)
                .containsExactly("A", "D", "B", "E", "C", "F");
    }

    @Test
    void interleavingGathererIterator() {
        // Arrange
        final Stream<String> left = Stream.of("A", "B", "C");
        final Iterator<String> right = List.of("D", "E", "F").iterator();

        // Act
        final List<String> output = left
                .gather(Gatherers4j.interleave(right))
                .toList();

        // Assert
        assertThat(output)
                .containsExactly("A", "D", "B", "E", "C", "F");
    }

    @Test
    void interleavingGathererOtherEmpty() {
        // Arrange
        final Stream<String> left = Stream.of("A", "B", "C");
        final Stream<String> right = Stream.empty();

        // Act
        final List<String> output = left
                .gather(Gatherers4j.interleave(right))
                .toList();

        // Assert
        assertThat(output)
                .containsExactly("A");
    }

    @Test
    void interleavingGathererStream() {
        // Arrange
        final Stream<String> left = Stream.of("A", "B", "C");
        final Stream<String> right = Stream.of("D", "E", "F");

        // Act
        final List<String> output = left
                .gather(Gatherers4j.interleave(right))
                .toList();

        // Assert
        assertThat(output)
                .containsExactly("A", "D", "B", "E", "C", "F");
    }

    @Test
    void interleavingGathererThisEmpty() {
        // Arrange
        final Stream<String> left = Stream.empty();
        final Stream<String> right = Stream.of("A", "B", "C");

        // Act
        final List<String> output = left
                .gather(Gatherers4j.interleave(right))
                .toList();

        // Assert
        assertThat(output).isEmpty();
    }
}