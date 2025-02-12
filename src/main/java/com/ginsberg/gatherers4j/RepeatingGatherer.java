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

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Gatherer;

public class RepeatingGatherer<INPUT extends @Nullable Object>
        implements Gatherer<INPUT, RepeatingGatherer.State<INPUT>, INPUT> {

    private static final int INFINITE = -1;
    private final int repeats;

    public static <INPUT> RepeatingGatherer<INPUT> ofInfinite() {
        return new RepeatingGatherer<>(INFINITE);
    }

    public static <INPUT> RepeatingGatherer<INPUT> ofFinite(final int repeats) {
        if (repeats <= 1) {
            throw new IllegalArgumentException("Number of repeats must be greater than 1");
        }
        return new RepeatingGatherer<>(repeats);
    }

    private RepeatingGatherer(final int repeats) {
        this.repeats = repeats;
    }

    @Override
    public Supplier<RepeatingGatherer.State<INPUT>> initializer() {
        return () -> new State<>(repeats);
    }

    @Override
    public Integrator<RepeatingGatherer.State<INPUT>, INPUT, INPUT> integrator() {
        return Integrator.ofGreedy((state, element, downstream) -> {
            state.theStream.add(element);
            return !downstream.isRejecting();
        });
    }

    @Override
    public BiConsumer<RepeatingGatherer.State<INPUT>, Downstream<? super INPUT>> finisher() {
        return (inputState, downstream) -> {
            while (!downstream.isRejecting() && (inputState.repeatsRemaining == INFINITE || inputState.repeatsRemaining > 0)) {
                inputState.theStream.forEach(downstream::push);
                if (inputState.repeatsRemaining != INFINITE) {
                    inputState.repeatsRemaining--;
                }
            }
        };
    }

    public static class State<INPUT> {
        int repeatsRemaining;
        final List<INPUT> theStream = new ArrayList<>();

        State(final int repeatsRemaining) {
            this.repeatsRemaining = repeatsRemaining;
        }
    }
}
