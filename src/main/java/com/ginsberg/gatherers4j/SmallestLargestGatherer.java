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

import com.ginsberg.gatherers4j.util.GathererUtils;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Gatherer;

import static com.ginsberg.gatherers4j.util.GathererUtils.mustNotBeNull;
import static com.ginsberg.gatherers4j.util.GathererUtils.pushAll;

/// Keeps the `count` best elements of the stream in a bounded heap and emits them, best first, when the stream ends.
/// "Best" is first in the order of the given comparator, with ties going to the element encountered first, so the
/// result is the same as `sorted(comparator).limit(count)` without sorting or holding the whole stream.
public class SmallestLargestGatherer<INPUT extends @Nullable Object>
        implements Gatherer<INPUT, SmallestLargestGatherer.State<INPUT>, INPUT> {

    private final Comparator<Entry<INPUT>> bestFirst;
    private final int count;
    private final boolean skipNulls;

    static <INPUT extends @Nullable Comparable<? super INPUT>> Gatherer<INPUT, ?, INPUT> largest(final int count) {
        return of(count, Comparator.reverseOrder(), true);
    }

    static <INPUT extends @Nullable Object> Gatherer<INPUT, ?, INPUT> largestBy(
            final int count,
            final Comparator<? super INPUT> comparator
    ) {
        return of(
                count,
                mustNotBeNull(comparator, "Comparator must not be null").reversed(),
                false
        );
    }

    static <INPUT extends @Nullable Comparable<? super INPUT>> Gatherer<INPUT, ?, INPUT> smallest(final int count) {
        return of(count, Comparator.naturalOrder(), true);
    }

    static <INPUT extends @Nullable Object> Gatherer<INPUT, ?, INPUT> smallestBy(
            final int count,
            final Comparator<? super INPUT> comparator
    ) {
        return of(
                count,
                mustNotBeNull(comparator, "Comparator must not be null"),
                false
        );
    }

    private static <INPUT extends @Nullable Object> Gatherer<INPUT, ?, INPUT> of(
            final int count,
            final Comparator<? super INPUT> comparator,
            final boolean skipNulls
    ) {
        if (count < 0) {
            throw new IllegalArgumentException("Count must not be negative");
        } else if (count == 0) {
            return GathererUtils.emptyGatherer();
        } else {
            return new SmallestLargestGatherer<>(count, comparator, skipNulls);
        }
    }

    private SmallestLargestGatherer(
            final int count,
            final Comparator<? super INPUT> comparator,
            final boolean skipNulls
    ) {
        this.count = count;
        this.skipNulls = skipNulls;
        this.bestFirst = Comparator
                .<Entry<INPUT>, INPUT>comparing(Entry::value, comparator)
                .thenComparingLong(Entry::sequence);
    }

    @Override
    public BinaryOperator<State<INPUT>> combiner() {
        // Every element on the right came after every element on the left, so shifting the right's
        // sequence numbers past the left's keeps ties going to the earlier element.
        return (left, right) -> {
            for (final Entry<INPUT> entry : right.heap) {
                left.offer(new Entry<>(entry.sequence() + left.seen, entry.value()));
            }
            left.seen += right.seen;
            return left;
        };
    }

    @Override
    public BiConsumer<State<INPUT>, Downstream<? super INPUT>> finisher() {
        return (state, downstream) -> {
            final List<Entry<INPUT>> entries = new ArrayList<>(state.heap);
            entries.sort(bestFirst);
            pushAll(entries.stream().map(Entry::value), downstream);
        };
    }

    @Override
    public Supplier<State<INPUT>> initializer() {
        return () -> new State<>(count, bestFirst);
    }

    @Override
    public Integrator<State<INPUT>, INPUT, INPUT> integrator() {
        return Integrator.ofGreedy((state, element, downstream) -> {
            if (element != null || !skipNulls) {
                state.offer(new Entry<>(state.seen, element));
            }
            state.seen++;
            return !downstream.isRejecting();
        });
    }

    record Entry<INPUT extends @Nullable Object>(long sequence, INPUT value) {
    }

    public static class State<INPUT extends @Nullable Object> {
        private final Comparator<Entry<INPUT>> bestFirst;
        private final int count;
        final PriorityQueue<Entry<INPUT>> heap;
        long seen;

        State(final int count, final Comparator<Entry<INPUT>> bestFirst) {
            this.bestFirst = bestFirst;
            this.count = count;
            this.heap = new PriorityQueue<>(bestFirst.reversed());
        }

        void offer(final Entry<INPUT> entry) {
            if (heap.size() < count) {
                heap.add(entry);
            } else if (bestFirst.compare(entry, heap.element()) < 0) {
                heap.poll();
                heap.add(entry);
            }
        }
    }
}
