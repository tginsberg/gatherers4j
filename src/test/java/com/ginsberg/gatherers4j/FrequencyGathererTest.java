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

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FrequencyGathererTest {

    @Test
    void ascending() {
        // Arrange
        final Stream<String> input = Stream.of("A", "A", "A", "B", "B", "B", "B", "C", "C");

        // Act
        final List<WithCount<String>> output = input.gather(Gatherers4j.orderByFrequencyAscending()).toList();

        // Assert
        assertThat(output)
                .containsExactly(
                        new WithCount<>("C", 2),
                        new WithCount<>("A", 3),
                        new WithCount<>("B", 4)
                );
    }

    @Test
    void ascendingParallel() {
        // Arrange
        final Stream<String> input = Stream.of("A", "A", "A", "B", "B", "B", "B", "C", "C");

        // Act
        final List<WithCount<String>> output = input.parallel().gather(Gatherers4j.orderByFrequencyAscending()).toList();

        // Assert
        assertThat(output)
                .containsExactly(
                        new WithCount<>("C", 2),
                        new WithCount<>("A", 3),
                        new WithCount<>("B", 4)
                );
    }

    @Test
    void descending() {
        // Arrange
        final Stream<String> input = Stream.of("A", "A", "A", "B", "B", "B", "B", "C", "C");

        // Act
        final List<WithCount<String>> output = input.gather(Gatherers4j.orderByFrequencyDescending()).toList();

        // Assert
        assertThat(output)
                .containsExactly(
                        new WithCount<>("B", 4),
                        new WithCount<>("A", 3),
                        new WithCount<>("C", 2)
                );
    }

    @Test
    void descendingParallel() {
        // Arrange
        final Stream<String> input = Stream.of("A", "A", "A", "B", "B", "B", "B", "C", "C");

        // Act
        final List<WithCount<String>> output = input.parallel().gather(Gatherers4j.orderByFrequencyDescending()).toList();

        // Assert
        assertThat(output)
                .containsExactly(
                        new WithCount<>("B", 4),
                        new WithCount<>("A", 3),
                        new WithCount<>("C", 2)
                );
    }

    @Test
    void orderMustBeSpecified() {
        assertThatThrownBy(() ->
                Stream.of("A").gather(new FrequencyGatherer<>(null)).toList()
        ).isInstanceOf(IllegalArgumentException.class);
    }

}