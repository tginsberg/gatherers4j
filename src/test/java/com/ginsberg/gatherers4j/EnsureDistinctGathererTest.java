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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static com.ginsberg.gatherers4j.test.ParallelAndSequentialTest.NULL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

class EnsureDistinctGathererTest {

    private static final String MESSAGE = "Elements are not distinct";

    @Nested
    class EnsureDistinct {

        @ParallelAndSequentialTest(values = {"A", "B", "C"})
        void distinctElementsAreEmittedInOrder(final Stream<String> input) {
            // Act
            final List<String> output = input.gather(Gatherers4j.ensureDistinct()).toList();

            // Assert
            assertThat(output).containsExactly("A", "B", "C");
        }

        @ParallelAndSequentialTest()
        void emptyStream(final Stream<String> input) {
            // Act
            final List<String> output = input.gather(Gatherers4j.ensureDistinct()).toList();

            // Assert
            assertThat(output).isEmpty();
        }

        @Test
        void emitsNothingBeforeFailing() {
            // Arrange
            final List<String> emitted = new ArrayList<>();

            // Act/Assert
            assertThatIllegalStateException()
                    .isThrownBy(() -> Stream.of("A", "B", "C", "A")
                            .gather(Gatherers4j.ensureDistinct())
                            .forEach(emitted::add))
                    .withMessage(MESSAGE);
            assertThat(emitted).isEmpty();
        }

        @ParallelAndSequentialTest(values = {"A", "B", "C", "D", "E", "F", "G", "H", "A"})
        void failsOnDuplicateInDifferentPartitions(final Stream<String> input) {
            // Act/Assert
            assertThatIllegalStateException()
                    .isThrownBy(() -> input.gather(Gatherers4j.ensureDistinct()).toList())
                    .withMessage(MESSAGE);
        }

        @ParallelAndSequentialTest(values = {"A", "B", "A"})
        void failsOnDuplicate(final Stream<String> input) {
            // Act/Assert
            assertThatIllegalStateException()
                    .isThrownBy(() -> input.gather(Gatherers4j.ensureDistinct()).toList())
                    .withMessage(MESSAGE);
        }

        @ParallelAndSequentialTest(values = {NULL, "A", NULL})
        void failsOnDuplicateNull(final Stream<String> input) {
            // Act/Assert
            assertThatIllegalStateException()
                    .isThrownBy(() -> input.gather(Gatherers4j.ensureDistinct()).toList())
                    .withMessage(MESSAGE);
        }

        @ParallelAndSequentialTest(values = {"A", "B", "C", "D", "E", "F", "G", "G"})
        void failsOnDuplicateWithinOnePartition(final Stream<String> input) {
            // Act/Assert
            assertThatIllegalStateException()
                    .isThrownBy(() -> input.gather(Gatherers4j.ensureDistinct()).toList())
                    .withMessage(MESSAGE);
        }

        @ParallelAndSequentialTest(values = {"A", "B", "C", "D", "E", "F", "G", "H", "A"})
        void orElseEmptyOnDuplicate(final Stream<String> input) {
            // Act
            final List<String> output = input
                    .gather(Gatherers4j.<String>ensureDistinct().orElseEmpty())
                    .toList();

            // Assert
            assertThat(output).isEmpty();
        }

