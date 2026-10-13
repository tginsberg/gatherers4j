/*
 * Copyright 2024-2026 Todd Ginsberg
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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Gatherer;

import static com.ginsberg.gatherers4j.util.GathererUtils.mustNotBeNull;
import static com.ginsberg.gatherers4j.util.GathererUtils.pushAll;

public class DistinctLatestGatherer<INPUT extends @Nullable Object>
        implements Gatherer<INPUT, DistinctLatestGatherer.State<INPUT>, INPUT> {

    private final Function<INPUT, @Nullable Object> mappingFunction;

    DistinctLatestGatherer(final Function<INPUT, @Nullable Object> mappingFunction) {
        this.mappingFunction = mustNotBeNull(mappingFunction, "Mapping function must not be null");
    }

    @Override
    public BinaryOperator<State<INPUT>> combiner() {
        return (left, right) -> {
            left.knownObjects.putAll(right.knownObjects);
            return left;
        };
    }

    @Override
    public BiConsumer<State<INPUT>, Downstream<? super INPUT>> finisher() {
        return (state, downstream) -> pushAll(state.knownObjects.values(), downstream);
    }

    @Override
    public Supplier<State<INPUT>> initializer() {
        return State::new;
    }

    @Override
    public Integrator<State<INPUT>, INPUT, INPUT> integrator() {
        return Integrator.ofGreedy((state, element, downstream) -> {
            state.knownObjects.put(mappingFunction.apply(element), element);
            return !downstream.isRejecting();
        });
    }

    public static class State<INPUT extends @Nullable Object> {
        final Map<@Nullable Object, INPUT> knownObjects = new LinkedHashMap<>();
    }
}
