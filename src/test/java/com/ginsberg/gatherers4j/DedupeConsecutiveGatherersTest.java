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

class DedupeConsecutiveGatherersTest {

    @Test
    void dedupeConsecutive() {
        // Arrange
        final Stream<String> input = Stream.of("A", "A", "A", "B", "B", "C", "C", "D", "A", "B", "C");

        // Act
        final List<String> output = input
                .gather(Gatherers4j.dedupeConsecutive())
                .toList();

        // Assert
        assertThat(output).containsExactly("A", "B", "C", "D", "A", "B", "C");
    }

    @Test
    void dedupeConsecutiveWithNulls() {
        // Arrange
        final Stream<String> input = Stream.of(null, null, "A", "A", null);

        // Act
        final List<String> output = input
                .gather(Gatherers4j.dedupeConsecutive())
                .toList();

        // Assert
        assertThat(output).containsExactly(null, "A", null);
    }

    @Test
    void dedupeConsecutiveEmpty() {
        // Act
        final List<Object> output = Stream.empty()
                .gather(Gatherers4j.dedupeConsecutive())
                .toList();

        // Assert
        assertThat(output).isEmpty();
    }

    @Test
    void dedupeConsecutiveBy() {
        record TestConsecutive(int left, String right) {
        }

        // Arrange
        final Stream<TestConsecutive> input = Stream.of(
                new TestConsecutive(1, "A"),
                new TestConsecutive(2, "A"),
                new TestConsecutive(3, "A"),
                new TestConsecutive(4, "B"),
                new TestConsecutive(5, "B"),
                new TestConsecutive(6, "C")
        );

        // Act
        final List<TestConsecutive> output = input
                .gather(Gatherers4j.dedupeConsecutiveBy(TestConsecutive::right))
                .toList();

        // Assert
        assertThat(output).containsExactly(
                new TestConsecutive(1, "A"),
                new TestConsecutive(4, "B"),
                new TestConsecutive(6, "C")
        );
    }
}