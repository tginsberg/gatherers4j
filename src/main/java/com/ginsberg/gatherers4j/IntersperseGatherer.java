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

public class IntersperseGatherer<INPUT extends @Nullable Object>
        implements Gatherer<INPUT, IntersperseGatherer.State, INPUT> {

    private final INPUT interspersed;

    IntersperseGatherer(INPUT interspersed) {
        this.interspersed = interspersed;
    }

    @Override
    public Supplier<State> initializer() {
        return State::new;
    }

    @Override
    public Integrator<State, INPUT, INPUT> integrator() {
        return Integrator.ofGreedy((state, element, downstream) -> {
            if(state.hasStarted) {
                downstream.push(interspersed);
            } else {
                state.hasStarted = true;
            }
            return downstream.push(element);
        });
    }

    public static class State {
        boolean hasStarted;
    }

}
