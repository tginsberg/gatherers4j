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

import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Gatherer;

import static com.ginsberg.gatherers4j.GathererUtils.mustNotBeNull;

public class ReduceIndexedGatherer<INPUT extends @Nullable Object>
        implements Gatherer<INPUT, ReduceIndexedGatherer.State<INPUT>, INPUT> {

    private final TriFunction<Long, INPUT, INPUT, INPUT> reduceFunction;

    ReduceIndexedGatherer(final TriFunction<Long, INPUT, INPUT, INPUT> reduceFunction) {
        mustNotBeNull(reduceFunction, "Reduce function must not be null");
        this.reduceFunction = reduceFunction;
    }

    @Override
    public Supplier<State<INPUT>> initializer() {
        return State::new;
    }

    @Override
    public Integrator<State<INPUT>, INPUT, INPUT> integrator() {
        return Integrator.ofGreedy((state, element, downstream) -> {
            if (!state.hasFirstValue) {
                state.value = element;
                state.hasFirstValue = true;
            } else {
                state.value = reduceFunction.apply(state.index++, state.value, element);
            }
            return !downstream.isRejecting();
        });
    }

    @Override
    public BiConsumer<State<INPUT>, Downstream<? super INPUT>> finisher() {
        return (inputState, downstream) -> downstream.push(inputState.value);
    }

    public static class State<INPUT> {
        @Nullable
        INPUT value;
        long index = 1;
        boolean hasFirstValue = false;
    }
}
