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

import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Gatherer;

import static com.ginsberg.gatherers4j.util.GathererUtils.mustNotBeNull;

public class TakeUntilGatherer<INPUT extends @Nullable Object>
        implements Gatherer<INPUT, TakeUntilGatherer.State, INPUT> {

    private final Predicate<INPUT> predicate;

    TakeUntilGatherer(final Predicate<INPUT> predicate) {
        this.predicate = mustNotBeNull(predicate, "Predicate must not be null");
    }

    @Override
    public Supplier<State> initializer() {
        return State::new;
    }

    @Override
    public Integrator<State, INPUT, INPUT> integrator() {
        return (state, element, downstream) -> {
            if(state.done) {
                return false;
            }
            state.done = predicate.test(element);
            return downstream.push(element);
        };
    }

    public static class State {
        boolean done;
    }
}
