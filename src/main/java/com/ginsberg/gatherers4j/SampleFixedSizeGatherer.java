/*
 * Copyright 2025 Todd Ginsberg
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

import static com.ginsberg.gatherers4j.util.GathererUtils.pushAll;

public class SampleFixedSizeGatherer<INPUT extends @Nullable Object> implements Gatherer<INPUT, SampleFixedSizeGatherer.State<INPUT>, INPUT> {

    private final int sampleSize;

    SampleFixedSizeGatherer(final int sampleSize) {
        if (sampleSize < 1) {
            throw new IllegalArgumentException("sampleSize must be at least 1");
        }
        this.sampleSize = sampleSize;
    }

    @Override
    public Supplier<State<INPUT>> initializer() {
        return () -> new State<>(sampleSize);
    }

    @Override
    public Integrator<State<INPUT>, INPUT, INPUT> integrator() {
        return Integrator.ofGreedy((state, element, downstream) -> {
            state.take(element);
            return !downstream.isRejecting();
        });
    }

    @Override
    public BiConsumer<State<INPUT>, Downstream<? super INPUT>> finisher() {
        return (inputState, downstream) -> pushAll(inputState.elements, downstream);
    }

    public static class State<INPUT> {
        private final List<INPUT> elements = new ArrayList<>();
        private final RandomGenerator random = RandomGenerator.getDefault();
        private final int sampleSize;
        private int index = 0;

        State(final int sampleSize) {
            this.sampleSize = sampleSize;
        }

        void take(final @Nullable INPUT element) {
            if (index < sampleSize) {
                elements.add(element);
            } else {
                int n = random.nextInt(0, index);
                if (n < sampleSize) {
                    // Not replacing element at n because we want to keep iteration order.
                    elements.remove(n);
                    elements.add(element);
                }
            }
            index++;
        }
    }

}