        @SuppressWarnings("DataFlowIssue")
        @Test
        void orElseMustNotBeNull() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Gatherers4j.ensureDistinct().orElse(null))
                    .withMessage("The orElse function must not be null");
        }

        @ParallelAndSequentialTest(values = {"A", "B", "C", "D", "E", "F", "G", "H", "A"})
        void orElseOnDuplicate(final Stream<String> input) {
            // Act
            final List<String> output = input
                    .gather(Gatherers4j.<String>ensureDistinct().orElse(() -> Stream.of("X", "Y")))
                    .toList();

            // Assert
            assertThat(output).containsExactly("X", "Y");
        }

        @ParallelAndSequentialTest(values = {"A", "B", "C"})
        void orElseNotUsedWhenDistinct(final Stream<String> input) {
            // Act
            final List<String> output = input
                    .gather(Gatherers4j.<String>ensureDistinct().orElse(() -> Stream.of("X", "Y")))
                    .toList();

            // Assert
            assertThat(output).containsExactly("A", "B", "C");
        }

        @ParallelAndSequentialTest(values = {"A", NULL, "B"})
        void singleNullIsDistinct(final Stream<String> input) {
            // Act
            final List<String> output = input.gather(Gatherers4j.ensureDistinct()).toList();

            // Assert
            assertThat(output).containsExactly("A", null, "B");
        }

        @ParallelAndSequentialTest(values = "A")
        void singleElement(final Stream<String> input) {
            // Act
            final List<String> output = input.gather(Gatherers4j.ensureDistinct()).toList();

            // Assert
            assertThat(output).containsExactly("A");
        }

        @Test
        void stopsAtFirstDuplicate() {
            // Arrange
            final AtomicInteger pulled = new AtomicInteger();

            // Act/Assert
            assertThatIllegalStateException()
                    .isThrownBy(() -> Stream.of("A", "B", "A", "C", "D")
                            .peek(_ -> pulled.incrementAndGet())
                            .gather(Gatherers4j.ensureDistinct())
                            .toList())
                    .withMessage(MESSAGE);
            assertThat(pulled).hasValue(3);
        }

        @Test
        void stopsWhenDownstreamRejects() {
            // Act
            final List<String> output = Stream.of("A", "B", "C")
                    .gather(Gatherers4j.ensureDistinct())
                    .limit(2)
                    .toList();

            // Assert
            assertThat(output).containsExactly("A", "B");
        }
    }

    @Nested
    class EnsureDistinctBy {

        @ParallelAndSequentialTest(values = {"A", "BB", "CCC"})
        void distinctMappedValuesEmitOriginals(final Stream<String> input) {
            // Act
            final List<String> output = input.gather(Gatherers4j.ensureDistinctBy(String::length)).toList();

            // Assert
            assertThat(output).containsExactly("A", "BB", "CCC");
        }

        @ParallelAndSequentialTest()
        void emptyStream(final Stream<String> input) {
            // Act
            final List<String> output = input.gather(Gatherers4j.ensureDistinctBy(String::length)).toList();

            // Assert
            assertThat(output).isEmpty();
        }

        @ParallelAndSequentialTest(values = {"A", "BB", "C"})
        void failsOnDuplicateMappedValue(final Stream<String> input) {
            // Act/Assert
            assertThatIllegalStateException()
                    .isThrownBy(() -> input.gather(Gatherers4j.ensureDistinctBy(String::length)).toList())
                    .withMessage(MESSAGE);
        }

        @ParallelAndSequentialTest(values = {"A", "BB", "CCC", "DDDD", "EEEEE", "FFFFFF", "GGGGGGG", "H"})
        void failsOnDuplicateMappedValueInDifferentPartitions(final Stream<String> input) {
            // Act/Assert
            assertThatIllegalStateException()
                    .isThrownBy(() -> input.gather(Gatherers4j.ensureDistinctBy(String::length)).toList())
                    .withMessage(MESSAGE);
        }

        @ParallelAndSequentialTest(values = {NULL, "A", NULL})
        void failsOnDuplicateNullMappedValue(final Stream<String> input) {
            // Act/Assert
            assertThatIllegalStateException()
                    .isThrownBy(() -> input
                            .gather(Gatherers4j.ensureDistinctBy(it -> it == null ? null : it.length()))
                            .toList())
                    .withMessage(MESSAGE);
        }

        @SuppressWarnings("DataFlowIssue")
        @Test
        void mappingFunctionMustNotBeNull() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Gatherers4j.ensureDistinctBy(null))
                    .withMessage("Mapping function must not be null");
        }

        @ParallelAndSequentialTest(values = {"A", "BB", "CCC", "DDDD", "EEEEE", "FFFFFF", "GGGGGGG", "H"})
        void orElseOnDuplicateMappedValue(final Stream<String> input) {
            // Act
            final List<String> output = input
                    .gather(Gatherers4j.ensureDistinctBy(String::length).orElse(() -> Stream.of("X")))
                    .toList();

            // Assert
            assertThat(output).containsExactly("X");
        }

        @ParallelAndSequentialTest(values = {NULL, "A", "BB"})
        void singleNullMappedValueIsDistinct(final Stream<String> input) {
            // Act
            final List<String> output = input
                    .gather(Gatherers4j.ensureDistinctBy(it -> it == null ? null : it.length()))
                    .toList();

            // Assert
            assertThat(output).containsExactly(null, "A", "BB");
        }

        @ParallelAndSequentialTest(values = "A")
        void singleElement(final Stream<String> input) {
            // Act
            final List<String> output = input.gather(Gatherers4j.ensureDistinctBy(String::length)).toList();

            // Assert
            assertThat(output).containsExactly("A");
        }
    }
}
