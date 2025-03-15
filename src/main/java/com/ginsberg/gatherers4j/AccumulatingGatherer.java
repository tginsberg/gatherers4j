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

import static com.ginsberg.gatherers4j.util.GathererUtils.mustNotBeNull;

public class AccumulatingGatherer<INPUT extends @Nullable Object, OUTPUT extends @Nullable Object>
        implements Gatherer<INPUT, AccumulatingGatherer.State<OUTPUT>, OUTPUT> {

    private final IndexedAccumulatorFunction<? super OUTPUT, ? super INPUT, ? extends OUTPUT> accumulatorFunction;
    private final Supplier<OUTPUT> initialValue;
    private final boolean running;

    AccumulatingGatherer(
            final boolean running,
            final Supplier<OUTPUT> initialValue,
            final IndexedAccumulatorFunction<? super OUTPUT, ? super INPUT, ? extends OUTPUT> foldFunction
    ) {
        mustNotBeNull(initialValue, "Initial value supplier must not be null");
        mustNotBeNull(foldFunction, "Accumulator function must not be null");
        this.accumulatorFunction = foldFunction;
        this.initialValue = initialValue;
        this.running = running;
    }

    @Override
    public Supplier<State<OUTPUT>> initializer() {
        return () -> new State<>(initialValue.get());
    }

    @Override
    public Integrator<State<OUTPUT>, INPUT, OUTPUT> integrator() {
        return Integrator.ofGreedy((state, element, downstream) -> {
            state.carriedValue = accumulatorFunction.apply(state.index++, state.carriedValue, element);
            if (running) {
                downstream.push(state.carriedValue);
            }
            return !downstream.isRejecting();
        });
    }

    @Override
    public BiConsumer<State<OUTPUT>, Downstream<? super OUTPUT>> finisher() {
        return (outputState, downstream) -> {
            if (!running) {
                downstream.push(outputState.carriedValue);
            }
        };
    }

    public static class State<OUTPUT> {
        @Nullable
        OUTPUT carriedValue;
        long index;

        private State(@Nullable final OUTPUT initialValue) {
            carriedValue = initialValue;
        }
    }
}
