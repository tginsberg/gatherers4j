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

import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Gatherer;

import static com.ginsberg.gatherers4j.util.GathererUtils.pushAll;

public class UniquelyOccurringGatherer<INPUT extends @Nullable Object>
        implements Gatherer<INPUT, UniquelyOccurringGatherer.State<INPUT>, INPUT> {

    UniquelyOccurringGatherer() {
        // Nothing to do
    }

    @Override
    public Supplier<State<INPUT>> initializer() {
        return State::new;
    }

    @Override
    public Integrator<State<INPUT>, INPUT, INPUT> integrator() {
        return Integrator.ofGreedy((state, element, downstream) -> {
            if (!state.duplicates.contains(element)) {
                if (state.found.contains(element)) {
                    state.duplicates.add(element);
                    state.found.remove(element);
                } else {
                    state.found.add(element);
                }
            }
            return !downstream.isRejecting();
        });
    }

    @Override
    public BinaryOperator<State<INPUT>> combiner() {
        return (left, right) -> {
            for (INPUT element : right.duplicates) {
                left.duplicates.add(element);
                left.found.remove(element);
            }

            for (INPUT element : right.found) {
                if (!left.duplicates.contains(element)) {
                    if (left.found.contains(element)) {
                        left.found.remove(element);
                        left.duplicates.add(element);
                    } else {
                        left.found.add(element);
                    }
                }
            }
            return left;
        };
    }

    @Override
    public BiConsumer<State<INPUT>, Downstream<? super INPUT>> finisher() {
        return (inputState, downstream) -> pushAll(inputState.found, downstream);
    }

    public static class State<INPUT extends @Nullable Object> {
        final Set<INPUT> duplicates = new HashSet<>();
        final Set<INPUT> found = new LinkedHashSet<>();
    }
}
