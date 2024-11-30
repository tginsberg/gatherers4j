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
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;
import java.util.stream.Gatherer;

import static com.ginsberg.gatherers4j.GathererUtils.mustNotBeNull;

public class ShufflingGatherer<INPUT extends @Nullable Object> implements
        Gatherer<INPUT, ShufflingGatherer.State<INPUT>, INPUT> {

    private final RandomGenerator randomGenerator;

    ShufflingGatherer(final RandomGenerator randomGenerator) {
        mustNotBeNull(randomGenerator, "RandomGenerator must not be null");
        this.randomGenerator = randomGenerator;
    }

    @Override
    public Supplier<ShufflingGatherer.State<INPUT>> initializer() {
        return State::new;
    }

    @Override
    public Integrator<ShufflingGatherer.State<INPUT>, INPUT, INPUT> integrator() {
        return Integrator.ofGreedy((state, element, downstream) -> {
            state.inputs.add(element);
            return !downstream.isRejecting();
        });
    }

    @Override
    public BiConsumer<ShufflingGatherer.State<INPUT>, Downstream<? super INPUT>> finisher() {
        return (state, downstream) -> {
            while (!state.inputs.isEmpty() && !downstream.isRejecting()) {
                int randomSlot = randomGenerator.nextInt(state.inputs.size());
                INPUT last = state.inputs.removeLast();
                if (randomSlot < state.inputs.size()) {
                    downstream.push(state.inputs.set(randomSlot, last));
                } else {
                    downstream.push(last);
                }
            }
        };
    }

    public static class State<INPUT> {
        final List<INPUT> inputs = new ArrayList<>();
    }
}
