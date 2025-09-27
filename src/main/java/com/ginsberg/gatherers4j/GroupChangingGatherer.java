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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Gatherer;

import static com.ginsberg.gatherers4j.util.GathererUtils.mustNotBeNull;

public class GroupChangingGatherer<INPUT>
        implements Gatherer<INPUT, GroupChangingGatherer.State<INPUT>, List<INPUT>> {

    private final Order operation;
    private final Comparator<INPUT> comparator;

    static <INPUT> GroupChangingGatherer<INPUT> usingComparator(
            final Order operation,
            final Comparator<INPUT> comparator
    ) {
        return new GroupChangingGatherer<>(operation, comparator);
    }

    static <INPUT extends Comparable<INPUT>> GroupChangingGatherer<INPUT> usingComparable(
            final Order operation
    ) {
        return new GroupChangingGatherer<>(operation, Comparable::compareTo);
    }

    GroupChangingGatherer(
            final Order operation,
            final Comparator<INPUT> comparator
    ) {
        this.operation = mustNotBeNull(operation, "Operation must not be null");
        this.comparator = mustNotBeNull(comparator, "Comparator must not be null");
    }

    @Override
    public Supplier<GroupChangingGatherer.State<INPUT>> initializer() {
        return State::new;
    }

    @Override
    public Integrator<GroupChangingGatherer.State<INPUT>, INPUT, List<INPUT>> integrator() {
        return Integrator.ofGreedy((state, element, downstream) -> {
            if (!state.currentElements.isEmpty() && !isInSameList(state.currentElements.getLast(), element)) {
                downstream.push(Collections.unmodifiableList(state.currentElements));
                state.currentElements = new ArrayList<>();
            }
            state.currentElements.add(element);
            return !downstream.isRejecting();
        });
    }

    boolean isInSameList(final INPUT previous, final INPUT next) {
        return operation.allows(comparator.compare(next, previous));
    }

    @Override
    public BiConsumer<State<INPUT>, Downstream<? super List<INPUT>>> finisher() {
        return (inputState, downstream) -> {
            if (!inputState.currentElements.isEmpty()) {
                downstream.push(Collections.unmodifiableList(inputState.currentElements));
            }
        };
    }

    public static class State<INPUT> {
        List<INPUT> currentElements = new ArrayList<>();
    }

}
