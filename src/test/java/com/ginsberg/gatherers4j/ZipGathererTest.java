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

class ZipGathererTest {

    @Test
    void zipGatherer() {
        // Arrange
        final Stream<String> left = Stream.of("A", "B", "C");
        final Stream<Integer> right = Stream.of(1, 2, 3);

        // Act
        final List<Pair<String, Integer>> output = left
                .gather(Gatherers4j.zip(right))
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
    void zipGathererOtherEmpty() {
        // Arrange
        final Stream<String> left = Stream.of("A", "B", "C");
        final Stream<Integer> right = Stream.empty();

        // Act
        final List<Pair<String, Integer>> output = left
                .gather(Gatherers4j.zip(right))
                .toList();

        // Assert
        assertThat(output).isEmpty();
    }

    @Test
    void interleavingGathererThisEmpty() {
        // Arrange
        final Stream<String> left = Stream.empty();
        final Stream<Integer> right = Stream.of(1, 2, 3);

        // Act
        final List<Pair<String, Integer>> output = left
                .gather(Gatherers4j.zip(right))
                .toList();

        // Assert
        assertThat(output).isEmpty();
    }
}