/*
 * Copyright 2024 Todd Ginsberg
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

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Gatherer;

import static com.ginsberg.gatherers4j.GathererUtils.safeEquals;

public class DedupeConsecutiveGatherer<INPUT extends @Nullable Object>
        implements Gatherer<INPUT, DedupeConsecutiveGatherer.State, INPUT> {

    @Nullable
    private final Function<INPUT, @Nullable Object> mappingFunction;

    DedupeConsecutiveGatherer() {
        this.mappingFunction = null;
    }

    DedupeConsecutiveGatherer(@Nullable final Function<INPUT, @Nullable Object> mappingFunction) {
        this.mappingFunction = mappingFunction;
    }

    @Override
    public Supplier<State> initializer() {
        return State::new;
    }

    @Override
    public Integrator<DedupeConsecutiveGatherer.State, INPUT, INPUT> integrator() {
        return Integrator.ofGreedy((state, element, downstream) -> {
            final Object mapped = mappingFunction == null ? element : mappingFunction.apply(element);
            if (!state.hasValue) {
                state.hasValue = true;
                state.value = mapped;
                return downstream.push(element);
            } else if (!safeEquals(state.value, mapped)) {
                state.value = mapped;
                return downstream.push(element);
            }
            return !downstream.isRejecting();
        });
    }

    public static class State {
        @Nullable
        Object value;
        boolean hasValue;
    }
}