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

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InterleavingGathererTest {

    @Nested
    class FromIterable {

        @Test
        void argumentIterableMustNotBeNull() {
            assertThatThrownBy(() -> Stream.of("A")
                    .gather(Gatherers4j.interleaveWith((Iterable<String>) null))
                    .toList()
            ).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void interleavingGathererIterable() {
            // Arrange
            final Stream<String> left = Stream.of("A", "B", "C");
            final Iterable<String> right = List.of("D", "E", "F");

            // Act
            final List<String> output = left
                    .gather(Gatherers4j.interleaveWith(right))
                    .toList();

            // Assert
            assertThat(output)
                    .containsExactly("A", "D", "B", "E", "C", "F");
        }

    }

    @Nested
    class FromIterator {

        @Test
        void argumentIteratorMustNotBeNull() {
            assertThatThrownBy(() -> Stream.of("A")
                    .gather(Gatherers4j.interleaveWith((Iterator<String>) null))
                    .toList()
            ).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void interleavingGathererIterator() {
            // Arrange
            final Stream<String> left = Stream.of("A", "B", "C");
            final Iterator<String> right = List.of("D", "E", "F").iterator();

            // Act
            final List<String> output = left
                    .gather(Gatherers4j.interleaveWith(right))
                    .toList();

            // Assert
            assertThat(output)
                    .containsExactly("A", "D", "B", "E", "C", "F");
        }

    }

    @Nested
    class FromStream {
        @Test
        void argumentStreamMustNotBeNull() {
            assertThatThrownBy(() -> Stream.of("A")
                    .gather(Gatherers4j.interleaveWith((Stream<String>) null))
                    .toList()
            ).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void interleaveArgumentLongerSpecifyingArgument() {
            final Stream<String> left = Stream.of("A", "B", "C");
            final Stream<String> right = Stream.of("D", "E", "F", "G", "H");

            // Act
            final List<String> output = left
                    .gather(Gatherers4j.interleaveWith(right).appendArgumentIfLonger())
                    .toList();

            // Assert
            assertThat(output)
                    .containsExactly("A", "D", "B", "E", "C", "F", "G", "H");
        }

        @Test
        void interleaveOtherLongerSpecifyingEither() {
            final Stream<String> left = Stream.of("A", "B", "C");
            final Stream<String> right = Stream.of("D", "E", "F", "G", "H");

            // Act
            final List<String> output = left
                    .gather(Gatherers4j.interleaveWith(right).appendLonger())
                    .toList();

            // Assert
            assertThat(output)
                    .containsExactly("A", "D", "B", "E", "C", "F", "G", "H");
        }

        @Test
        void interleaveSourceLongerSpecifyingSourceEither() {
            final Stream<String> left = Stream.of("A", "B", "C", "D", "E");
            final Stream<String> right = Stream.of("F", "G", "H");

            // Act
            final List<String> output = left
                    .gather(Gatherers4j.interleaveWith(right).appendSourceIfLonger())
                    .toList();

            // Assert
            assertThat(output)
                    .containsExactly("A", "F", "B", "G", "C", "H", "D", "E");
        }

        @Test
        void interleavingGathererOtherEmpty() {
            // Arrange
            final Stream<String> left = Stream.of("A", "B", "C");
            final Stream<String> right = Stream.empty();

            // Act
            final List<String> output = left
                    .gather(Gatherers4j.interleaveWith(right))
                    .toList();

            // Assert
            assertThat(output)
                    .containsExactly("A");
        }

        @Test
        void interleavingGathererStream() {
            // Arrange
            final Stream<String> left = Stream.of("A", "B", "C");
            final Stream<String> right = Stream.of("D", "E", "F");

            // Act
            final List<String> output = left
                    .gather(Gatherers4j.interleaveWith(right))
                    .toList();

            // Assert
            assertThat(output)
                    .containsExactly("A", "D", "B", "E", "C", "F");
        }

        @Test
        void interleavingGathererThisEmpty() {
            // Arrange
            final Stream<String> left = Stream.empty();
            final Stream<String> right = Stream.of("A", "B", "C");

            // Act
            final List<String> output = left
                    .gather(Gatherers4j.interleaveWith(right))
                    .toList();

            // Assert
            assertThat(output).isEmpty();
        }

    }

    @Nested
    class FromVarargs {

        @Test
        void argumentMustNotBeNull() {
            assertThatThrownBy(() -> Stream.of("A")
                    .gather(Gatherers4j.interleaveWith((String[]) null))
                    .toList()
            ).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void interleavingGathererVarargs() {
            // Arrange
            final Stream<String> left = Stream.of("A", "B", "C");

            // Act
            final List<String> output = left
                    .gather(Gatherers4j.interleaveWith("D", "E", "F"))
                    .toList();

            // Assert
            assertThat(output)
                    .containsExactly("A", "D", "B", "E", "C", "F");
        }

    }

}