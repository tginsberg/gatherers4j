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

import java.util.function.BiPredicate;
import java.util.function.Supplier;
import java.util.stream.Gatherer;

import static com.ginsberg.gatherers4j.GathererUtils.mustNotBeNull;

public class FilteringWithIndexGatherer<INPUT>
        implements Gatherer<INPUT, FilteringWithIndexGatherer.State, INPUT> {

    private final BiPredicate<Long, INPUT> predicate;

    FilteringWithIndexGatherer(final BiPredicate<Long, INPUT> predicate) {
        mustNotBeNull(predicate, "Predicate must not be null");
        this.predicate = predicate;
    }

    @Override
    public Supplier<FilteringWithIndexGatherer.State> initializer() {
        return State::new;
    }

    @Override
    public Integrator<FilteringWithIndexGatherer.State, INPUT, INPUT> integrator() {
        return Integrator.ofGreedy((state, element, downstream) -> {
            if (predicate.test(state.index++, element)) {
                downstream.push(element);
            }
            return !downstream.isRejecting();
        });
    }

    public static class State {
        long index;
    }
}
