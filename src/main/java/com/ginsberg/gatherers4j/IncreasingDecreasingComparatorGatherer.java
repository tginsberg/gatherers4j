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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Gatherer;

import static com.ginsberg.gatherers4j.GathererUtils.mustNotBeNull;

public sealed class IncreasingDecreasingComparatorGatherer<INPUT>
        implements Gatherer<INPUT, IncreasingDecreasingComparatorGatherer.State<INPUT>, List<INPUT>>
        permits IncreasingDecreasingComparableGatherer {

    private final Operation operation;
    private final Comparator<INPUT> comparator;

    IncreasingDecreasingComparatorGatherer(
            final Operation operation,
            final Comparator<INPUT> comparator
    ) {
        mustNotBeNull(operation, "Operation must not be null");
        mustNotBeNull(comparator, "Comparator must not be null");
        this.operation = operation;
        this.comparator = comparator;
    }

    @Override
    public Supplier<IncreasingDecreasingComparatorGatherer.State<INPUT>> initializer() {
        return State::new;
    }

    @Override
    public Integrator<IncreasingDecreasingComparatorGatherer.State<INPUT>, INPUT, List<INPUT>> integrator() {
        return Integrator.ofGreedy((state, element, downstream) -> {
            if (!state.currentElements.isEmpty() && !isInSameList(state.currentElements.getLast(), element)) {
                downstream.push(List.copyOf(state.currentElements));
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
                downstream.push(List.copyOf(inputState.currentElements));
            }
        };
    }

    public static class State<INPUT> {
        List<INPUT> currentElements = new ArrayList<>();
    }

    enum Operation {
        Decreasing {
            @Override
            boolean allows(final int comparison) {
                return comparison < 0;
            }
        },
        Increasing {
            @Override
            boolean allows(final int comparison) {
                return comparison > 0;
            }
        },
        NonDecreasing {
            @Override
            boolean allows(final int comparison) {
                return comparison >= 0;
            }
        },
        NonIncreasing {
            @Override
            boolean allows(final int comparison) {
                return comparison <= 0;
            }
        };

        abstract boolean allows(final int comparison);
    }
}
