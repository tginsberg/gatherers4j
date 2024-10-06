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

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShufflingGathererTest {

    @Test
    void testKnownShuffle() {
        // Arrange
        final Stream<String> input = Stream.of("A", "B", "C", "D", "E", "F", "G");

        // Act
        final String output = input
                .gather(Gatherers4j.shuffle(new Random(42)))
                .collect(Collectors.joining());

        // Assert
        assertThat(output).isEqualTo("BDFAEGC");
    }

    @Test
    void testRandomShuffles() {
        // Arrange
        final List<String> input = List.of("A", "B", "C", "D", "E");

        // Act
        final Set<String> output = new HashSet<>(
                List.of(
                        input.stream().gather(Gatherers4j.shuffle()).collect(Collectors.joining()),
                        input.stream().gather(Gatherers4j.shuffle()).collect(Collectors.joining()),
                        input.stream().gather(Gatherers4j.shuffle()).collect(Collectors.joining()),
                        input.stream().gather(Gatherers4j.shuffle()).collect(Collectors.joining()),
                        input.stream().gather(Gatherers4j.shuffle()).collect(Collectors.joining())
                )
        );

        // Assert
        assertThat(output).hasSizeGreaterThan(1);
    }

    @Test
    void withNullRandomGenerator() {
        // Arrange
        final Stream<String> input = Stream.of("A");

        // Act/Assert
        assertThatThrownBy(() -> input.gather(Gatherers4j.shuffle(null)).toList())
                .isExactlyInstanceOf(IllegalArgumentException.class);
    }
}