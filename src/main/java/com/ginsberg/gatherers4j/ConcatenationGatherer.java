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

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Gatherer;
import java.util.stream.Stream;

import static com.ginsberg.gatherers4j.GathererUtils.mustNotBeNull;

public class ConcatenationGatherer<INPUT> implements Gatherer<INPUT, ConcatenationGatherer.State<INPUT>, INPUT> {

    private final List<Stream<INPUT>> concatThese = new ArrayList<>();

    ConcatenationGatherer(final Stream<INPUT> concatThis) {
        concat(concatThis);
    }

    public final ConcatenationGatherer<INPUT> concat(final Stream<INPUT> concatThis) {
        mustNotBeNull(concatThis, "Concatenated stream must not be null");
        this.concatThese.add(concatThis);
        return this;
    }

    @Override
    public Supplier<State<INPUT>> initializer() {
        return () -> new State<>(concatThese);
    }

    @Override
    public Integrator<State<INPUT>, INPUT, INPUT> integrator() {
        return (_, element, downstream) -> downstream.push(element);
    }

    @Override
    public BiConsumer<State<INPUT>, Downstream<? super INPUT>> finisher() {
        return (state, downstream) -> {
            for (Stream<INPUT> concatThis : state.concatThese) {
                concatThis.forEach(downstream::push);
            }
        };
    }

    public static class State<INPUT> {
        final List<Stream<INPUT>> concatThese;

        public State(List<Stream<INPUT>> concatThese) {
            this.concatThese = concatThese;
        }
    }
}
