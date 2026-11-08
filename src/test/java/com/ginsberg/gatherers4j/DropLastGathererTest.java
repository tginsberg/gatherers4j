/*
 * Copyright 2024-2026 Todd Ginsberg
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
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class DropLastGathererTest {

    @Test
    void countMustNotBeNegative() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> Gatherers4j.dropLast(-1))
            .withMessage("DropLast count must not be negative");
    }

    @Test
    void dropLast() {
        // Arrange
        final Stream<String> input = Stream.of("A", "B", "C");

        // Act
        final List<String> output = input.gather(Gatherers4j.dropLast(2)).toList();

        // Assert
        assertThat(output).containsExactly("A");
    }

    @Test
    void dropLastZero() {
        // Arrange
        final Stream<String> input = Stream.of("A", "B", "C");

        // Act
        final List<String> output = input.gather(Gatherers4j.dropLast(0)).toList();

        // Assert
        assertThat(output).containsExactly("A", "B", "C");
    }

    @Test
    void dropLastNulls() {
        // Arrange
        final Stream<String> input = Stream.of("A", null, null);

        // Act
        final List<String> output = input.gather(Gatherers4j.dropLast(2)).toList();

        // Assert
        assertThat(output).containsExactly("A");
    }

    @Test
    void dropLastLongerThanStreamReturnsEmpty() {
        // Arrange
        final Stream<String> input = Stream.of("A", "B", "C");

        // Act
        final List<String> output = input.gather(Gatherers4j.dropLast(4)).toList();

        // Assert
        assertThat(output).isEmpty();
    }

}
