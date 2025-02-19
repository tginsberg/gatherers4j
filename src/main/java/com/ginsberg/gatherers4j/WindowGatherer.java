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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Gatherer;

public class WindowGatherer<INPUT extends @Nullable Object>
        implements Gatherer<INPUT, WindowGatherer.State<INPUT>, List<INPUT>> {

    private final boolean includePartials;
    private final int stepping;
    private final int windowSize;

    WindowGatherer(final int windowSize, final int stepping, final boolean includePartials) {
        if (windowSize <= 0) {
            throw new IllegalArgumentException("Window size must be greater than zero");
        }
        if (stepping <= 0) {
            throw new IllegalArgumentException("Stepping must be greater than zero");
        }
        this.windowSize = windowSize;
        this.stepping = stepping;
        this.includePartials = includePartials;
    }

    @Override
    public Supplier<State<INPUT>> initializer() {
        return State::new;
    }

    @Override
    public Integrator<State<INPUT>, INPUT, List<INPUT>> integrator() {
        return Integrator.ofGreedy((state, element, downstream) -> {
            if (state.stepDelta == 0) {
                state.window.add(element);
            } else {
                state.stepDelta--;
            }
            if (state.window.size() == windowSize) {
                downstream.push(List.copyOf(state.window));
                state.stepDelta = Math.max(0, stepping - windowSize);

                if (stepping >= windowSize) {
                    state.window.clear();
                } else {
                    for (int i = 0; i < stepping; i++) {
                        state.window.removeFirst();
                    }
                }
            }
            return !downstream.isRejecting();
        });
    }

    @Override
    public BiConsumer<State<INPUT>, Downstream<? super List<INPUT>>> finisher() {
        return (inputState, downstream) -> {
            if (includePartials && !inputState.window.isEmpty()) {
                downstream.push(List.copyOf(inputState.window));
            }
        };
    }

    public static class State<INPUT> {
        int stepDelta = 0;
        final Deque<INPUT> window = new ArrayDeque<>();
    }
}
