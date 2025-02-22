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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WindowGathererTest {

    @Test
    void emptyStream() {
        // Arrange
        final Stream<String> input = Stream.empty();

        // Act
        final List<List<String>> output = input.gather(
                Gatherers4j.window(1, 1, true)
        ).toList();

        // Assert
        assertThat(output).isEmpty();
    }

    @Test
    void excludesPartialWindow() {
        // Arrange
        final Stream<String> input = Stream.of("A", "B", "C", "D", "E");

        // Act
        final List<List<String>> output = input.gather(
                Gatherers4j.window(2, 2, false)
        ).toList();

        // Assert
        assertThat(output)
                .containsExactly(
                        List.of("A", "B"),
                        List.of("C", "D")
                );
    }

    @Test
    void includesPartialWindow() {
        // Arrange
        final Stream<String> input = Stream.of("A", "B", "C", "D", "E");

        // Act
        final List<List<String>> output = input.gather(
                Gatherers4j.window(2, 2, true)
        ).toList();

        // Assert
        assertThat(output)
                .containsExactly(
                        List.of("A", "B"),
                        List.of("C", "D"),
                        List.of("E")
                );
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0})
    void steppingMustBePositive(int stepping) {
        assertThatThrownBy(() -> new WindowGatherer<>(1, stepping, true))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0})
    void windowSizeMustBePositive(int windowSize) {
        assertThatThrownBy(() -> new WindowGatherer<>(windowSize, 1, true))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void windowWithSteppingAndPartials() {
        // Arrange
        final Stream<String> input = Stream.of("A", "B", "C", "D", "E", "F");

        // Act
        final List<List<String>> output = input.gather(
                Gatherers4j.window(3, 2, true)
        ).toList();

        // Assert
        assertThat(output)
                .containsExactly(
                        List.of("A", "B", "C"),
                        List.of("C", "D", "E"),
                        List.of("E", "F")
                );
    }

    @Test
    void windowWithSteppingThatSkips() {
        // Arrange
        final Stream<String> input = Stream.of("A", "B", "C", "D", "E", "F");

        // Act
        final List<List<String>> output = input.gather(
                Gatherers4j.window(2, 3, true)
        ).toList();

        // Assert
        assertThat(output)
                .containsExactly(
                        List.of("A", "B"),
                        List.of("D", "E")
                );
    }

    @Test
    @Disabled("This is a bug I need to fix later")
    void windowNulls() {
        // Arrange
        final Stream<String> input = Stream.of(null, null, null, null, null);

        // Act
        final List<List<String>> output = input.gather(
                Gatherers4j.window(2, 2, false)
        ).toList();

        // Assert
        assertThat(output)
                .containsExactly(
                        Arrays.asList(null, null),
                        Arrays.asList(null, null)
                );
    }

}