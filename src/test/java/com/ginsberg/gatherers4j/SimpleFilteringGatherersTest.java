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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class SimpleFilteringGatherersTest {

    @Nested
    class FilterNotNull {

        @Test
        void allNullsShouldBeEmpty() {
            // Arrange
            final Stream<String> input = Stream.of(null, null, null);

            // Act
            final List<String> output = input.gather(Gatherers4j.filterNotNull()).toList();

            // Assert
            assertThat(output).isEmpty();
        }

        @Test
        void emptyShouldRemainEmpty() {
            // Arrange
            final Stream<String> input = Stream.empty();

            // Act
            final List<String> output = input.gather(Gatherers4j.filterNotNull()).toList();

            // Assert
            assertThat(output).isEmpty();
        }

        @Test
        void someNullsShouldBeRemoved() {
            // Arrange
            final Stream<String> input = Stream.of(null, "a", null, "b", "c", null);

            // Act
            final List<String> output = input.gather(Gatherers4j.filterNotNull()).toList();

            // Assert
            assertThat(output).containsExactly("a", "b", "c");
        }

        @Test
        void zeroNullsShouldRemainUnchanged() {
            // Arrange
            final Stream<String> input = Stream.of("a", "b", "c");

            // Act
            final List<String> output = input.gather(SimpleFilteringGatherers.filterNotNull()).toList();

            // Assert
            assertThat(output).containsExactly("a", "b", "c");
        }

    }

}