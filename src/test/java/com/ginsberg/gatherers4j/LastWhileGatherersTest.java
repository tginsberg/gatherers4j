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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.ginsberg.gatherers4j.test.ParallelAndSequentialTest.NULL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class LastWhileGatherersTest {

    @Nested
    class DropLastWhile {

        @Test
        void dropLastWhile() {
            // Arrange
            final Stream<Integer> input = Stream.of(1, 5, 2, 8, 9);

            // Act
            final List<Integer> output = input.gather(Gatherers4j.dropLastWhile(i -> i > 4)).toList();

            // Assert
            assertThat(output).containsExactly(1, 5, 2);
        }

        @Test
        void dropLastWhileAllMatch() {
            // Arrange
            final Stream<Integer> input = Stream.of(5, 6, 7);

            // Act
            final List<Integer> output = input.gather(Gatherers4j.dropLastWhile(i -> i > 4)).toList();

            // Assert
            assertThat(output).isEmpty();
        }

        @Test
        void dropLastWhileCallsPredicateOncePerElementInOrder() {
            // Arrange
            final List<Integer> tested = new ArrayList<>();
            final Stream<Integer> input = Stream.of(1, 5, 2, 8, 9);

            // Act
            final List<Integer> output = input
                    .gather(Gatherers4j.dropLastWhile(i -> {
                        tested.add(i);
                        return i > 4;
                    }))
                    .toList();

            // Assert
            assertThat(output).containsExactly(1, 5, 2);
            assertThat(tested).containsExactly(1, 5, 2, 8, 9);
        }

        @Test
        void dropLastWhileEmitsAsElementsArrive() {
            // Arrange
            final List<Integer> pulled = new ArrayList<>();
            final Stream<Integer> input = Stream.iterate(0, i -> i + 1).peek(pulled::add);

            // Act
            final List<Integer> output = input
                    .gather(Gatherers4j.dropLastWhile(i -> i % 3 != 0))
                    .limit(4)
                    .toList();

            // Assert
            assertThat(output).containsExactly(0, 1, 2, 3);
            assertThat(pulled).containsExactly(0, 1, 2, 3);
        }

        @Test
        void dropLastWhileEmpty() {
            // Arrange
            final Stream<Integer> input = Stream.empty();

            // Act
            final List<Integer> output = input.gather(Gatherers4j.dropLastWhile(i -> i > 4)).toList();

            // Assert
            assertThat(output).isEmpty();
        }

        @Test
        void dropLastWhileNoneMatch() {
            // Arrange
            final Stream<Integer> input = Stream.of(1, 2, 3);

            // Act
            final List<Integer> output = input.gather(Gatherers4j.dropLastWhile(i -> i > 4)).toList();

            // Assert
            assertThat(output).containsExactly(1, 2, 3);
        }

        @Test
        void dropLastWhileNullElements() {
            // Arrange
            final Stream<String> input = Stream.of(null, "A", null, "B", null, null);

            // Act
            final List<String> output = input.gather(Gatherers4j.dropLastWhile(s -> s == null)).toList();

            // Assert
            assertThat(output).containsExactly(null, "A", null, "B");
        }

        @Test
        void dropLastWhileSingleElementMatching() {
            // Arrange
            final Stream<Integer> input = Stream.of(5);

            // Act
            final List<Integer> output = input.gather(Gatherers4j.dropLastWhile(i -> i > 4)).toList();

            // Assert
            assertThat(output).isEmpty();
        }

        @Test
        void dropLastWhileSingleElementNotMatching() {
            // Arrange
            final Stream<Integer> input = Stream.of(1);

            // Act
            final List<Integer> output = input.gather(Gatherers4j.dropLastWhile(i -> i > 4)).toList();

            // Assert
            assertThat(output).containsExactly(1);
        }

        @Test
        void dropLastWhileStopsInsideAHeldRun() {
            // Arrange
            final List<Integer> pulled = new ArrayList<>();
            final Stream<Integer> input = Stream.of(5, 6, 7, 1, 2).peek(pulled::add);

            // Act
            final List<Integer> output = input
                    .gather(Gatherers4j.dropLastWhile(i -> i > 4))
                    .limit(2)
                    .toList();

            // Assert
            assertThat(output).containsExactly(5, 6);
            assertThat(pulled).containsExactly(5, 6, 7, 1);
        }

        @SuppressWarnings("DataFlowIssue")
        @Test
        void predicateMustNotBeNull() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Gatherers4j.dropLastWhile(null))
                    .withMessage("Predicate must not be null");
        }

        @Test
        void reusedAcrossStreams() {
            // Arrange
            final var gatherer = Gatherers4j.<Integer>dropLastWhile(i -> i > 4);

            // Act
            final List<Integer> first = Stream.of(1, 5).gather(gatherer).toList();
            final List<Integer> second = Stream.of(2, 6).gather(gatherer).toList();

            // Assert
            assertThat(first).containsExactly(1);
            assertThat(second).containsExactly(2);
        }
    }

    @Nested
    class TakeLastWhile {

        @SuppressWarnings("DataFlowIssue")
        @Test
        void predicateMustNotBeNull() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Gatherers4j.takeLastWhile(null))
                    .withMessage("Predicate must not be null");
        }

        @Test
        void reusedAcrossStreams() {
            // Arrange
            final var gatherer = Gatherers4j.<Integer>takeLastWhile(i -> i > 4);

            // Act
            final List<Integer> first = Stream.of(1, 5).gather(gatherer).toList();
            final List<Integer> second = Stream.of(2, 6).gather(gatherer).toList();

            // Assert
            assertThat(first).containsExactly(5);
            assertThat(second).containsExactly(6);
        }

        @ParallelAndSequentialTest(values = {"a", "bb", "c", "dd", "ee"})
        void takeLastWhile(final Stream<String> input) {
            // Act
            final List<String> output = input.gather(Gatherers4j.takeLastWhile(s -> s.length() == 2)).toList();

            // Assert
            assertThat(output).containsExactly("dd", "ee");
        }

        @ParallelAndSequentialTest(values = {"aa", "bb", "cc"})
        void takeLastWhileAllMatch(final Stream<String> input) {
            // Act
            final List<String> output = input.gather(Gatherers4j.takeLastWhile(s -> s.length() == 2)).toList();

            // Assert
            assertThat(output).containsExactly("aa", "bb", "cc");
        }

        @ParallelAndSequentialTest(values = {})
        void takeLastWhileEmpty(final Stream<String> input) {
            // Act
            final List<String> output = input.gather(Gatherers4j.takeLastWhile(s -> s.length() == 2)).toList();

            // Assert
            assertThat(output).isEmpty();
        }

        @ParallelAndSequentialTest(values = {"aa", "bb", "c"})
        void takeLastWhileLastDoesNotMatch(final Stream<String> input) {
            // Act
            final List<String> output = input.gather(Gatherers4j.takeLastWhile(s -> s.length() == 2)).toList();

            // Assert
            assertThat(output).isEmpty();
        }

        @Test
        void takeLastWhileLargeParallelStream() {
            // Arrange
            final Predicate<Integer> matching = i -> i % 1000 >= 500;

            // Act
            final List<Integer> output = IntStream.range(0, 100_000)
                    .boxed()
                    .parallel()
                    .gather(Gatherers4j.takeLastWhile(matching))
                    .toList();

            // Assert
            assertThat(output).isEqualTo(IntStream.range(99_500, 100_000).boxed().toList());
        }

        @Test
        void takeLastWhileLargeParallelStreamAllMatch() {
            // Act
            final List<Integer> output = IntStream.range(0, 100_000)
                    .boxed()
                    .parallel()
                    .gather(Gatherers4j.takeLastWhile(_ -> true))
                    .toList();

            // Assert
            assertThat(output).isEqualTo(IntStream.range(0, 100_000).boxed().toList());
        }

        @ParallelAndSequentialTest(values = {"aa", NULL, "b", NULL, NULL})
        void takeLastWhileNullElements(final Stream<String> input) {
            // Act
            final List<String> output = input.gather(Gatherers4j.takeLastWhile(s -> s == null)).toList();

            // Assert
            assertThat(output).containsExactly(null, null);
        }

        @ParallelAndSequentialTest(values = {"aa"})
        void takeLastWhileSingleElementMatching(final Stream<String> input) {
            // Act
            final List<String> output = input.gather(Gatherers4j.takeLastWhile(s -> s.length() == 2)).toList();

            // Assert
            assertThat(output).containsExactly("aa");
        }

        @ParallelAndSequentialTest(values = {"a"})
        void takeLastWhileSingleElementNotMatching(final Stream<String> input) {
            // Act
            final List<String> output = input.gather(Gatherers4j.takeLastWhile(s -> s.length() == 2)).toList();

            // Assert
            assertThat(output).isEmpty();
        }

        @Test
        void takeLastWhileStopsWhenDownstreamStops() {
            // Arrange
            final Stream<Integer> input = Stream.of(1, 5, 6, 7);

            // Act
            final List<Integer> output = input
                    .gather(Gatherers4j.takeLastWhile(i -> i > 4))
                    .limit(2)
                    .toList();

            // Assert
            assertThat(output).containsExactly(5, 6);
        }
    }

    @Nested
    class Wildcards {

        @Test
        void dropLastWhileBroaderPredicateKeepsElementType() {
            // Arrange
            final Predicate<CharSequence> empty = CharSequence::isEmpty;

            // Act
            final List<String> output = Stream.of("A", "", "").gather(Gatherers4j.dropLastWhile(empty)).toList();

            // Assert
            assertThat(output).containsExactly("A");
        }

        @Test
        void takeLastWhileBroaderPredicateKeepsElementType() {
            // Arrange
            final Predicate<CharSequence> empty = CharSequence::isEmpty;

            // Act
            final List<String> output = Stream.of("A", "", "").gather(Gatherers4j.takeLastWhile(empty)).toList();

            // Assert
            assertThat(output).containsExactly("", "");
        }
    }
}
