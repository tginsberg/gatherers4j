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

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Gatherer;

public class DistinctGatherer<INPUT, MAPPED>
        implements Gatherer<INPUT, DistinctGatherer.State<MAPPED>, INPUT> {

    private final Function<INPUT, MAPPED> byFunction;

    DistinctGatherer(final Function<INPUT, MAPPED> byFunction) {
        Objects.requireNonNull(byFunction, "Mapping function must not be null");
        this.byFunction = byFunction;
    }

    @Override
    public Supplier<State<MAPPED>> initializer() {
        return State::new;
    }

    @Override
    public Integrator<DistinctGatherer.State<MAPPED>, INPUT, INPUT> integrator() {
        return (state, element, downstream) -> {
            if (state.knownObjects.add(byFunction.apply(element))) {
                downstream.push(element);
            }
            return !downstream.isRejecting();
        };
    }

    public static class State<MAPPED> {
        final Set<MAPPED> knownObjects = new HashSet<>();
    }
}
