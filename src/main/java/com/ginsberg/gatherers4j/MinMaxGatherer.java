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

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Gatherer;

public class MinMaxGatherer<INPUT, MAPPED extends Comparable<MAPPED>>
        implements Gatherer<INPUT, MinMaxGatherer.State<INPUT, MAPPED>, INPUT> {

    private final Function<INPUT, MAPPED> mappingFunction;
    private final boolean max;

    MinMaxGatherer(final boolean max, final Function<INPUT, MAPPED> mappingFunction) {
        this.mappingFunction = mappingFunction;
        this.max = max;
    }

    @Override
    public Supplier<State<INPUT, MAPPED>> initializer() {
        return State::new;
    }

    @Override
    public Integrator<State<INPUT, MAPPED>, INPUT, INPUT> integrator() {
        return Integrator.ofGreedy((state, element, downstream) -> {
            final MAPPED mapped = element == null ? null : mappingFunction.apply(element);
            if (mapped == null) {
                return !downstream.isRejecting();
            }
            if (state.bestSoFar == null) {
                state.bestSoFar = element;
                state.mappedField = mapped;
            } else {
                final int compared = mapped.compareTo(state.mappedField);
                if ((compared > 0 && max) || (compared < 0 && !max)) {
                    state.bestSoFar = element;
                    state.mappedField = mapped;
                }
            }
            return !downstream.isRejecting();
        });
    }

    @Override
    public BiConsumer<State<INPUT, MAPPED>, Downstream<? super INPUT>> finisher() {
        return (state, downstream) -> {
            if (state.bestSoFar != null) {
                downstream.push(state.bestSoFar);
            }
        };
    }

    public static class State<INPUT, MAPPED extends Comparable<MAPPED>> {
        private INPUT bestSoFar;
        private MAPPED mappedField;
    }
}
