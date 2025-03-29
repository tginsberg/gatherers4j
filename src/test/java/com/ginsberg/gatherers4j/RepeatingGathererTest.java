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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RepeatingGathererTest {

    @ParameterizedTest(name = "With {0} repeats")
    @ValueSource(ints = {2, 30, 100})
    void finiteRepeat(final int repeats) {
        // Arrange
        final Stream<String> input = Stream.of("A", "B", "C");

        // Act
        final List<String> output = input.gather(Gatherers4j.repeat(repeats)).toList();

        // Assert
        assertThat(output).hasSize(3 * repeats);
        for (int i = 0; i < output.size(); i = i + 3) {
            assertThat(output.get(i)).isEqualTo("A");
            assertThat(output.get(i + 1)).isEqualTo("B");
            assertThat(output.get(i + 2)).isEqualTo("C");
        }
    }

    @Test
    void flatMapIntegration() {
        // Arrange
        final Stream<Integer> input = Stream.of(0, 1, 2, 3);

        // Act
        final List<Integer> output = input
                .flatMap(n -> Stream.of(n).gather(Gatherers4j.repeat(n)))
                .toList();

        // Assert
        assertThat(output).isEqualTo(List.of(1, 2, 2, 3, 3, 3));
    }

    @Test
    void infiniteRepeats() {
        // Arrange
        final Stream<String> input = Stream.of("A", "B", "C");

        // Act
        final List<String> output = input.gather(Gatherers4j.repeatInfinitely()).limit(1_000).toList();

        // Assert
        assertThat(output).hasSize(1_000);
        for (int i = 0; i < output.size() - 3; i = i + 3) {
            assertThat(output.get(i)).isEqualTo("A");
            assertThat(output.get(i + 1)).isEqualTo("B");
            assertThat(output.get(i + 2)).isEqualTo("C");
        }
    }

    @ParameterizedTest(name = "With {0} repeats")
    @ValueSource(ints = {Integer.MIN_VALUE, -1})
    void numberOfRepeatsMustBeNegative(int repeats) {
        assertThatThrownBy(() ->
                RepeatingGatherer.ofFinite(repeats)
        ).isExactlyInstanceOf(IllegalArgumentException.class);
    }


}