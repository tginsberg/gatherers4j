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
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Gatherer;

import static com.ginsberg.gatherers4j.GathererUtils.mustNotBeNull;
import static com.ginsberg.gatherers4j.GathererUtils.safeEquals;

public class GroupingByGatherer<INPUT> implements Gatherer<INPUT, GroupingByGatherer.State<INPUT>, List<INPUT>> {

    private final Function<INPUT, Object> mappingFunction;

    GroupingByGatherer(Function<INPUT, Object> mappingFunction) {
        mustNotBeNull(mappingFunction, "Mapping function must not be null");
        this.mappingFunction = mappingFunction;
    }

    @Override
    public Supplier<State<INPUT>> initializer() {
        return State::new;
    }

    @Override
    public Integrator<State<INPUT>, INPUT, List<INPUT>> integrator() {
        return Integrator.ofGreedy(((state, element, downstream) -> {
            final Object thisMatch = mappingFunction.apply(element);
            if (state.working == null) {
                state.working = new ArrayList<>();
                state.working.add(element);
                state.pastMatch = thisMatch;
            } else if (!safeEquals(state.pastMatch, thisMatch)) {
                downstream.push(state.working);
                state.working = new ArrayList<>();
                state.working.add(element);
                state.pastMatch = thisMatch;
            } else {
                state.working.add(element);
            }
            return !downstream.isRejecting();
        }));
    }

    @Override
    public BiConsumer<State<INPUT>, Downstream<? super List<INPUT>>> finisher() {
        return ((state, downstream) -> {
            if (state.working != null) {
                downstream.push(state.working);
            }
        });
    }

    public static class State<INPUT> {
        Object pastMatch = null;
        List<INPUT> working = null;
    }
}
