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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GroupingByGathererTest {

    @Test
    void emptyStream() {
        // Arrange
        final Stream<String> input = Stream.empty();

        // Act
        final List<List<String>> output = input.gather(Gatherers4j.groupingBy(String::length)).toList();

        // Assert
        assertThat(output).isEmpty();
    }

    @Test
    void groupingByFunction() {
        // Arrange
        final Stream<String> input = Stream.of("A", "B", "AA", "BB", "CCC", "A", "BB", "CCC");

        // Act
        final List<List<String>> output = input.gather(Gatherers4j.groupingBy(String::length)).toList();

        // Assert
        assertThat(output).containsExactly(
                List.of("A", "B"),
                List.of("AA", "BB"),
                List.of("CCC"),
                List.of("A"),
                List.of("BB"),
                List.of("CCC")
        );
    }

    @Test
    void groupingByIdentity() {
        // Arrange
        final Stream<String> input = Stream.of("A", "A", "B", "B", "C", "C", "C");

        // Act
        final List<List<String>> output = input.gather(Gatherers4j.grouping()).toList();

        // Assert
        assertThat(output).containsExactly(
                List.of("A", "A"),
                List.of("B", "B"),
                List.of("C", "C", "C")
        );
    }

    @Test
    void mappingFunctionMustNotBeNull() {
        assertThatThrownBy(() -> new GroupingByGatherer<>(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nullsMatch() {
        // Arrange
        final Stream<String> input = Stream.of(null, null, "A");

        // Act
        final List<List<String>> output = input
                .gather(Gatherers4j.groupingBy(it -> it == null ? null : it.length())).toList();

        // Assert
        assertThat(output)
                .containsExactly(
                        Arrays.asList(null, null),
                        List.of("A")
                );
    }

    @Test
    void singleElementStream() {
        // Arrange
        final Stream<String> input = Stream.of("A");

        // Act
        final List<List<String>> output = input.gather(Gatherers4j.groupingBy(String::length)).toList();

        // Assert
        assertThat(output).containsExactly(
                List.of("A")
        );
    }
}