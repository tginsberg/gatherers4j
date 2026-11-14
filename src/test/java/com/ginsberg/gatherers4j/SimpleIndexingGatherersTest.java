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

import com.ginsberg.gatherers4j.dto.WithIndex;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class SimpleIndexingGatherersTest {

    @Nested
    class DropWhileIndexed {

        @Test
        void dropWhileIndexed() {
            // Arrange
            final Stream<String> input = Stream.of("A", "B", "C", "D", "E");

            // Act
            final List<String> output = input
                    .gather(Gatherers4j.dropWhileIndexed((index, element) -> index < 2 || element.equals("C")))
                    .toList();

            // Assert
            assertThat(output).containsExactly("D", "E");
        }

        @Test
        void dropWhileIndexedAllDropped() {
            // Arrange
            final Stream<String> input = Stream.of("A", "B", "C");

            // Act
            final List<String> output = input.gather(Gatherers4j.dropWhileIndexed((_, _) -> true)).toList();

            // Assert
            assertThat(output).isEmpty();
        }

        @Test
        void dropWhileIndexedEmpty() {
            // Arrange
            final Stream<String> input = Stream.empty();

            // Act
            final List<String> output = input.gather(Gatherers4j.dropWhileIndexed((_, _) -> true)).toList();

            // Assert
            assertThat(output).isEmpty();
        }

        @Test
        void dropWhileIndexedNoneDropped() {
            // Arrange
            final Stream<String> input = Stream.of("A", "B", "C");

            // Act
            final List<String> output = input.gather(Gatherers4j.dropWhileIndexed((_, _) -> false)).toList();

            // Assert
            assertThat(output).containsExactly("A", "B", "C");
        }

        @Test
        void dropWhileIndexedNullElements() {
            // Arrange
            final Stream<String> input = Stream.of(null, "A", null, "B");

            // Act
            final List<String> output = input
                    .gather(Gatherers4j.dropWhileIndexed((_, element) -> element == null))
                    .toList();

            // Assert
            assertThat(output).containsExactly("A", null, "B");
        }

        @Test
        void dropWhileIndexedSingleElement() {
            // Arrange
            final Stream<String> input = Stream.of("A");

            // Act
            final List<String> output = input.gather(Gatherers4j.dropWhileIndexed((index, _) -> index > 0)).toList();

            // Assert
            assertThat(output).containsExactly("A");
        }

        @Test
        void dropWhileIndexedStopsWhenDownstreamStops() {
            // Arrange
            final List<Integer> pulled = new ArrayList<>();
            final Stream<Integer> input = Stream.iterate(0, i -> i + 1).peek(pulled::add);

            // Act
            final List<Integer> output = input
                    .gather(Gatherers4j.dropWhileIndexed((index, _) -> index < 2))
                    .limit(2)
                    .toList();

            // Assert
            assertThat(output).containsExactly(2, 3);
            assertThat(pulled).containsExactly(0, 1, 2, 3);
        }

        @Test
        void predicateIsNotCalledAfterItFails() {
            // Arrange
            final Stream<String> input = Stream.of("A", "B", "C", "D");
            final List<Long> indices = new ArrayList<>();

            // Act
            final List<String> output = input
                    .gather(Gatherers4j.dropWhileIndexed((index, _) -> {
                        indices.add(index);
                        return index < 1;
                    }))
                    .toList();

            // Assert
            assertThat(output).containsExactly("B", "C", "D");
            assertThat(indices).containsExactly(0L, 1L);
        }

        @SuppressWarnings("DataFlowIssue")
        @Test
        void predicateMustNotBeNull() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Gatherers4j.dropWhileIndexed(null))
                    .withMessage("Predicate must not be null");
        }

        @Test
        void reusedAcrossStreams() {
            // Arrange
            final var gatherer = Gatherers4j.<String>dropWhileIndexed((index, _) -> index < 1);

            // Act
            final List<String> first = Stream.of("A", "B").gather(gatherer).toList();
            final List<String> second = Stream.of("C", "D").gather(gatherer).toList();

            // Assert
            assertThat(first).containsExactly("B");
            assertThat(second).containsExactly("D");
        }
    }

    @Nested
    class FilterIndexed {

        @Test
        void filterWithIndex() {
            // Arrange
            final Stream<String> input = Stream.of("A", "B", "C", "D");

            // Act
            final List<String> output = input
                    .gather(Gatherers4j.filterIndexed((index, element) ->
                            index % 2 == 0 || element.equals("D"))
                    )
                    .toList();

            // Assert
            assertThat(output).containsExactly("A", "C", "D");
        }

        @Test
        void filteringIncrementsIndex() {
            // Arrange
            final Stream<String> input = Stream.of("A", "B", "C", "D");
            final List<Long> indices = new ArrayList<>();

            // Act
            final List<String> output = input
                    .gather(Gatherers4j.filterIndexed((index, _) -> {
                                indices.add(index);
                                return index % 2 == 0;
                            }
                    )).toList();

            assertThat(output).containsExactly("A", "C");
            assertThat(indices).containsExactly(0L, 1L, 2L, 3L);
        }

        @SuppressWarnings("DataFlowIssue")
        @Test
        void predicateMustNotBeNull() {
            assertThatIllegalArgumentException().isThrownBy(() ->
                    Gatherers4j.filterIndexed(null)
            );
        }
    }

    @Nested
    class FlatMapIndexed {

        @Test
        void flatMapIndexed() {
            // Arrange
            final Stream<String> input = Stream.of("A", "B", "C");

            // Act
            final List<String> output = input
                    .gather(Gatherers4j.flatMapIndexed((index, element) ->
                            Stream.generate(() -> element).limit(index + 1)
                    ))
                    .toList();

            // Assert
            assertThat(output).containsExactly("A", "B", "B", "C", "C", "C");
        }

        @Test
        void flatMapIndexedClosesEachMappedStream() {
            // Arrange
            final AtomicInteger closed = new AtomicInteger();
            final Stream<String> input = Stream.of("A", "B");

            // Act
            final List<String> output = input
                    .gather(Gatherers4j.flatMapIndexed((_, element) -> Stream.of(element).onClose(closed::incrementAndGet)))
                    .toList();

            // Assert
            assertThat(output).containsExactly("A", "B");
            assertThat(closed).hasValue(2);
        }

        @Test
        void flatMapIndexedEmpty() {
            // Arrange
            final Stream<String> input = Stream.empty();

            // Act
            final List<String> output = input.gather(Gatherers4j.flatMapIndexed((_, element) -> Stream.of(element))).toList();

            // Assert
            assertThat(output).isEmpty();
        }

        @Test
        void flatMapIndexedEmptyAndNullResultsStillAdvanceIndex() {
            // Arrange
            final Stream<String> input = Stream.of("A", "B", "C", "D");

            // Act
            final List<String> output = input
                    .gather(Gatherers4j.flatMapIndexed((index, element) -> switch (index.intValue()) {
                        case 0 -> Stream.empty();
                        case 1 -> null;
                        default -> Stream.of(element + index);
                    }))
                    .toList();

            // Assert
            assertThat(output).containsExactly("C2", "D3");
        }

        @Test
        void flatMapIndexedNullElements() {
            // Arrange
            final Stream<String> input = Stream.of(null, "A");

            // Act
            final List<String> output = input
                    .gather(Gatherers4j.flatMapIndexed((index, element) -> Stream.of(element, String.valueOf(index))))
                    .toList();

            // Assert
            assertThat(output).containsExactly(null, "0", "A", "1");
        }

        @Test
        void flatMapIndexedSingleElement() {
            // Arrange
            final Stream<String> input = Stream.of("A");

            // Act
            final List<String> output = input
                    .gather(Gatherers4j.flatMapIndexed((index, element) -> Stream.of(element, element + index)))
                    .toList();

            // Assert
            assertThat(output).containsExactly("A", "A0");
        }

        @Test
        void flatMapIndexedStopsWhenDownstreamStops() {
            // Arrange
            final List<String> pulled = new ArrayList<>();
            final AtomicInteger closed = new AtomicInteger();
            final Stream<String> input = Stream.of("A", "B").peek(pulled::add);

            // Act
            final List<String> output = input
                    .gather(Gatherers4j.flatMapIndexed((_, element) ->
                            Stream.generate(() -> element).onClose(closed::incrementAndGet)
                    ))
                    .limit(3)
                    .toList();

            // Assert
            assertThat(output).containsExactly("A", "A", "A");
            assertThat(pulled).containsExactly("A");
            assertThat(closed).hasValue(1);
        }

        @SuppressWarnings("DataFlowIssue")
        @Test
        void mappingFunctionMustNotBeNull() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Gatherers4j.flatMapIndexed(null))
                    .withMessage("mappingFunction must not be null");
        }
    }

    @Nested
    class MapIndexed {
        @SuppressWarnings("DataFlowIssue")
        @Test
        void mappingFunctionMustNotBeNull() {
            assertThatIllegalArgumentException().isThrownBy(() ->
                    Gatherers4j.mapIndexed(null)
            );
        }

        @Test
        void mapIndexedEmpty() {
            // Arrange
            final Stream<String> input = Stream.empty();

            // Act
            final List<String> output = input.gather(Gatherers4j.mapIndexed((_, element) -> element)).toList();

            // Assert
            assertThat(output).isEmpty();
        }

        @Test
        void mapIndexed() {
            // Arrange
            final Stream<String> input = Stream.of("A", "B", "C");

            // Act
            final List<String> output = input.gather(Gatherers4j.mapIndexed((index, element) -> element + index)).toList();

            // Assert
            assertThat(output).containsExactly("A0", "B1", "C2");
        }

    }

    @Nested
    class PeekIndexed {
        @SuppressWarnings("DataFlowIssue")
        @Test
        void peekingFunctionMustNotBeNull() {
            assertThatIllegalArgumentException().isThrownBy(() ->
                    Gatherers4j.peekIndexed(null)
            );
        }

        @Test
        void peekIndexedEmpty() {
            // Arrange
            final Stream<String> input = Stream.empty();
            final List<String> peeked = new ArrayList<>();

            // Act
            final List<String> output = input.gather(Gatherers4j.peekIndexed((index, element) -> peeked.add(element + index))).toList();

            // Assert
            assertThat(output).isEmpty();
            assertThat(peeked).isEmpty();
        }

        @Test
        void peekIndexed() {
            // Arrange
            final Stream<String> input = Stream.of("A", "B", "C");
            final List<String> peeked = new ArrayList<>();

            // Act
            final List<String> output = input.gather(Gatherers4j.peekIndexed((index, element) -> peeked.add(element + index))).toList();

            // Assert
            assertThat(output).containsExactly("A", "B", "C");
            assertThat(peeked).containsExactly("A0", "B1", "C2");
        }

    }

    @Nested
    class TakeWhileIndexed {

        @SuppressWarnings("DataFlowIssue")
        @Test
        void predicateMustNotBeNull() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Gatherers4j.takeWhileIndexed(null))
                    .withMessage("Predicate must not be null");
        }

        @Test
        void reusedAcrossStreams() {
            // Arrange
            final var gatherer = Gatherers4j.<String>takeWhileIndexed((index, _) -> index < 1);

            // Act
            final List<String> first = Stream.of("A", "B").gather(gatherer).toList();
            final List<String> second = Stream.of("C", "D").gather(gatherer).toList();

            // Assert
            assertThat(first).containsExactly("A");
            assertThat(second).containsExactly("C");
        }

        @Test
        void takeWhileIndexed() {
            // Arrange
            final Stream<String> input = Stream.of("A", "B", "C", "D", "E");

            // Act
            final List<String> output = input
                    .gather(Gatherers4j.takeWhileIndexed((index, element) -> index < 3 && !element.equals("C")))
                    .toList();

            // Assert
            assertThat(output).containsExactly("A", "B");
        }

        @Test
        void takeWhileIndexedAllTaken() {
            // Arrange
            final Stream<String> input = Stream.of("A", "B", "C");

            // Act
            final List<String> output = input.gather(Gatherers4j.takeWhileIndexed((_, _) -> true)).toList();

            // Assert
            assertThat(output).containsExactly("A", "B", "C");
        }

        @Test
        void takeWhileIndexedEmpty() {
            // Arrange
            final Stream<String> input = Stream.empty();

            // Act
            final List<String> output = input.gather(Gatherers4j.takeWhileIndexed((_, _) -> true)).toList();

            // Assert
            assertThat(output).isEmpty();
        }

        @Test
        void takeWhileIndexedInfiniteStream() {
            // Arrange
            final Stream<Integer> input = Stream.iterate(0, i -> i + 1);

            // Act
            final List<Integer> output = input
                    .gather(Gatherers4j.takeWhileIndexed((index, _) -> index < 3))
                    .toList();

            // Assert
            assertThat(output).containsExactly(0, 1, 2);
        }

        @Test
        void takeWhileIndexedNullElements() {
            // Arrange
            final Stream<String> input = Stream.of(null, "A", "B", null);

            // Act
            final List<String> output = input
                    .gather(Gatherers4j.takeWhileIndexed((_, element) -> element == null || element.equals("A")))
                    .toList();

            // Assert
            assertThat(output).containsExactly(null, "A");
        }

        @Test
        void takeWhileIndexedSingleElement() {
            // Arrange
            final Stream<String> input = Stream.of("A");

            // Act
            final List<String> output = input.gather(Gatherers4j.takeWhileIndexed((index, _) -> index == 0)).toList();

            // Assert
            assertThat(output).containsExactly("A");
        }

        @Test
        void takeWhileIndexedStopsReadingAtFirstFailure() {
            // Arrange
            final List<String> pulled = new ArrayList<>();
            final Stream<String> input = Stream.of("A", "B", "C", "D").peek(pulled::add);

            // Act
            final List<String> output = input
                    .gather(Gatherers4j.takeWhileIndexed((index, _) -> index < 1))
                    .toList();

            // Assert
            assertThat(output).containsExactly("A");
            assertThat(pulled).containsExactly("A", "B");
        }

        @Test
        void takeWhileIndexedStopsWhenDownstreamStops() {
            // Arrange
            final List<String> pulled = new ArrayList<>();
            final Stream<String> input = Stream.of("A", "B", "C", "D").peek(pulled::add);

            // Act
            final List<String> output = input
                    .gather(Gatherers4j.takeWhileIndexed((_, _) -> true))
                    .limit(2)
                    .toList();

            // Assert
            assertThat(output).containsExactly("A", "B");
            assertThat(pulled).containsExactly("A", "B");
        }
    }

    @Nested
    class WithIndexes {
        @Test
        void objectWithIndex() {
            // Arrange
            final Stream<String> input = Stream.of("A", "B", "C");

            // Act
            final List<WithIndex<String>> output = input
                    .gather(Gatherers4j.withIndex())
                    .toList();

            // Assert
            assertThat(output)
                    .containsExactly(
                            new WithIndex<>(0, "A"),
                            new WithIndex<>(1, "B"),
                            new WithIndex<>(2, "C")
                    );
        }

        @Test
        void integerWithIndex() {
            // Arrange
            final Stream<Integer> input = Stream.of(1, 2, 3);

            // Act
            final List<WithIndex<Integer>> output = input
                    .gather(Gatherers4j.withIndex())
                    .toList();

            // Assert
            assertThat(output)
                    .containsExactly(
                            new WithIndex<>(0, 1),
                            new WithIndex<>(1, 2),
                            new WithIndex<>(2, 3)
                    );
        }
    }

    @Nested
    class Wildcards {

        @Test
        void dropWhileIndexedBroaderPredicateKeepsElementType() {
            // Arrange
            final BiPredicate<Number, CharSequence> firstOnly = (index, _) -> index.longValue() < 1;

            // Act
            final List<String> output = Stream.of("A", "B").gather(Gatherers4j.dropWhileIndexed(firstOnly)).toList();

            // Assert
            assertThat(output).containsExactly("B");
        }

        @Test
        void filterIndexedBroaderPredicateKeepsElementType() {
            // Arrange
            final BiPredicate<Number, CharSequence> evenIndex = (index, _) -> index.longValue() % 2 == 0;

            // Act
            final List<String> output = Stream.of("A", "B", "C").gather(Gatherers4j.filterIndexed(evenIndex)).toList();

            // Assert
            assertThat(output).containsExactly("A", "C");
        }

        @Test
        void flatMapIndexedBroaderFunctionKeepsOutputType() {
            // Arrange
            final BiFunction<Number, CharSequence, Stream<String>> withIndex =
                    (index, cs) -> Stream.of(cs.toString(), String.valueOf(index));

            // Act
            final List<CharSequence> output = Stream.of("A", "B")
                    .gather(Gatherers4j.<String, CharSequence>flatMapIndexed(withIndex))
                    .toList();

            // Assert
            assertThat(output).containsExactly("A", "0", "B", "1");
        }

        @Test
        void mapIndexedBroaderFunctionKeepsOutputType() {
            // Arrange
            final BiFunction<Number, CharSequence, String> withIndex = (index, cs) -> cs + "" + index;

            // Act
            final List<String> output = Stream.of("A", "B").gather(Gatherers4j.mapIndexed(withIndex)).toList();

            // Assert
            assertThat(output).containsExactly("A0", "B1");
        }

        @Test
        void peekIndexedBroaderConsumerKeepsElementType() {
            // Arrange
            final List<String> peeked = new ArrayList<>();
            final BiConsumer<Number, CharSequence> recorder = (index, cs) -> peeked.add(cs + "" + index);

            // Act
            final List<String> output = Stream.of("A", "B").gather(Gatherers4j.peekIndexed(recorder)).toList();

            // Assert
            assertThat(output).containsExactly("A", "B");
            assertThat(peeked).containsExactly("A0", "B1");
        }

        @Test
        void takeWhileIndexedBroaderPredicateKeepsElementType() {
            // Arrange
            final BiPredicate<Number, CharSequence> firstOnly = (index, _) -> index.longValue() < 1;

            // Act
            final List<String> output = Stream.of("A", "B").gather(Gatherers4j.takeWhileIndexed(firstOnly)).toList();

            // Assert
            assertThat(output).containsExactly("A");
        }
    }
}