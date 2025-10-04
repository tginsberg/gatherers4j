/*
 * Copyright 2025 Todd Ginsberg
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

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.ginsberg.gatherers4j.test.ParallelAndSequentialTest.NULL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UniquelyOccurringGathererTest {

    @Nested
    class UniquelyOccurring {

        @ParallelAndSequentialTest(values = {NULL, NULL})
        void allNullsNonUnique(final Stream<String> input) {
            // Act
            final List<String> output = input
                    .gather(Gatherers4j.uniquelyOccurring())
                    .toList();

            // Assert
            assertThat(output).isEmpty();
        }

        @ParallelAndSequentialTest(values = {"A", NULL, "B", "C"})
        void allUniqueAreEmittedInOrder(final Stream<String> input) {
            // Act
            final List<String> output = input
                    .gather(Gatherers4j.uniquelyOccurring())
                    .toList();

            // Assert
            assertThat(output).containsExactly("A", null, "B", "C");
        }

        @ParallelAndSequentialTest(values = {NULL, "B", "B", "C", "C", "D", "D"})
        void allowsUniqueNull(final Stream<String> input) {
            //Act
            final List<String> output = input.gather(Gatherers4j.uniquelyOccurring()).toList();

            // Assert
            assertThat(output).hasSize(1).containsNull();
        }

        @ParallelAndSequentialTest
        void emitsEmptyOnEmptyStream(final Stream<String> input) {
            // Act
            final List<String> output = input
                    .gather(Gatherers4j.uniquelyOccurring())
                    .toList();

            // Assert
            assertThat(output).isEmpty();
        }

        @ParallelAndSequentialTest(values = {"A", "A", "B", "B", "C", "C", "D", "D"})
        void emitsEmptyOnNoUniqueItems(final Stream<String> input) {
            //Act
            final List<String> output = input.gather(Gatherers4j.uniquelyOccurring()).toList();

            // Assert
            assertThat(output).isEmpty();
        }

        @ParallelAndSequentialTest(values = {"A", "B", "C", "D", "B", "C"})
        void emitsInEncounterOrder(final Stream<String> input) {
            //Act
            final List<String> output = input.gather(Gatherers4j.uniquelyOccurring()).toList();

            // Assert
            assertThat(output).containsExactly("A", "D");
        }

        @ParallelAndSequentialTest(values = {"A", "B", "A", "A"})
        void filtersOutNonUnique(final Stream<String> input) {
            //Act
            final List<String> output = input.gather(Gatherers4j.uniquelyOccurring()).toList();

            // Assert
            assertThat(output).containsExactly("B");
        }

        @ParallelAndSequentialTest(values = {NULL, "B", NULL, NULL})
        void filtersOutNonUniqueNull(final Stream<String> input) {
            //Act
            final List<String> output = input.gather(Gatherers4j.uniquelyOccurring()).toList();

            // Assert
            assertThat(output).containsExactly("B");
        }

        @ParallelAndSequentialTest(values = {NULL, "A", "B", "B", "C", NULL, "D", "E", "E"})
        void mixtureWithSeveralUniquesIncludingNull(final Stream<String> input) {
            // Act
            final List<String> output = input
                    .gather(Gatherers4j.uniquelyOccurring())
                    .toList();

            // Assert
            assertThat(output).containsExactly("A", "C", "D");
        }

        @ParallelAndSequentialTest(values = "A")
        void singleElementIsUnique(final Stream<String> input) {
            // Act
            final List<String> output = input
                    .gather(Gatherers4j.uniquelyOccurring())
                    .toList();

            // Assert
            assertThat(output).containsExactly("A");
        }

        @ParallelAndSequentialTest(values = NULL)
        void singleNullIsUnique(final Stream<String> input) {
            // Act
            final List<String> output = input
                    .gather(Gatherers4j.uniquelyOccurring())
                    .toList();

            // Assert
            assertThat(output).hasSize(1).containsNull();
        }

        @ParallelAndSequentialTest(values = {"A", "B", "C", "A"})
        void uniqueThatBecomesDuplicateIsRemoved(final Stream<String> input) {
            // Act
            final List<String> output = input
                    .gather(Gatherers4j.uniquelyOccurring())
                    .toList();

            // Assert
            assertThat(output).containsExactly("B", "C");
        }

    }


    @Nested
    class UniquelyOccurringBy {

        @SuppressWarnings("DataFlowIssue")
        @Test
        void mappingFunctionMustNotBeNull() {
            assertThatThrownBy(() ->
                    Gatherers4j.uniquelyOccurringBy(null)
            ).isExactlyInstanceOf(IllegalArgumentException.class);
        }

        @ParallelAndSequentialTest(values = {NULL, NULL})
        void allNullsNonUnique(final Stream<String> input) {
            // Act
            final List<String> output = input
                    .gather(Gatherers4j.uniquelyOccurringBy(Function.identity()))
                    .toList();

            // Assert
            assertThat(output).isEmpty();
        }

        @ParallelAndSequentialTest(values = {"A", NULL, "B", "C"})
        void allUniqueAreEmittedInOrder(final Stream<String> input) {
            // Act
            final List<String> output = input
                    .gather(Gatherers4j.uniquelyOccurringBy(Function.identity()))
                    .toList();

            // Assert
            assertThat(output).containsExactly("A", null, "B", "C");
        }

        @ParallelAndSequentialTest(values = {NULL, "B", "B", "C", "C", "D", "D"})
        void allowsUniqueNull(final Stream<String> input) {
            //Act
            final List<String> output = input.gather(Gatherers4j.uniquelyOccurringBy(Function.identity())).toList();

            // Assert
            assertThat(output).hasSize(1).containsNull();
        }

        @ParallelAndSequentialTest(values = {"A", "AA", "AAA", "AAAA", "AAAAA"})
        void emitsOriginalsFromMappedUniques(final Stream<String> input) {
            //Act
            final List<String> output = input.gather(Gatherers4j.uniquelyOccurringBy(String::length)).toList();

            // Assert
            assertThat(output).containsExactly("A", "AA", "AAA", "AAAA", "AAAAA");
        }

        @ParallelAndSequentialTest
        void emitsEmptyOnEmptyStream(final Stream<String> input) {
            // Act
            final List<String> output = input
                    .gather(Gatherers4j.uniquelyOccurringBy(Function.identity()))
                    .toList();

            // Assert
            assertThat(output).isEmpty();
        }

        @ParallelAndSequentialTest(values = {"A", "A", "B", "B", "C", "C", "D", "D"})
        void emitsEmptyOnNoUniqueItems(final Stream<String> input) {
            //Act
            final List<String> output = input.gather(Gatherers4j.uniquelyOccurringBy(String::length)).toList();

            // Assert
            assertThat(output).isEmpty();
        }

        @ParallelAndSequentialTest(values = {"A", "B", "C", "D", "B", "C"})
        void emitsInEncounterOrder(final Stream<String> input) {
            //Act
            final List<String> output = input.gather(Gatherers4j.uniquelyOccurringBy(Function.identity())).toList();

            // Assert
            assertThat(output).containsExactly("A", "D");
        }

        @ParallelAndSequentialTest(values = {"A", "B", "A", "A"})
        void filtersOutNonUnique(final Stream<String> input) {
            //Act
            final List<String> output = input.gather(Gatherers4j.uniquelyOccurringBy(Function.identity())).toList();

            // Assert
            assertThat(output).containsExactly("B");
        }

        @ParallelAndSequentialTest(values = {NULL, "B", NULL, NULL})
        void filtersOutNonUniqueNull(final Stream<String> input) {
            //Act
            final List<String> output = input.gather(Gatherers4j.uniquelyOccurringBy(Function.identity())).toList();

            // Assert
            assertThat(output).containsExactly("B");
        }

        @ParallelAndSequentialTest(values = {NULL, "A", "B", "B", "C", NULL, "D", "E", "E"})
        void mixtureWithSeveralUniquesIncludingNull(final Stream<String> input) {
            // Act
            final List<String> output = input
                    .gather(Gatherers4j.uniquelyOccurringBy(Function.identity()))
                    .toList();

            // Assert
            assertThat(output).containsExactly("A", "C", "D");
        }

        @ParallelAndSequentialTest(values = "A")
        void singleElementIsUnique(final Stream<String> input) {
            // Act
            final List<String> output = input
                    .gather(Gatherers4j.uniquelyOccurringBy(Function.identity()))
                    .toList();

            // Assert
            assertThat(output).containsExactly("A");
        }

        @ParallelAndSequentialTest(values = NULL)
        void singleNullIsUnique(final Stream<String> input) {
            // Act
            final List<String> output = input
                    .gather(Gatherers4j.uniquelyOccurringBy(Function.identity()))
                    .toList();

            // Assert
            assertThat(output).hasSize(1).containsNull();
        }

        @ParallelAndSequentialTest(values = {"A", "B", "C", "A"})
        void uniqueThatBecomesDuplicateIsRemoved(final Stream<String> input) {
            // Act
            final List<String> output = input
                    .gather(Gatherers4j.uniquelyOccurringBy(Function.identity()))
                    .toList();

            // Assert
            assertThat(output).containsExactly("B", "C");
        }

    }
}