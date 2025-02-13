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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Gatherer;

import static com.ginsberg.gatherers4j.GathererUtils.mustNotBeNull;
import static com.ginsberg.gatherers4j.GathererUtils.safeEquals;

public class GroupingByGatherer<INPUT extends @Nullable Object> implements
        Gatherer<INPUT, GroupingByGatherer.State<INPUT>, List<INPUT>> {

    @Nullable
    private final Function<INPUT, Object> mappingFunction;

    GroupingByGatherer() {
        mappingFunction = null;
    }

    GroupingByGatherer(final Function<@Nullable INPUT, @Nullable Object> mappingFunction) {
        mustNotBeNull(mappingFunction, "Mapping function must not be null");
        this.mappingFunction = mappingFunction;
    }

    @Override
    public Supplier<State<INPUT>> initializer() {
        return State::new;
    }

    @Override
    public Integrator<State<INPUT>, INPUT, List<INPUT>> integrator() {
        return Integrator.ofGreedy((state, element, downstream) -> {
            final Object thisMappedElement = mappingFunction == null ? element: mappingFunction.apply(element);
            if (!state.working.isEmpty() && !safeEquals(state.previousMappedElement, thisMappedElement)) {
                downstream.push(Collections.unmodifiableList(state.working));
                state.working = new ArrayList<>();
            }
            state.previousMappedElement = thisMappedElement;
            state.working.add(element);
            return !downstream.isRejecting();
        });
    }

    @Override
    public BiConsumer<State<INPUT>, Downstream<? super List<INPUT>>> finisher() {
        return (state, downstream) -> {
            if (!state.working.isEmpty()) {
                downstream.push(Collections.unmodifiableList(state.working));
            }
        };
    }

    public static class State<INPUT> {
        @Nullable Object previousMappedElement = null;
        List<INPUT> working = new ArrayList<>();
    }
}
