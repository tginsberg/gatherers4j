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

class ConcatenationGathererTest {

    @Test
    void additionalStreamCannotBeNull() {
        assertThatThrownBy(() ->
                Stream.of("A").gather(Gatherers4j.concat(Stream.of("1")).thenConcat(null))
        ).isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void allowsEmptyConcat() {
        // Arrange
        final Stream<String> input1 = Stream.of("A", "B", "C");
        final Stream<String> input2 = Stream.empty();

        // Act
        final List<String> output = input1.gather(Gatherers4j.concat(input2)).toList();

        // Assert
        assertThat(output).containsExactly("A", "B", "C");
    }

    @Test
    void allowsEmptySource() {
        // Arrange
        final Stream<String> input1 = Stream.empty();
        final Stream<String> input2 = Stream.of("D", "E", "F");

        // Act
        final List<String> output = input1.gather(Gatherers4j.concat(input2)).toList();

        // Assert
        assertThat(output).containsExactly("D", "E", "F");
    }

    @Test
    void initialStreamCannotBeNull() {
        assertThatThrownBy(() ->
                Stream.of("A").gather(Gatherers4j.concat(null))
        ).isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void multipleConcatenations() {
        // Arrange
        final Stream<String> input1 = Stream.of("A", "B", "C");
        final Stream<String> input2 = Stream.of("D", "E", "F");
        final Stream<String> input3 = Stream.of("G", "H", "I");

        // Act
        final List<String> output = input1.gather(Gatherers4j.concat(input2).thenConcat(input3)).toList();

        // Assert
        assertThat(output).containsExactly("A", "B", "C", "D", "E", "F", "G", "H", "I");
    }

    @Test
    void simpleConcatenation() {
        // Arrange
        final Stream<String> input1 = Stream.of("A", "B", "C");
        final Stream<String> input2 = Stream.of("D", "E", "F");

        // Act
        final List<String> output = input1.gather(Gatherers4j.concat(input2)).toList();

        // Assert
        assertThat(output).containsExactly("A", "B", "C", "D", "E", "F");
    }

}