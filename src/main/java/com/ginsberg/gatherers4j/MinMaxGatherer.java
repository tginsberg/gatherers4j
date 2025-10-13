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

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Gatherer;

import static com.ginsberg.gatherers4j.util.GathererUtils.mustNotBeNull;

public class MinMaxGatherer<INPUT extends @Nullable Object>
        implements Gatherer<INPUT, MinMaxGatherer.State<INPUT>, INPUT> {

    private final Comparator<INPUT> comparator;
    private final int windowSize;
    private boolean excludePartialValues;

    static <INPUT> MinMaxGatherer<INPUT> runningUsingComparator(
            final boolean sortingMin,
            final Comparator<INPUT> comparator
    ) {
        return new MinMaxGatherer<>(sortingMin ? comparator : comparator.reversed());
    }

    static <INPUT> MinMaxGatherer<INPUT> movingUsingComparator(
            final int windowSize,
            final boolean sortingMin,
            final Comparator<INPUT> comparator
    ) {
        return new MinMaxGatherer<>(windowSize, sortingMin ? comparator : comparator.reversed());
    }

    static <INPUT extends Comparable<INPUT>> MinMaxGatherer<INPUT> runningUsingComparable(
            final boolean sortingMin
    ) {
        return new MinMaxGatherer<>(sortingMin ? Comparable::compareTo : Comparator.reverseOrder());
    }

    static <INPUT extends Comparable<INPUT>> MinMaxGatherer<INPUT> movingUsingComparable(
            final int windowSize,
            final boolean sortingMin
    ) {
        return new MinMaxGatherer<>(windowSize, sortingMin ? Comparable::compareTo : Comparator.reverseOrder());
    }

    private MinMaxGatherer(final Comparator<INPUT> comparator) {
        this.windowSize = -1;
        this.comparator = mustNotBeNull(comparator, "Comparator must not be null");
    }

    private MinMaxGatherer(final int windowSize, final Comparator<INPUT> comparator) {
        if (windowSize < 2) {
            throw new IllegalArgumentException("windowSize must be greater than 1");
        }
        this.windowSize = windowSize;
        this.comparator = mustNotBeNull(comparator, "Comparator must not be null");
    }

    @Override
    public Supplier<State<INPUT>> initializer() {
        return () -> windowSize == -1 ? new State<>(comparator) : new MovingState<>(windowSize, comparator, excludePartialValues);
    }

    @Override
    public Integrator<State<INPUT>, INPUT, INPUT> integrator() {
        return Integrator.ofGreedy((state, element, downstream) -> {
            if (element != null) {
                state.add(element);
                if(state.canCalculate()) {
                    return downstream.push(state.calculate());
                }
            }
            return !downstream.isRejecting();
        });
    }

    public MinMaxGatherer<INPUT> excludePartialValues() {
        this.excludePartialValues = true;
        return this;
    }

    /// Include the original input value from the stream in addition to the calculated value.
    public WithOriginalGatherer<INPUT, ?, INPUT> withOriginal() {
        return new WithOriginalGatherer<>(this);
    }

    public static class State<INPUT extends @Nullable Object> {
        @Nullable INPUT bestValue;
        final Comparator<INPUT> comparator;

        public State(final Comparator<INPUT> comparator) {
            this.comparator = comparator;
        }

        void add(final INPUT nextValue) {
            if (bestValue == null) {
                bestValue = nextValue;
            } else {
                bestValue = eval(nextValue) ? bestValue : nextValue;
            }
        }

        boolean canCalculate() {
            return true;
        }

        @SuppressWarnings("NullAway")
        @NonNull INPUT calculate() {
            assert bestValue != null;
            return bestValue;
        }

        boolean eval(final INPUT nextValue) {
            return comparator.compare(bestValue, nextValue) <= 0;
        }
    }

    public static class MovingState<INPUT> extends State<INPUT> {
        private record IndexValue<INPUT>(int index, INPUT value) {
        }

        private final List<IndexValue<INPUT>> queue = new ArrayList<>();
        private final int windowSize;
        private final boolean excludePartialValues;
        private int seen;

        public MovingState(final int windowSize, final Comparator<INPUT> comparator, final boolean excludePartialValues) {
            super(comparator);
            this.windowSize = windowSize;
            this.excludePartialValues = excludePartialValues;
        }

        @Override
        void add(final INPUT nextValue) {
            seen++;
            while (!queue.isEmpty() && queue.getFirst().index() <= (seen - windowSize)) {
                queue.removeFirst();
            }

            while (!queue.isEmpty() && shouldRemove(queue.getLast().value(), nextValue)) {
                queue.removeLast();
            }
            queue.add(new IndexValue<>(seen, nextValue));
            bestValue = queue.getFirst().value();
        }

        @Override
        boolean canCalculate() {
            return !excludePartialValues || seen >= windowSize;
        }

        private boolean shouldRemove(final INPUT oldItem, final INPUT newItem) {
            return comparator.compare(oldItem, newItem) > 0;
        }
    }
}
