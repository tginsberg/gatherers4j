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

import com.ginsberg.gatherers4j.enums.Order;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GroupChangingGathererTest {

    @Nested
    class Equals {

        @Test
        void emptyStream() {
            // Arrange
            final Stream<String> input = Stream.empty();

            // Act
            final List<List<String>> output = input.gather(Gatherers4j.group()).toList();

            // Assert
            assertThat(output).isEmpty();
        }


        @Test
        void groupingByIdentity() {
            // Arrange
            final Stream<String> input = Stream.of("A", "A", "B", "B", "C", "C", "C");

            // Act
            final List<List<String>> output = input.gather(Gatherers4j.group()).toList();

            // Assert
            assertThat(output).containsExactly(
                    List.of("A", "A"),
                    List.of("B", "B"),
                    List.of("C", "C", "C")
            );
        }

        @Test
        void nullsMatch() {
            // Arrange
            final Stream<String> input = Stream.of(null, null, "A");

            // Act
            final List<List<String>> output = input
                    .gather(Gatherers4j.group()).toList();

            // Assert
            assertThat(output)
                    .containsExactly(
                            Arrays.asList(null, null),
                            List.of("A")
                    );
        }

        @Test
        void returnedListUnmodifiable() {
            // Arrange
            final Stream<String> input = Stream.of("A", "A", "B", "B", "C", "C", "C");

            // Act
            final List<List<String>> output = input.gather(Gatherers4j.group()).toList();

            // Assert
            assertThat(output).hasSize(3);
            output.forEach(it ->
                    assertThatThrownBy(() ->
                            it.add("D")
                    ).isInstanceOf(UnsupportedOperationException.class)
            );
        }

    }

    @Nested
    class EqualsBy {

        @Test
        void emptyStream() {
            // Arrange
            final Stream<String> input = Stream.empty();

            // Act
            final List<List<String>> output = input.gather(Gatherers4j.groupBy(String::length)).toList();

            // Assert
            assertThat(output).isEmpty();
        }

        @Test
        void groupingByFunction() {
            // Arrange
            final Stream<String> input = Stream.of("A", "B", "AA", "BB", "CCC", "A", "BB", "CCC");

            // Act
            final List<List<String>> output = input.gather(Gatherers4j.groupBy(String::length)).toList();

            // Assert
            assertThat(output).containsExactly(
                    List.of("A", "B"),
                    List.of("AA", "BB"),
                    List.of("CCC"),
                    List.of("A"),
                    List.of("BB"),
                    List.of("CCC")
            );
        }

        @SuppressWarnings("DataFlowIssue")
        @Test
        void mappingFunctionMustNotBeNull() {
            assertThatThrownBy(() -> Gatherers4j.groupBy(null)).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void nullsMatch() {
            // Arrange
            final Stream<String> input = Stream.of(null, null, "A");

            // Act
            final List<List<String>> output = input
                    .gather(Gatherers4j.groupBy(it -> it == null ? null : it.length())).toList();

            // Assert
            assertThat(output)
                    .containsExactly(
                            Arrays.asList(null, null),
                            List.of("A")
                    );
        }

        @Test
        void singleElementStream() {
            // Arrange
            final Stream<String> input = Stream.of("A");

            // Act
            final List<List<String>> output = input.gather(Gatherers4j.groupBy(String::length)).toList();

            // Assert
            assertThat(output).containsExactly(
                    List.of("A")
            );
        }
    }

    @Nested
    class WithComparable {
        @Nested
        class Descending {

            @Test
            void descending() {
                // Arrange
                final Stream<Integer> input = Stream.of(3, 2, 1, 2, 2, 1);

                // Act
                final List<List<Integer>> output = input
                        .gather(Gatherers4j.groupOrdered(Order.Descending))
                        .toList();

                // Assert
                assertThat(output)
                        .containsExactly(
                                List.of(3, 2, 1),
                                List.of(2),
                                List.of(2, 1)
                        );
            }

            @Test
            void emptyStream() {
                // Arrange
                final Stream<String> input = Stream.empty();

                // Act
                final List<List<String>> output = input
                        .gather(Gatherers4j.groupOrdered(Order.Descending))
                        .toList();

                // Assert
                assertThat(output).isEmpty();
            }

            @Test
            void ensureDescending() {
                // Arrange
                final Stream<Integer> input = Stream.of(4, 3, 2, 1);

                // Act
                final List<Integer> output = input.gather(Gatherers4j.ensureOrdered(Order.Descending)).toList();

                // Assert
                assertThat(output).containsExactly(4, 3, 2, 1);
            }

            @Test
            void ensureDescendingFailureCase() {
                assertThatThrownBy(() ->
                        Stream.of(1, 1).gather(Gatherers4j.ensureOrdered(Order.Descending)).toList()
                ).isExactlyInstanceOf(IllegalStateException.class);
            }

            @Test
            void singleElementStream() {
                // Arrange
                final Stream<String> input = Stream.of("A");

                // Act
                final List<List<String>> output = input
                        .gather(Gatherers4j.groupOrdered(Order.Descending))
                        .toList();

                // Assert
                assertThat(output).containsExactly(List.of("A"));
            }
        }

        @Nested
        class Ascending {
            @Test
            void emptyStream() {
                // Arrange
                final Stream<String> input = Stream.empty();

                // Act
                final List<List<String>> output = input
                        .gather(Gatherers4j.groupOrdered(Order.Ascending))
                        .toList();

                // Assert
                assertThat(output).isEmpty();
            }

            @Test
            void ensureAscending() {
                // Arrange
                final Stream<Integer> input = Stream.of(1, 2, 3, 4);

                // Act
                final List<Integer> output = input.gather(Gatherers4j.ensureOrdered(Order.Ascending)).toList();

                // Assert
                assertThat(output).containsExactly(1, 2, 3, 4);
            }

            @Test
            void ensureAscendingFailureCase() {
                assertThatThrownBy(() ->
                        Stream.of(1, 1).gather(Gatherers4j.ensureOrdered(Order.Ascending)).toList()
                ).isExactlyInstanceOf(IllegalStateException.class);
            }

            @Test
            void ascending() {
                // Arrange
                final Stream<Integer> input = Stream.of(1, 2, 3, 3, 2, 3);

                // Act
                final List<List<Integer>> output = input
                        .gather(Gatherers4j.groupOrdered(Order.Ascending))
                        .toList();

                // Assert
                assertThat(output)
                        .containsExactly(
                                List.of(1, 2, 3),
                                List.of(3),
                                List.of(2, 3)
                        );
            }

            @Test
            void singleElementStream() {
                // Arrange
                final Stream<String> input = Stream.of("A");

                // Act
                final List<List<String>> output = input
                        .gather(Gatherers4j.groupOrdered(Order.Ascending))
                        .toList();

                // Assert
                assertThat(output).containsExactly(List.of("A"));
            }
        }

        @Nested
        class AscendingOrEqual {
            @Test
            void emptyStream() {
                // Arrange
                final Stream<String> input = Stream.empty();

                // Act
                final List<List<String>> output = input
                        .gather(Gatherers4j.groupOrdered(Order.AscendingOrEqual))
                        .toList();

                // Assert
                assertThat(output).isEmpty();
            }

            @Test
            void ensureAscendingOrEqual() {
                // Arrange
                final Stream<Integer> input = Stream.of(4, 4, 5, 6);

                // Act
                final List<Integer> output = input.gather(Gatherers4j.ensureOrdered(Order.AscendingOrEqual)).toList();

                // Assert
                assertThat(output).containsExactly(4, 4, 5, 6);
            }

            @Test
            void ensureAscendingOrEqualFailureCase() {
                assertThatThrownBy(() ->
                        Stream.of(1, 0).gather(Gatherers4j.ensureOrdered(Order.AscendingOrEqual)).toList()
                ).isExactlyInstanceOf(IllegalStateException.class);
            }

            @Test
            void ascendingOrEqual() {
                // Arrange
                final Stream<Integer> input = Stream.of(1, 2, 3, 3, 2, 3);

                // Act
                final List<List<Integer>> output = input
                        .gather(Gatherers4j.groupOrdered(Order.AscendingOrEqual))
                        .toList();

                // Assert
                assertThat(output)
                        .containsExactly(
                                List.of(1, 2, 3, 3),
                                List.of(2, 3)
                        );
            }

            @Test
            void singleElementStream() {
                // Arrange
                final Stream<String> input = Stream.of("A");

                // Act
                final List<List<String>> output = input
                        .gather(Gatherers4j.groupOrdered(Order.AscendingOrEqual))
                        .toList();

                // Assert
                assertThat(output).containsExactly(List.of("A"));
            }
        }

        @Nested
        class DescendingOrEqual {
            @Test
            void emptyStream() {
                // Arrange
                final Stream<String> input = Stream.empty();

                // Act
                final List<List<String>> output = input
                        .gather(Gatherers4j.groupOrdered(Order.DescendingOrEqual))
                        .toList();

                // Assert
                assertThat(output).isEmpty();
            }

            @Test
            void ensureDescendingOrEqual() {
                // Arrange
                final Stream<Integer> input = Stream.of(4, 3, 2, 2);

                // Act
                final List<Integer> output = input.gather(Gatherers4j.ensureOrdered(Order.DescendingOrEqual)).toList();

                // Assert
                assertThat(output).containsExactly(4, 3, 2, 2);
            }

            @Test
            void ensureDescendingOrEqualFailureCase() {
                assertThatThrownBy(() ->
                        Stream.of(1, 2).gather(Gatherers4j.ensureOrdered(Order.DescendingOrEqual)).toList()
                ).isExactlyInstanceOf(IllegalStateException.class);
            }

            @Test
            void descendingOrEqual() {
                // Arrange
                final Stream<Integer> input = Stream.of(3, 2, 1, 2, 2, 1);

                // Act
                final List<List<Integer>> output = input
                        .gather(Gatherers4j.groupOrdered(Order.DescendingOrEqual))
                        .toList();

                // Assert
                assertThat(output)
                        .containsExactly(
                                List.of(3, 2, 1),
                                List.of(2, 2, 1)
                        );
            }

            @Test
            void singleElementStream() {
                // Arrange
                final Stream<String> input = Stream.of("A");

                // Act
                final List<List<String>> output = input
                        .gather(Gatherers4j.groupOrdered(Order.DescendingOrEqual))
                        .toList();

                // Assert
                assertThat(output).containsExactly(List.of("A"));
            }

        }
    }

    @Nested
    class WithComparator {

        @Nested
        class Common {
            @SuppressWarnings("DataFlowIssue")
            @Test
            void comparatorMustNotBeNull() {
                assertThatThrownBy(() ->
                        new GroupChangingGatherer<>(Order.Ascending, null)
                ).isExactlyInstanceOf(IllegalArgumentException.class);
            }

            @SuppressWarnings("DataFlowIssue")
            @Test
            void operationMustNotBeNull() {
                assertThatThrownBy(() ->
                        new GroupChangingGatherer<>(null, (_, _) -> 0)
                ).isExactlyInstanceOf(IllegalArgumentException.class);
            }

            @Test
            void returnedListUnmodifiable() {
                // Arrange
                final Stream<String> input = Stream.of("A", "B", "C");

                // Act
                final List<List<String>> output = input
                        .gather(new GroupChangingGatherer<>(Order.Ascending, Comparator.comparing(String::length)))
                        .toList();

                // Assert
                assertThat(output).hasSize(3);
                output.forEach(it ->
                        assertThatThrownBy(() ->
                                it.add("D")
                        ).isInstanceOf(UnsupportedOperationException.class)
                );
            }
        }

        @Nested
        class Descending {
            @Test
            void descending() {
                // Arrange
                final Stream<String> input = Stream.of("AAA", "AA", "A", "AA", "AA", "A");

                // Act
                final List<List<String>> output = input
                        .gather(Gatherers4j.groupOrderedBy(Order.Descending, Comparator.comparingInt(String::length)))
                        .toList();

                // Assert
                assertThat(output)
                        .containsExactly(
                                List.of("AAA", "AA", "A"),
                                List.of("AA"),
                                List.of("AA", "A")
                        );
            }

            @Test
            void emptyStream() {
                // Arrange
                final Stream<String> input = Stream.empty();

                // Act
                final List<List<String>> output = input
                        .gather(Gatherers4j.groupOrderedBy(Order.Descending, Comparator.comparingInt(String::length)))
                        .toList();

                // Assert
                assertThat(output).isEmpty();
            }

            @Test
            void ensureDecreasingFailureCase() {
                assertThatThrownBy(() ->
                        Stream.of("A", "AA")
                                .gather(Gatherers4j.ensureOrderedBy(Order.Descending, Comparator.comparingInt(String::length)))
                                .toList()
                ).isExactlyInstanceOf(IllegalStateException.class);
            }

            @Test
            void ensureOrderedDescending() {
                // Arrange
                final Stream<String> input = Stream.of("AAA", "AA", "A");

                // Act
                final List<String> output = input
                        .gather(Gatherers4j.ensureOrderedBy(Order.Descending, Comparator.comparingInt(String::length)))
                        .toList();

                // Assert
                assertThat(output).containsExactly("AAA", "AA", "A");
            }

            @Test
            void singleElementStream() {
                // Arrange
                final Stream<String> input = Stream.of("A");

                // Act
                final List<List<String>> output = input
                        .gather(Gatherers4j.groupOrderedBy(Order.Descending, Comparator.comparingInt(String::length)))
                        .toList();

                // Assert
                assertThat(output).containsExactly(List.of("A"));
            }
        }

        @Nested
        class Ascending {
            @Test
            void emptyStream() {
                // Arrange
                final Stream<String> input = Stream.empty();

                // Act
                final List<List<String>> output = input
                        .gather(Gatherers4j.groupOrderedBy(Order.Ascending, Comparator.comparingInt(String::length)))
                        .toList();

                // Assert
                assertThat(output).isEmpty();
            }

            @Test
            void ensureAscending() {
                // Arrange
                final Stream<String> input = Stream.of("A", "AA", "AAA");

                // Act
                final List<String> output = input
                        .gather(Gatherers4j.ensureOrderedBy(Order.Ascending, Comparator.comparingInt(String::length)))
                        .toList();

                // Assert
                assertThat(output).containsExactly("A", "AA", "AAA");
            }

            @Test
            void ensureAscendingFailureCase() {
                assertThatThrownBy(() ->
                        Stream.of("AA", "A")
                                .gather(Gatherers4j.ensureOrderedBy(Order.Ascending, Comparator.comparingInt(String::length)))
                                .toList()
                ).isExactlyInstanceOf(IllegalStateException.class);
            }

            @Test
            void ascending() {
                // Arrange
                final Stream<String> input = Stream.of("A", "AA", "AAA", "AAA", "AA", "AAA");

                // Act
                final List<List<String>> output = input
                        .gather(Gatherers4j.groupOrderedBy(Order.Ascending, Comparator.comparingInt(String::length)))
                        .toList();

                // Assert
                assertThat(output)
                        .containsExactly(
                                List.of("A", "AA", "AAA"),
                                List.of("AAA"),
                                List.of("AA", "AAA")
                        );
            }

            @Test
            void singleElementStream() {
                // Arrange
                final Stream<String> input = Stream.of("A");

                // Act
                final List<List<String>> output = input
                        .gather(Gatherers4j.groupOrderedBy(Order.Ascending, Comparator.comparingInt(String::length)))
                        .toList();

                // Assert
                assertThat(output).containsExactly(List.of("A"));
            }
        }

        @Nested
        class AscendingOrEqual {

            @Test
            void emptyStream() {
                // Arrange
                final Stream<String> input = Stream.empty();

                // Act
                final List<List<String>> output = input
                        .gather(Gatherers4j.groupOrderedBy(Order.AscendingOrEqual, Comparator.comparingInt(String::length)))
                        .toList();

                // Assert
                assertThat(output).isEmpty();
            }

            @Test
            void ensureAscendingOrEqual() {
                // Arrange
                final Stream<String> input = Stream.of("A", "A", "A");

                // Act
                final List<String> output = input
                        .gather(Gatherers4j.ensureOrderedBy(Order.AscendingOrEqual, Comparator.comparingInt(String::length)))
                        .toList();

                // Assert
                assertThat(output).containsExactly("A", "A", "A");
            }

            @Test
            void ensureAscendingOrEqualFailureCase() {
                assertThatThrownBy(() ->
                        Stream.of("AA", "A")
                                .gather(Gatherers4j.ensureOrderedBy(Order.AscendingOrEqual, Comparator.comparingInt(String::length)))
                                .toList()
                ).isExactlyInstanceOf(IllegalStateException.class);
            }

            @Test
            void nonDecreasing() {
                // Arrange
                final Stream<String> input = Stream.of("A", "AA", "AAA", "AAA", "AA", "AAA");

                // Act
                final List<List<String>> output = input
                        .gather(Gatherers4j.groupOrderedBy(Order.AscendingOrEqual, Comparator.comparingInt(String::length)))
                        .toList();

                // Assert
                assertThat(output)
                        .containsExactly(
                                List.of("A", "AA", "AAA", "AAA"),
                                List.of("AA", "AAA")
                        );
            }

            @Test
            void singleElementStream() {
                // Arrange
                final Stream<String> input = Stream.of("A");

                // Act
                final List<List<String>> output = input
                        .gather(Gatherers4j.groupOrderedBy(Order.AscendingOrEqual, Comparator.comparingInt(String::length)))
                        .toList();

                // Assert
                assertThat(output).containsExactly(List.of("A"));
            }
        }

        @Nested
        class DescendingOrEqual {
            @Test
            void emptyStream() {
                // Arrange
                final Stream<String> input = Stream.empty();

                // Act
                final List<List<String>> output = input
                        .gather(Gatherers4j.groupOrderedBy(Order.DescendingOrEqual, Comparator.comparingInt(String::length)))
                        .toList();

                // Assert
                assertThat(output).isEmpty();
            }

            @Test
            void ensureDescendingOrEqual() {
                // Arrange
                final Stream<String> input = Stream.of("A", "A", "A");

                // Act
                final List<String> output = input
                        .gather(Gatherers4j.ensureOrderedBy(Order.DescendingOrEqual, Comparator.comparingInt(String::length)))
                        .toList();

                // Assert
                assertThat(output).containsExactly("A", "A", "A");
            }

            @Test
            void ensureDescendingOrEqualFailureCase() {
                assertThatThrownBy(() ->
                        Stream.of("AA", "AAA")
                                .gather(Gatherers4j.ensureOrderedBy(Order.DescendingOrEqual, Comparator.comparingInt(String::length)))
                                .toList()
                ).isExactlyInstanceOf(IllegalStateException.class);
            }

            @Test
            void descendingOrEqual() {
                // Arrange
                final Stream<String> input = Stream.of("AAA", "AA", "A", "AA", "AA", "A");

                // Act
                final List<List<String>> output = input
                        .gather(Gatherers4j.groupOrderedBy(Order.DescendingOrEqual, Comparator.comparingInt(String::length)))
                        .toList();

                // Assert
                assertThat(output)
                        .containsExactly(
                                List.of("AAA", "AA", "A"),
                                List.of("AA", "AA", "A")
                        );
            }

            @Test
            void singleElementStream() {
                // Arrange
                final Stream<String> input = Stream.of("A");

                // Act
                final List<List<String>> output = input
                        .gather(Gatherers4j.groupOrderedBy(Order.DescendingOrEqual, Comparator.comparingInt(String::length)))
                        .toList();

                // Assert
                assertThat(output).containsExactly(List.of("A"));
            }
        }
    }

}