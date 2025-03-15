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

import com.ginsberg.gatherers4j.dto.WithCount;
import com.ginsberg.gatherers4j.enums.Frequency;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Gatherer;

import static com.ginsberg.gatherers4j.util.GathererUtils.mustNotBeNull;

public class FrequencyGatherer<INPUT extends @Nullable Object>
        implements Gatherer<INPUT, FrequencyGatherer.State<INPUT>, WithCount<INPUT>> {

    private final Frequency order;

    FrequencyGatherer(final Frequency order) {
        mustNotBeNull(order, "Order must be specified");
        this.order = order;
    }

    @Override
    public Supplier<State<INPUT>> initializer() {
        return State::new;
    }

    @Override
    public Integrator<State<INPUT>, INPUT, WithCount<INPUT>> integrator() {
        return Integrator.ofGreedy((state, element, downstream) -> {
            state.counts.merge(element, 1L, Long::sum);
            return !downstream.isRejecting();
        });
    }

    @Override
    public BinaryOperator<State<INPUT>> combiner() {
        return (state1, state2) -> {
            state2.counts.forEach((key, value) -> state1.counts.merge(key, value, Long::sum));
            return state1;
        };
    }

    @Override
    public BiConsumer<State<INPUT>, Downstream<? super WithCount<INPUT>>> finisher() {
        return (inputState, downstream) -> inputState.counts
                .entrySet()
                .stream().map(it -> new WithCount<>(it.getKey(), it.getValue()))
                .sorted(comparator())
                .forEach(downstream::push);
    }

    private Comparator<WithCount<INPUT>> comparator() {
        if(order == Frequency.Descending) {
            return (o1, o2) -> (int)(o2.count() - o1.count());
        } else {
            return (o1, o2) -> (int)(o1.count() - o2.count());
        }
    }

    public static class State<INPUT> {
        final Map<INPUT, Long> counts = new HashMap<>();
    }
}
