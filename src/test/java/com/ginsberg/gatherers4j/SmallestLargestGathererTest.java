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

import com.ginsberg.gatherers4j.test.ParallelAndSequentialTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Gatherer;
import java.util.stream.Stream;

import static com.ginsberg.gatherers4j.test.ParallelAndSequentialTest.NULL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class SmallestLargestGathererTest {

    private static final Comparator<String> BY_LENGTH = Comparator.comparingInt(String::length);

    @Nested
    class Largest {

        @ParallelAndSequentialTest(values = {"B", "E", "A", "D", "C"})
        void emitsLargestFirst(final Stream<String> input) {
            // Act
            final List<String> output = input.gather(Gatherers4j.largest(3)).toList();

            // Assert
            assertThat(output).containsExactly("E", "D", "C");
        }

        @ParallelAndSequentialTest
        void emptyStream(final Stream<String> input) {
            // Act
            final List<String> output = input.gather(Gatherers4j.largest(3)).toList();

            // Assert
            assertThat(output).isEmpty();
        }

        @ParallelAndSequentialTest(values = {"B", "A"})
        void fewerElementsThanCountEmitsAll(final Stream<String> input) {
            // Act
            final List<String> output = input.gather(Gatherers4j.largest(3)).toList();

            // Assert
            assertThat(output).containsExactly("B", "A");
        }

        @ParallelAndSequentialTest(values = {"B", NULL, "A", NULL, "C"})
        void ignoresNulls(final Stream<String> input) {
            // Act
            final List<String> output = input.gather(Gatherers4j.largest(5)).toList();

            // Assert
            assertThat(output).containsExactly("C", "B", "A");
        }

        @ParallelAndSequentialTest(values = {NULL, NULL})
        void onlyNullsEmitsNothing(final Stream<String> input) {
            // Act
            final List<String> output = input.gather(Gatherers4j.largest(2)).toList();

            // Assert
            assertThat(output).isEmpty();
        }

        @ParallelAndSequentialTest(values = "A")
        void singleElement(final Stream<String> input) {
            // Act
            final List<String> output = input.gather(Gatherers4j.largest(3)).toList();

            // Assert
            assertThat(output).containsExactly("A");
        }

        @ParallelAndSequentialTest(values = {"A", "B", "A", "B"})
        void keepsDuplicates(final Stream<String> input) {
            // Act
            final List<String> output = input.gather(Gatherers4j.largest(3)).toList();

            // Assert
            assertThat(output).containsExactly("B", "B", "A");
        }

        @Test
        void zeroCountStopsReading() {
            // Act
            final List<Integer> output = Stream.iterate(1, it -> it + 1).gather(Gatherers4j.largest(0)).toList();

            // Assert
            assertThat(output).isEmpty();
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, Integer.MIN_VALUE})
        void countMustNotBeNegative(final int count) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Gatherers4j.largest(count))
                    .withMessage("Count must not be negative");
        }
    }

    @Nested
    class LargestBy {

        @SuppressWarnings("DataFlowIssue")
        @Test
        void comparatorMustNotBeNull() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Gatherers4j.largestBy(1, null))
                    .withMessage("Comparator must not be null");
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, Integer.MIN_VALUE})
        void countMustNotBeNegative(final int count) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Gatherers4j.largestBy(count, BY_LENGTH))
                    .withMessage("Count must not be negative");
        }

        @ParallelAndSequentialTest(values = {"BB", "A", "DDDD", "CCC"})
        void emitsLargestFirst(final Stream<String> input) {
            // Act
            final List<String> output = input.gather(Gatherers4j.largestBy(2, BY_LENGTH)).toList();

            // Assert
            assertThat(output).containsExactly("DDDD", "CCC");
        }

        @ParallelAndSequentialTest
        void emptyStream(final Stream<String> input) {
            // Act
            final List<String> output = input.gather(Gatherers4j.largestBy(2, BY_LENGTH)).toList();

            // Assert
            assertThat(output).isEmpty();
        }

        @ParallelAndSequentialTest(values = {"B", NULL, "A"})
        void passesNullsToComparator(final Stream<String> input) {
            // Act
            final List<String> output = input
                    .gather(Gatherers4j.largestBy(3, Comparator.nullsLast(Comparator.naturalOrder())))
                    .toList();

            // Assert
            assertThat(output).containsExactly(null, "B", "A");
        }

        @ParallelAndSequentialTest(values = "A")
        void singleElement(final Stream<String> input) {
            // Act
            final List<String> output = input.gather(Gatherers4j.largestBy(2, BY_LENGTH)).toList();

            // Assert
            assertThat(output).containsExactly("A");
        }

        @ParallelAndSequentialTest(values = {"a", "BB", "c", "DD", "e", "FF"})
        void tiesKeepEncounterOrder(final Stream<String> input) {
            // Act
            final List<String> output = input.gather(Gatherers4j.largestBy(4, BY_LENGTH)).toList();

            // Assert
            assertThat(output).containsExactly("BB", "DD", "FF", "a");
        }

        @Test
        void zeroCountStopsReading() {
            // Act
            final List<Integer> output = Stream.iterate(1, it -> it + 1)
                    .gather(Gatherers4j.largestBy(0, Comparator.naturalOrder()))
                    .toList();

            // Assert
            assertThat(output).isEmpty();
        }
    }

    @Nested
    class Smallest {

        @ParameterizedTest
        @ValueSource(ints = {-1, Integer.MIN_VALUE})
        void countMustNotBeNegative(final int count) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Gatherers4j.smallest(count))
                    .withMessage("Count must not be negative");
        }

        @ParallelAndSequentialTest(values = {"B", "E", "A", "D", "C"})
        void emitsSmallestFirst(final Stream<String> input) {
            // Act
            final List<String> output = input.gather(Gatherers4j.smallest(3)).toList();

            // Assert
            assertThat(output).containsExactly("A", "B", "C");
        }

        @ParallelAndSequentialTest
        void emptyStream(final Stream<String> input) {
            // Act
            final List<String> output = input.gather(Gatherers4j.smallest(3)).toList();

            // Assert
            assertThat(output).isEmpty();
        }

        @ParallelAndSequentialTest(values = {"B", "A"})
        void fewerElementsThanCountEmitsAll(final Stream<String> input) {
            // Act
            final List<String> output = input.gather(Gatherers4j.smallest(3)).toList();

            // Assert
            assertThat(output).containsExactly("A", "B");
        }

        @ParallelAndSequentialTest(values = {"B", NULL, "A", NULL, "C"})
        void ignoresNulls(final Stream<String> input) {
            // Act
            final List<String> output = input.gather(Gatherers4j.smallest(5)).toList();

            // Assert
            assertThat(output).containsExactly("A", "B", "C");
        }

        @ParallelAndSequentialTest(values = {"B", "A", "B", "A"})
        void keepsDuplicates(final Stream<String> input) {
            // Act
            final List<String> output = input.gather(Gatherers4j.smallest(3)).toList();

            // Assert
            assertThat(output).containsExactly("A", "A", "B");
        }

        @ParallelAndSequentialTest(values = "A")
        void singleElement(final Stream<String> input) {
            // Act
            final List<String> output = input.gather(Gatherers4j.smallest(3)).toList();

            // Assert
            assertThat(output).containsExactly("A");
        }

        @Test
        void zeroCountStopsReading() {
            // Act
            final List<Integer> output = Stream.iterate(1, it -> it + 1).gather(Gatherers4j.smallest(0)).toList();

            // Assert
            assertThat(output).isEmpty();
        }
    }

    @Nested
    class SmallestBy {

        @SuppressWarnings("DataFlowIssue")
        @Test
        void comparatorMustNotBeNull() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Gatherers4j.smallestBy(1, null))
                    .withMessage("Comparator must not be null");
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, Integer.MIN_VALUE})
        void countMustNotBeNegative(final int count) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Gatherers4j.smallestBy(count, BY_LENGTH))
                    .withMessage("Count must not be negative");
        }

        @ParallelAndSequentialTest(values = {"BB", "DDDD", "A", "CCC"})
        void emitsSmallestFirst(final Stream<String> input) {
            // Act
            final List<String> output = input.gather(Gatherers4j.smallestBy(2, BY_LENGTH)).toList();

            // Assert
            assertThat(output).containsExactly("A", "BB");
        }

        @ParallelAndSequentialTest
        void emptyStream(final Stream<String> input) {
            // Act
            final List<String> output = input.gather(Gatherers4j.smallestBy(2, BY_LENGTH)).toList();

            // Assert
            assertThat(output).isEmpty();
        }

        @ParallelAndSequentialTest(values = {"B", NULL, "A"})
        void passesNullsToComparator(final Stream<String> input) {
            // Act
            final List<String> output = input
                    .gather(Gatherers4j.smallestBy(2, Comparator.nullsFirst(Comparator.naturalOrder())))
                    .toList();

            // Assert
            assertThat(output).containsExactly(null, "A");
        }

        @ParallelAndSequentialTest(values = "A")
        void singleElement(final Stream<String> input) {
            // Act
            final List<String> output = input.gather(Gatherers4j.smallestBy(2, BY_LENGTH)).toList();

            // Assert
            assertThat(output).containsExactly("A");
        }

        @ParallelAndSequentialTest(values = {"AA", "b", "CC", "d", "EE", "f"})
        void tiesKeepEncounterOrder(final Stream<String> input) {
            // Act
            final List<String> output = input.gather(Gatherers4j.smallestBy(4, BY_LENGTH)).toList();

            // Assert
            assertThat(output).containsExactly("b", "d", "f", "AA");
        }

        @Test
        void zeroCountStopsReading() {
            // Act
            final List<Integer> output = Stream.iterate(1, it -> it + 1)
                    .gather(Gatherers4j.smallestBy(0, Comparator.naturalOrder()))
                    .toList();

            // Assert
            assertThat(output).isEmpty();
        }
    }

    @Nested
    class Behavior {

        record Item(int key, int id) {
        }

        private static final Comparator<Item> BY_KEY = Comparator.comparingInt(Item::key);

        // Many duplicate keys, so ties at the cut-off and across parallel partitions are common.
        private final List<Item> items = new Random(42)
                .ints(5_000, 0, 100)
                .boxed()
                .collect(ArrayList::new, (list, key) -> list.add(new Item(key, list.size())), List::addAll);

        @ParameterizedTest
        @ValueSource(ints = {1, 7, 100, 4_999, 5_000, 10_000})
        void largestByMatchesSortedLimit(final int count) {
            // Arrange
            final List<Item> expected = items.stream().sorted(BY_KEY.reversed()).limit(count).toList();

            // Act
            final List<Item> sequential = items.stream().gather(Gatherers4j.largestBy(count, BY_KEY)).toList();
            final List<Item> parallel = items.parallelStream().gather(Gatherers4j.largestBy(count, BY_KEY)).toList();

            // Assert
            assertThat(sequential).isEqualTo(expected);
            assertThat(parallel).isEqualTo(expected);
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 7, 100, 4_999, 5_000, 10_000})
        void smallestByMatchesSortedLimit(final int count) {
            // Arrange
            final List<Item> expected = items.stream().sorted(BY_KEY).limit(count).toList();

            // Act
            final List<Item> sequential = items.stream().gather(Gatherers4j.smallestBy(count, BY_KEY)).toList();
            final List<Item> parallel = items.parallelStream().gather(Gatherers4j.smallestBy(count, BY_KEY)).toList();

            // Assert
            assertThat(sequential).isEqualTo(expected);
            assertThat(parallel).isEqualTo(expected);
        }

        @Test
        void downstreamCanStopEarly() {
            // Act
            final List<Integer> output = Stream.of(3, 1, 4, 1, 5, 9, 2, 6)
                    .gather(Gatherers4j.largest(5))
                    .limit(2)
                    .toList();

            // Assert
            assertThat(output).containsExactly(9, 6);
        }

        @Test
        void gathererCanBeReused() {
            // Arrange
            final Gatherer<Integer, ?, Integer> gatherer = Gatherers4j.smallest(2);

            // Act
            final List<Integer> first = Stream.of(5, 3, 4).gather(gatherer).toList();
            final List<Integer> second = Stream.of(9, 8, 7).gather(gatherer).toList();

            // Assert
            assertThat(first).containsExactly(3, 4);
            assertThat(second).containsExactly(7, 8);
        }
    }

    @Nested
    class Wildcards {

        @Test
        void broaderComparatorKeepsElementType() {
            // Arrange
            final Comparator<CharSequence> length = Comparator.comparingInt(CharSequence::length);

            // Act
            final List<String> largest = Stream.of("A", "BBB", "CC").gather(Gatherers4j.largestBy(1, length)).toList();
            final List<String> smallest = Stream.of("A", "BBB", "CC").gather(Gatherers4j.smallestBy(1, length)).toList();

            // Assert
            assertThat(largest).containsExactly("BBB");
            assertThat(smallest).containsExactly("A");
        }
    }
}
