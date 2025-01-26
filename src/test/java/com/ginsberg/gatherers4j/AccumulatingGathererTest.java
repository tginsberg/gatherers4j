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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AccumulatingGathererTest {

    @Nested
    class Fold {
        @Test
        @SuppressWarnings("DataFlowIssue")
        void accumulatorFunctionMustNotBeNull() {
            assertThatThrownBy(() -> Gatherers4j.foldIndexed(() -> "A", null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void foldWithIndex() {
            // Arrange
            final Stream<String> input = Stream.of("A", "B", "C", "D");

            // Act
            final List<IndexedValue<String>> output = input
                    .gather(
                            Gatherers4j.foldIndexed(
                                    () -> new ArrayList<IndexedValue<String>>(),
                                    (index, carry, next) -> {
                                        assert carry != null;
                                        carry.add(new IndexedValue<>(index, next));
                                        return carry;
                                    }
                            )
                    )
                    .toList()
                    .getFirst();

            // Assert
            assertThat(output)
                    .containsExactly(
                            new IndexedValue<>(0, "A"),
                            new IndexedValue<>(1, "B"),
                            new IndexedValue<>(2, "C"),
                            new IndexedValue<>(3, "D")
                    );
        }

        @Test
        @SuppressWarnings("DataFlowIssue")
        void initialSupplierMustNotBeNull() {
            assertThatThrownBy(() -> Gatherers4j.foldIndexed(null, (_, _, it) -> it))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class Scan {
        @Test
        @SuppressWarnings("DataFlowIssue")
        void initialValueMustNotBeNull() {
            assertThatThrownBy(() -> Gatherers4j.scanIndexed(null, (_, _, _) -> 0)).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @SuppressWarnings("DataFlowIssue")
        void scanIndexFunctionMustNotBeNull() {
            assertThatThrownBy(() -> Gatherers4j.scanIndexed(() -> "", null)).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void scanWithIndex() {
            // Arrange
            final Stream<String> input = Stream.of("A", "B", "C");

            // Act
            final List<String> output = input
                    .gather(Gatherers4j.scanIndexed(
                            () -> "",
                            (index, carry, next) -> carry + next + index
                    )).toList();

            // Assert
            assertThat(output)
                    .containsExactly("A0", "A0B1", "A0B1C2");
        }

        @Test
        void scanWithIndexEmpty() {
            // Arrange
            final Stream<String> input = Stream.empty();

            // Act
            final List<String> output = input
                    .gather(Gatherers4j.scanIndexed(
                            () -> "",
                            (index, carry, next) -> carry + next + index
                    )).toList();

            // Assert
            assertThat(output).isEmpty();
        }
    }
}
