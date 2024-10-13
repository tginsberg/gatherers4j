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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class MinMaxGathererTest {

    private record TestObject(String a, int b) {
    }

    @Nested
    class Max {

        @Test
        void doesNotTestNullMappings() {
            // Arrange
            final Stream<TestObject> input = Stream.of(new TestObject(null, 1));

            // Act
            final List<TestObject> output = input.gather(Gatherers4j.maxBy(TestObject::a)).toList();

            // Assert
            assertThat(output).isEmpty();
        }

        @Test
        void emptyStreamHasNoBest() {
            // Arrange
            final Stream<TestObject> input = Stream.empty();

            // Act
            final List<TestObject> output = input.gather(Gatherers4j.maxBy(TestObject::b)).toList();

            // Assert
            assertThat(output).isEmpty();
        }

        @Test
        void firstValueSelectedWhenMultipleExist() {
            // Arrange
            final Stream<TestObject> input = Stream.of(
                    new TestObject("A", 1),
                    new TestObject("B", 1)
            );

            // Act
            final List<TestObject> output = input.gather(Gatherers4j.maxBy(TestObject::b)).toList();

            // Assert
            assertThat(output).containsExactly(new TestObject("A", 1));
        }

        @Test
        void maxValue() {
            // Arrange
            final Stream<TestObject> input = Stream.of(
                    new TestObject("A", 1),
                    new TestObject("B", 2),
                    new TestObject("C", 3),
                    new TestObject("E", 2),
                    new TestObject("E", 1)
            );

            // Act
            final List<TestObject> output = input.gather(Gatherers4j.maxBy(TestObject::b)).toList();

            // Assert
            assertThat(output).containsExactly(new TestObject("C", 3));
        }
    }

    @Nested
    class Min {

        @Test
        void doesNotTestNullMappings() {
            // Arrange
            final Stream<TestObject> input = Stream.of(new TestObject(null, 1));

            // Act
            final List<TestObject> output = input.gather(Gatherers4j.minBy(TestObject::a)).toList();

            // Assert
            assertThat(output).isEmpty();
        }

        @Test
        void emptyStreamHasNoBest() {
            // Arrange
            final Stream<TestObject> input = Stream.empty();

            // Act
            final List<TestObject> output = input.gather(Gatherers4j.minBy(TestObject::b)).toList();

            // Assert
            assertThat(output).isEmpty();
        }

        @Test
        void firstValueSelectedWhenMultipleExist() {
            // Arrange
            final Stream<TestObject> input = Stream.of(
                    new TestObject("A", 1),
                    new TestObject("B", 1)
            );

            // Act
            final List<TestObject> output = input.gather(Gatherers4j.minBy(TestObject::b)).toList();

            // Assert
            assertThat(output).containsExactly(new TestObject("A", 1));
        }

        @Test
        void minValue() {
            // Arrange
            final Stream<TestObject> input = Stream.of(
                    new TestObject("A", 3),
                    new TestObject("B", 2),
                    new TestObject("C", 1),
                    new TestObject("E", 2),
                    new TestObject("E", 3)
            );

            // Act
            final List<TestObject> output = input.gather(Gatherers4j.minBy(TestObject::b)).toList();

            // Assert
            assertThat(output).containsExactly(new TestObject("C", 1));
        }
    }

}