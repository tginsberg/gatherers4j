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

class DistinctGathererTest {

    @Test
    void distinctBy() {
        // Arrange
        final Stream<String> input = Stream.of("A", "a", "b", "B", "C", "c");

        // Act
        final List<String> output = input
                .gather(Gatherers4j.distinctBy(String::toUpperCase))
                .toList();

        // Assert
        assertThat(output).containsExactly("A", "b", "C");
    }

    @Test
    void distinctByWithNull() {
        // Arrange
        final Stream<String> input = Stream.of(null, "a", null);

        // Act
        final List<String> output = input
                .gather(Gatherers4j.distinctBy(it -> it == null ? null : it.toUpperCase()))
                .toList();

        // Assert
        assertThat(output).containsExactly(null, "a");
    }
}