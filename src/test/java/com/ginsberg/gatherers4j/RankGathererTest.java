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

import com.ginsberg.gatherers4j.dto.WithRank;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Gatherer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class RankGathererTest {

    private static List<WithRank<Integer>> ranked(final Gatherer<Integer, ?, WithRank<Integer>> gatherer, final Integer... input) {
        return Stream.of(input).gather(gatherer).toList();
    }

    private static List<Long> ranksOf(final List<? extends WithRank<?>> ranked) {
        return ranked.stream().map(WithRank::rank).toList();
    }

    @Nested
    class Rank {

        @Test
        void rank() {
            // Arrange
            final Stream<Integer> input = Stream.of(10, 20, 20, 30);

            // Act
            final List<WithRank<Integer>> output = input.gather(Gatherers4j.rank()).toList();

            // Assert
            assertThat(output).containsExactly(
                    new WithRank<>(1, 10),
                    new WithRank<>(2, 20),
                    new WithRank<>(2, 20),
                    new WithRank<>(4, 30)
            );
        }

        @Test
        void rankEmptyStream() {
            assertThat(ranked(Gatherers4j.rank())).isEmpty();
        }

        @Test
        void rankSingleElement() {
            assertThat(ranked(Gatherers4j.rank(), 5)).containsExactly(new WithRank<>(1, 5));
        }

        @Test
        void rankAllTied() {
            assertThat(ranksOf(ranked(Gatherers4j.rank(), 7, 7, 7))).containsExactly(1L, 1L, 1L);
        }

        @Test
        void rankNoTies() {
            assertThat(ranksOf(ranked(Gatherers4j.rank(), 1, 2, 3))).containsExactly(1L, 2L, 3L);
        }

        @Test
        void rankSkipsBySizeOfEachTie() {
            // Arrange / Act
            final List<Long> ranks = ranksOf(ranked(Gatherers4j.rank(), 1, 1, 1, 2, 3, 3, 4));

            // Assert
            assertThat(ranks).containsExactly(1L, 1L, 1L, 4L, 5L, 5L, 7L);
        }

        @Test
        void rankDescendingInput() {
            assertThat(ranksOf(ranked(Gatherers4j.rank(), 30, 20, 20, 10))).containsExactly(1L, 2L, 2L, 4L);
        }

        @Test
        void rankUnsortedInputIsNotDetected() {
            assertThat(ranksOf(ranked(Gatherers4j.rank(), 10, 20, 10))).containsExactly(1L, 2L, 3L);
        }

        @Test
        void rankWorksOnInfiniteStreams() {
            // Arrange
            final Stream<Integer> input = Stream.iterate(0, i -> i + 1).flatMap(i -> Stream.of(i, i));

            // Act
            final List<Long> ranks = ranksOf(input.gather(Gatherers4j.rank()).limit(6).toList());

            // Assert
            assertThat(ranks).containsExactly(1L, 1L, 3L, 3L, 5L, 5L);
        }

        @Test
        void rankGathererCanBeReusedAcrossStreams() {
            // Arrange
            final Gatherer<Integer, ?, WithRank<Integer>> gatherer = Gatherers4j.rank();

            // Act
            final List<Long> first = ranksOf(ranked(gatherer, 1, 1, 2));
            final List<Long> second = ranksOf(ranked(gatherer, 1, 1, 2));

            // Assert
            assertThat(second).isEqualTo(first).containsExactly(1L, 1L, 3L);
        }

        @Test
        void rankNullElementWithNaturalOrderFails() {
            assertThatNullPointerException().isThrownBy(() -> ranked(Gatherers4j.rank(), 1, null));
        }
    }

    @Nested
    class RankBy {

        @Test
        void rankBy() {
            // Arrange
            final Stream<String> input = Stream.of("a", "bb", "cc", "ddd");

            // Act
            final List<WithRank<String>> output = input
                    .gather(Gatherers4j.rankBy(Comparator.comparingInt(String::length)))
                    .toList();

            // Assert
            assertThat(output).containsExactly(
                    new WithRank<>(1, "a"),
                    new WithRank<>(2, "bb"),
                    new WithRank<>(2, "cc"),
                    new WithRank<>(4, "ddd")
            );
        }

        @Test
        void rankByNullElements() {
            // Arrange
            final Stream<String> input = Stream.of(null, null, "a", "b");

            // Act
            final List<WithRank<String>> output = input
                    .gather(Gatherers4j.rankBy(Comparator.nullsFirst(Comparator.naturalOrder())))
                    .toList();

            // Assert
            assertThat(output).containsExactly(
                    new WithRank<>(1, null),
                    new WithRank<>(1, null),
                    new WithRank<>(3, "a"),
                    new WithRank<>(4, "b")
            );
        }

        @Test
        void rankByDescendingComparator() {
            // Arrange
            final Stream<Integer> input = Stream.of(30, 20, 20, 10);

            // Act
            final List<WithRank<Integer>> output = input.gather(Gatherers4j.rankBy(Comparator.<Integer>reverseOrder())).toList();

            // Assert
            assertThat(ranksOf(output)).containsExactly(1L, 2L, 2L, 4L);
        }

        @Test
        void rankByNullComparator() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Gatherers4j.rankBy(null))
                    .withMessage("Comparator must not be null");
        }
    }

    @Nested
    class DenseRank {

        @Test
        void denseRank() {
            // Arrange
            final Stream<Integer> input = Stream.of(10, 20, 20, 30);

            // Act
            final List<WithRank<Integer>> output = input.gather(Gatherers4j.denseRank()).toList();

            // Assert
            assertThat(output).containsExactly(
                    new WithRank<>(1, 10),
                    new WithRank<>(2, 20),
                    new WithRank<>(2, 20),
                    new WithRank<>(3, 30)
            );
        }

        @Test
        void denseRankEmptyStream() {
            assertThat(ranked(Gatherers4j.denseRank())).isEmpty();
        }

        @Test
        void denseRankSingleElement() {
            assertThat(ranked(Gatherers4j.denseRank(), 5)).containsExactly(new WithRank<>(1, 5));
        }

        @Test
        void denseRankNeverSkips() {
            assertThat(ranksOf(ranked(Gatherers4j.denseRank(), 1, 1, 1, 2, 3, 3, 4)))
                    .containsExactly(1L, 1L, 1L, 2L, 3L, 3L, 4L);
        }

        @Test
        void denseRankUnsortedInputIsNotDetected() {
            assertThat(ranksOf(ranked(Gatherers4j.denseRank(), 10, 20, 10))).containsExactly(1L, 2L, 3L);
        }

        @Test
        void denseRankWorksOnInfiniteStreams() {
            // Arrange
            final Stream<Integer> input = Stream.iterate(0, i -> i + 1).flatMap(i -> Stream.of(i, i));

            // Act
            final List<Long> ranks = ranksOf(input.gather(Gatherers4j.denseRank()).limit(6).toList());

            // Assert
            assertThat(ranks).containsExactly(1L, 1L, 2L, 2L, 3L, 3L);
        }

        @Test
        void denseRankNullElementWithNaturalOrderFails() {
            assertThatNullPointerException().isThrownBy(() -> ranked(Gatherers4j.denseRank(), 1, null));
        }
    }

    @Nested
    class DenseRankBy {

        @Test
        void denseRankBy() {
            // Arrange
            final Stream<String> input = Stream.of("a", "bb", "cc", "ddd");

            // Act
            final List<WithRank<String>> output = input
                    .gather(Gatherers4j.denseRankBy(Comparator.comparingInt(String::length)))
                    .toList();

            // Assert
            assertThat(output).containsExactly(
                    new WithRank<>(1, "a"),
                    new WithRank<>(2, "bb"),
                    new WithRank<>(2, "cc"),
                    new WithRank<>(3, "ddd")
            );
        }

        @Test
        void denseRankByNullElements() {
            // Arrange
            final Stream<String> input = Stream.of(null, null, "a", "b");

            // Act
            final List<WithRank<String>> output = input
                    .gather(Gatherers4j.denseRankBy(Comparator.nullsFirst(Comparator.<String>naturalOrder())))
                    .toList();

            // Assert
            assertThat(output).containsExactly(
                    new WithRank<>(1, null),
                    new WithRank<>(1, null),
                    new WithRank<>(2, "a"),
                    new WithRank<>(3, "b")
            );
        }

        @Test
        void denseRankByNullComparator() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Gatherers4j.denseRankBy(null))
                    .withMessage("Comparator must not be null");
        }
    }
}
