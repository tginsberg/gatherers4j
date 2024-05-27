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

class DroppingGathererTest {

    @Test
    void dropSome() {
        // Arrange
        final Stream<String> input = Stream.of("A", "B", "C", "D", "E", "F");
        final int drop = 3;

        // Act
        final List<String> output = input
                .gather(Gatherers4j.drop(drop))
                .toList();

        // Assert
        assertThat(output).containsExactly("D", "E", "F");
    }

    @Test
    void dropNone() {
        // Arrange
        final Stream<String> input = Stream.of("A", "B", "C", "D", "E", "F");
        final int drop = 0;

        // Act
        final List<String> output = input
                .gather(Gatherers4j.drop(drop))
                .toList();

        // Assert
        assertThat(output).containsExactly("A", "B", "C", "D", "E", "F");
    }

    @Test
    void dropCountNegative() {
        // Arrange
        final Stream<String> input = Stream.empty();
        final int drop = -1;

        // Act/Assert
        assertThatThrownBy(() ->
                input.gather(Gatherers4j.drop(drop))
        ).isExactlyInstanceOf(IllegalArgumentException.class);
    }

}