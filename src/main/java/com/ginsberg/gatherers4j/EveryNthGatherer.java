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

import java.util.function.Supplier;
import java.util.stream.Gatherer;

public class EveryNthGatherer<INPUT extends @Nullable Object>
        implements Gatherer<INPUT, EveryNthGatherer.State, INPUT> {

    private final int count;

    EveryNthGatherer(final int count) {
        if (count < 2) {
            throw new IllegalArgumentException("Count must be a minimum of 2");
        }
        this.count = count;
    }

    @Override
    public Supplier<State> initializer() {
        return State::new;
    }

    @Override
    public Integrator<EveryNthGatherer.State, INPUT, INPUT> integrator() {
        return (state, element, downstream) -> {
            if (state.size++ % count == 0) {
                downstream.push(element);
            }
            return !downstream.isRejecting();
        };
    }

    public static class State {
        int size;
    }
}
