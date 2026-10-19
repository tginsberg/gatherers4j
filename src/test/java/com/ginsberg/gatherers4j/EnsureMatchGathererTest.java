/*
 * Copyright 2026 Todd Ginsberg
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
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static com.ginsberg.gatherers4j.test.ParallelAndSequentialTest.NULL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

class EnsureMatchGathererTest {

    private static final String ALL_MESSAGE = "Not all elements match the predicate";
    private static final String ANY_MESSAGE = "No element matches the predicate";
    private static final String NONE_MESSAGE = "An element matches the predicate";

    @Nested
    class EnsureAllMatch {

        @ParallelAndSequentialTest(values = {"A1", "A2", "A3"})
        void allMatchEmitsInOrder(final Stream<String> input) {
            // Act
            final List<String> output = input.gather(Gatherers4j.ensureAllMatch(s -> s.startsWith("A"))).toList();

            // Assert
            assertThat(output).containsExactly("A1", "A2", "A3");
        }

        @ParallelAndSequentialTest
        void emptyStream(final Stream<String> input) {
            // Act
            final List<String> output = input.gather(Gatherers4j.ensureAllMatch(s -> s.startsWith("A"))).toList();

            // Assert
            assertThat(output).isEmpty();
        }

        @Test
        void emitsNothingBeforeFailing() {
            // Arrange
            final List<String> emitted = new ArrayList<>();

            // Act/Assert
            assertThatIllegalStateException()
                    .isThrownBy(() -> Stream.of("A1", "A2", "B1")
                            .gather(Gatherers4j.ensureAllMatch(s -> s.startsWith("A")))
                            .forEach(emitted::add))
                    .withMessage(ALL_MESSAGE);
            assertThat(emitted).isEmpty();
        }

        @ParallelAndSequentialTest(values = {"A1", "B1", "A2"})
        void failsOnNonMatch(final Stream<String> input) {
            // Act/Assert
            assertThatIllegalStateException()
                    .isThrownBy(() -> input.gather(Gatherers4j.ensureAllMatch(s -> s.startsWith("A"))).toList())
                    .withMessage(ALL_MESSAGE);
        }

        @ParallelAndSequentialTest(values = {"A1", "A2", "A3", "A4", "A5", "A6", "A7", "B1"})
        void failsOnNonMatchInDifferentPartitions(final Stream<String> input) {
            // Act/Assert
            assertThatIllegalStateException()
                    .isThrownBy(() -> input.gather(Gatherers4j.ensureAllMatch(s -> s.startsWith("A"))).toList())
                    .withMessage(ALL_MESSAGE);
        }

        @ParallelAndSequentialTest(values = {"A", NULL})
        void nullElementReachesPredicate(final Stream<String> input) {
            // Act/Assert
            assertThatIllegalStateException()
                    .isThrownBy(() -> input.gather(Gatherers4j.ensureAllMatch(Objects::nonNull)).toList())
                    .withMessage(ALL_MESSAGE);
        }

        @ParallelAndSequentialTest(values = {"A1", "B1", "A2"})
        void orElseEmptyOnNonMatch(final Stream<String> input) {
            // Act
            final List<String> output = input
                    .gather(Gatherers4j.<String>ensureAllMatch(s -> s.startsWith("A")).orElseEmpty())
                    .toList();

            // Assert
            assertThat(output).isEmpty();
        }

        @SuppressWarnings("DataFlowIssue")
        @Test
        void orElseMustNotBeNull() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Gatherers4j.<String>ensureAllMatch(s -> s.startsWith("A")).orElse(null))
                    .withMessage("The orElse function must not be null");
        }

        @ParallelAndSequentialTest(values = {"A1", "A2", "A3"})
        void orElseNotUsedWhenAllMatch(final Stream<String> input) {
            // Act
            final List<String> output = input
                    .gather(Gatherers4j.<String>ensureAllMatch(s -> s.startsWith("A")).orElse(() -> Stream.of("X", "Y")))
                    .toList();

            // Assert
            assertThat(output).containsExactly("A1", "A2", "A3");
        }

        @ParallelAndSequentialTest(values = {"A1", "B1", "A2"})
        void orElseOnNonMatch(final Stream<String> input) {
            // Act
            final List<String> output = input
                    .gather(Gatherers4j.<String>ensureAllMatch(s -> s.startsWith("A")).orElse(() -> Stream.of("X", "Y")))
                    .toList();

            // Assert
            assertThat(output).containsExactly("X", "Y");
        }

        @SuppressWarnings("DataFlowIssue")
        @Test
        void predicateMustNotBeNull() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Gatherers4j.ensureAllMatch(null))
                    .withMessage("Predicate must not be null");
        }

        @ParallelAndSequentialTest(values = "A1")
        void singleElement(final Stream<String> input) {
            // Act
            final List<String> output = input.gather(Gatherers4j.ensureAllMatch(s -> s.startsWith("A"))).toList();

            // Assert
            assertThat(output).containsExactly("A1");
        }

        @Test
        void stopsAtFirstNonMatch() {
            // Arrange
            final AtomicInteger pulled = new AtomicInteger();

            // Act/Assert
            assertThatIllegalStateException()
                    .isThrownBy(() -> Stream.of("A1", "B1", "A2", "A3")
                            .peek(_ -> pulled.incrementAndGet())
                            .gather(Gatherers4j.ensureAllMatch(s -> s.startsWith("A")))
                            .toList())
                    .withMessage(ALL_MESSAGE);
            assertThat(pulled).hasValue(2);
        }

        @Test
        void stopsWhenDownstreamRejects() {
            // Act
            final List<String> output = Stream.of("A1", "A2", "A3")
                    .gather(Gatherers4j.ensureAllMatch(s -> s.startsWith("A")))
                    .limit(2)
                    .toList();

            // Assert
            assertThat(output).containsExactly("A1", "A2");
        }
    }

    @Nested
    class EnsureAnyMatch {

        @Test
        void emitsAllWhenMatchIsFirst() {
            // Act
            final List<String> output = Stream.of("B1", "A1", "A2")
                    .gather(Gatherers4j.ensureAnyMatch(s -> s.startsWith("B")))
                    .toList();

            // Assert
            assertThat(output).containsExactly("B1", "A1", "A2");
        }

        @Test
        void emitsAllWhenMatchIsLast() {
            // Act
            final List<String> output = Stream.of("A1", "A2", "B1")
                    .gather(Gatherers4j.ensureAnyMatch(s -> s.startsWith("B")))
                    .toList();

            // Assert
            assertThat(output).containsExactly("A1", "A2", "B1");
        }

        @Test
        void emitsNothingBeforeFailing() {
            // Arrange
            final List<String> emitted = new ArrayList<>();

            // Act/Assert
            assertThatIllegalStateException()
                    .isThrownBy(() -> Stream.of("A1", "A2", "A3")
                            .gather(Gatherers4j.ensureAnyMatch(s -> s.startsWith("B")))
                            .forEach(emitted::add))
                    .withMessage(ANY_MESSAGE);
            assertThat(emitted).isEmpty();
        }

        @Test
        void emptyStreamFails() {
            // Act/Assert
            assertThatIllegalStateException()
                    .isThrownBy(() -> Stream.<String>empty()
                            .gather(Gatherers4j.ensureAnyMatch(s -> s.startsWith("B")))
                            .toList())
                    .withMessage(ANY_MESSAGE);
        }

        @Test
        void failsWhenNothingMatches() {
            // Act/Assert
            assertThatIllegalStateException()
                    .isThrownBy(() -> Stream.of("A1", "A2")
                            .gather(Gatherers4j.ensureAnyMatch(s -> s.startsWith("B")))
                            .toList())
                    .withMessage(ANY_MESSAGE);
        }

        @Test
        void keepsOrderAroundMatch() {
            // Act
            final List<String> output = Stream.of("A1", "A2", "B1", "A3")
                    .gather(Gatherers4j.ensureAnyMatch(s -> s.startsWith("B")))
                    .toList();

            // Assert
            assertThat(output).containsExactly("A1", "A2", "B1", "A3");
        }

        @ParallelAndSequentialTest(values = {"A1", "A2", "A3", "A4", "A5", "A6", "A7", "B1"})
        void matchInDifferentPartition(final Stream<String> input) {
            // Act
            final List<String> output = input.gather(Gatherers4j.ensureAnyMatch(s -> s.startsWith("B"))).toList();

            // Assert
            assertThat(output).containsExactly("A1", "A2", "A3", "A4", "A5", "A6", "A7", "B1");
        }

        @Test
        void nullElementReachesPredicate() {
            // Act
            final List<String> output = Stream.of("A", null)
                    .gather(Gatherers4j.ensureAnyMatch(Objects::isNull))
                    .toList();

            // Assert
            assertThat(output).containsExactly("A", null);
        }

        @Test
        void orElseEmptyWhenNothingMatches() {
            // Act
            final List<String> output = Stream.of("A1", "A2")
                    .gather(Gatherers4j.<String>ensureAnyMatch(s -> s.startsWith("B")).orElseEmpty())
                    .toList();

            // Assert
            assertThat(output).isEmpty();
        }

        @SuppressWarnings("DataFlowIssue")
        @Test
        void orElseMustNotBeNull() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Gatherers4j.<String>ensureAnyMatch(s -> s.startsWith("B")).orElse(null))
                    .withMessage("The orElse function must not be null");
        }

        @Test
        void orElseNotUsedWhenMatched() {
            // Act
            final List<String> output = Stream.of("A1", "B1", "A2")
                    .gather(Gatherers4j.<String>ensureAnyMatch(s -> s.startsWith("B")).orElse(() -> Stream.of("X", "Y")))
                    .toList();

            // Assert
            assertThat(output).containsExactly("A1", "B1", "A2");
        }

        @Test
        void orElseWhenNothingMatches() {
            // Act
            final List<String> output = Stream.of("A1", "A2")
                    .gather(Gatherers4j.<String>ensureAnyMatch(s -> s.startsWith("B")).orElse(() -> Stream.of("X", "Y")))
                    .toList();

            // Assert
            assertThat(output).containsExactly("X", "Y");
        }

        @Test
        void passesThroughAfterMatch() {
            // Act
            final List<String> output = Stream.iterate(0, i -> i + 1)
                    .map(i -> i == 2 ? "B" + i : "A" + i)
                    .gather(Gatherers4j.ensureAnyMatch(s -> s.startsWith("B")))
                    .limit(5)
                    .toList();

            // Assert
            assertThat(output).containsExactly("A0", "A1", "B2", "A3", "A4");
        }

        @SuppressWarnings("DataFlowIssue")
        @Test
        void predicateMustNotBeNull() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Gatherers4j.ensureAnyMatch(null))
                    .withMessage("Predicate must not be null");
        }

        @Test
        void singleMatchingElement() {
            // Act
            final List<String> output = Stream.of("B1")
                    .gather(Gatherers4j.ensureAnyMatch(s -> s.startsWith("B")))
                    .toList();

            // Assert
            assertThat(output).containsExactly("B1");
        }

        @Test
        void stopsWhenDownstreamRejectsWhileFlushing() {
            // Arrange
            final AtomicInteger pulled = new AtomicInteger();

            // Act
            final List<String> output = Stream.of("A1", "A2", "B1", "A3")
                    .peek(_ -> pulled.incrementAndGet())
                    .gather(Gatherers4j.ensureAnyMatch(s -> s.startsWith("B")))
                    .limit(1)
                    .toList();

            // Assert
            assertThat(output).containsExactly("A1");
            assertThat(pulled).hasValue(3);
        }
    }

    @Nested
    class EnsureNoneMatch {

        @ParallelAndSequentialTest(values = {"A1", "A2", "A3"})
        void noneMatchEmitsInOrder(final Stream<String> input) {
            // Act
            final List<String> output = input.gather(Gatherers4j.ensureNoneMatch(s -> s.startsWith("B"))).toList();

            // Assert
            assertThat(output).containsExactly("A1", "A2", "A3");
        }

        @ParallelAndSequentialTest
        void emptyStream(final Stream<String> input) {
            // Act
            final List<String> output = input.gather(Gatherers4j.ensureNoneMatch(s -> s.startsWith("B"))).toList();

            // Assert
            assertThat(output).isEmpty();
        }

        @Test
        void emitsNothingBeforeFailing() {
            // Arrange
            final List<String> emitted = new ArrayList<>();

            // Act/Assert
            assertThatIllegalStateException()
                    .isThrownBy(() -> Stream.of("A1", "A2", "B1")
                            .gather(Gatherers4j.ensureNoneMatch(s -> s.startsWith("B")))
                            .forEach(emitted::add))
                    .withMessage(NONE_MESSAGE);
            assertThat(emitted).isEmpty();
        }

        @ParallelAndSequentialTest(values = {"A1", "B1", "A2"})
        void failsOnMatch(final Stream<String> input) {
            // Act/Assert
            assertThatIllegalStateException()
                    .isThrownBy(() -> input.gather(Gatherers4j.ensureNoneMatch(s -> s.startsWith("B"))).toList())
                    .withMessage(NONE_MESSAGE);
        }

        @ParallelAndSequentialTest(values = {"A1", "A2", "A3", "A4", "A5", "A6", "A7", "B1"})
        void failsOnMatchInDifferentPartitions(final Stream<String> input) {
            // Act/Assert
            assertThatIllegalStateException()
                    .isThrownBy(() -> input.gather(Gatherers4j.ensureNoneMatch(s -> s.startsWith("B"))).toList())
                    .withMessage(NONE_MESSAGE);
        }

        @ParallelAndSequentialTest(values = {"A", NULL})
        void nullElementReachesPredicate(final Stream<String> input) {
            // Act/Assert
            assertThatIllegalStateException()
                    .isThrownBy(() -> input.gather(Gatherers4j.ensureNoneMatch(Objects::isNull)).toList())
                    .withMessage(NONE_MESSAGE);
        }

        @ParallelAndSequentialTest(values = {"A1", "B1", "A2"})
        void orElseEmptyOnMatch(final Stream<String> input) {
            // Act
            final List<String> output = input
                    .gather(Gatherers4j.<String>ensureNoneMatch(s -> s.startsWith("B")).orElseEmpty())
                    .toList();

            // Assert
            assertThat(output).isEmpty();
        }

        @SuppressWarnings("DataFlowIssue")
        @Test
        void orElseMustNotBeNull() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Gatherers4j.<String>ensureNoneMatch(s -> s.startsWith("B")).orElse(null))
                    .withMessage("The orElse function must not be null");
        }

        @ParallelAndSequentialTest(values = {"A1", "A2", "A3"})
        void orElseNotUsedWhenNoneMatch(final Stream<String> input) {
            // Act
            final List<String> output = input
                    .gather(Gatherers4j.<String>ensureNoneMatch(s -> s.startsWith("B")).orElse(() -> Stream.of("X", "Y")))
                    .toList();

            // Assert
            assertThat(output).containsExactly("A1", "A2", "A3");
        }

        @ParallelAndSequentialTest(values = {"A1", "B1", "A2"})
        void orElseOnMatch(final Stream<String> input) {
            // Act
            final List<String> output = input
                    .gather(Gatherers4j.<String>ensureNoneMatch(s -> s.startsWith("B")).orElse(() -> Stream.of("X", "Y")))
                    .toList();

            // Assert
            assertThat(output).containsExactly("X", "Y");
        }

        @SuppressWarnings("DataFlowIssue")
        @Test
        void predicateMustNotBeNull() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Gatherers4j.ensureNoneMatch(null))
                    .withMessage("Predicate must not be null");
        }

        @ParallelAndSequentialTest(values = "A1")
        void singleElement(final Stream<String> input) {
            // Act
            final List<String> output = input.gather(Gatherers4j.ensureNoneMatch(s -> s.startsWith("B"))).toList();

            // Assert
            assertThat(output).containsExactly("A1");
        }

        @Test
        void stopsAtFirstMatch() {
            // Arrange
            final AtomicInteger pulled = new AtomicInteger();

            // Act/Assert
            assertThatIllegalStateException()
                    .isThrownBy(() -> Stream.of("A1", "B1", "A2", "A3")
                            .peek(_ -> pulled.incrementAndGet())
                            .gather(Gatherers4j.ensureNoneMatch(s -> s.startsWith("B")))
                            .toList())
                    .withMessage(NONE_MESSAGE);
            assertThat(pulled).hasValue(2);
        }

        @Test
        void stopsWhenDownstreamRejects() {
            // Act
            final List<String> output = Stream.of("A1", "A2", "A3")
                    .gather(Gatherers4j.ensureNoneMatch(s -> s.startsWith("B")))
                    .limit(2)
                    .toList();

            // Assert
            assertThat(output).containsExactly("A1", "A2");
        }
    }
}
