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
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.function.Supplier;
import java.util.stream.Gatherer;

import static com.ginsberg.gatherers4j.GathererUtils.mustNotBeNull;

public class FilterChangingGatherer<INPUT>
        implements Gatherer<INPUT, FilterChangingGatherer.State<INPUT>, INPUT> {

    private final Order operation;
    private final Comparator<INPUT> comparator;

    static <INPUT> FilterChangingGatherer<INPUT> usingComparator(
            final Order operation,
            final Comparator<INPUT> comparator
    ) {
        return new FilterChangingGatherer<>(operation, comparator);
    }

    static <INPUT extends Comparable<INPUT>> FilterChangingGatherer<INPUT> usingComparable(
            final Order operation
    ) {
        return new FilterChangingGatherer<>(operation, Comparable::compareTo);
    }

    FilterChangingGatherer(
            final Order operation,
            final Comparator<INPUT> comparator
    ) {
        mustNotBeNull(operation, "Operation must not be null");
        mustNotBeNull(comparator, "Comparator must not be null");
        this.operation = operation;
        this.comparator = comparator;
    }

    @Override
    public Supplier<FilterChangingGatherer.State<INPUT>> initializer() {
        return State::new;
    }

    @Override
    public Integrator<FilterChangingGatherer.State<INPUT>, INPUT, INPUT> integrator() {
        return Integrator.ofGreedy((state, element, downstream) -> {
            if (state.first) {
                downstream.push(element);
                state.previousElement = element;
                state.first = false;
            } else if (allow(state.previousElement, element)) {
                downstream.push(element);
                state.previousElement = element;
            }
            return !downstream.isRejecting();
        });
    }

    boolean allow(final @Nullable INPUT previous, final INPUT next) {
        return operation.allows(comparator.compare(next, previous));
    }

    public static class State<INPUT> {
        boolean first = true;
        @Nullable
        INPUT previousElement;
    }
}
