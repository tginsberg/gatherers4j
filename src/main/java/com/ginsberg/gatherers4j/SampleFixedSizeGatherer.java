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

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;
import java.util.stream.Gatherer;

import static com.ginsberg.gatherers4j.util.GathererUtils.mustNotBeNull;
import static com.ginsberg.gatherers4j.util.GathererUtils.pushAll;

public class SampleFixedSizeGatherer<INPUT extends @Nullable Object>
        implements Gatherer<INPUT, SampleFixedSizeGatherer.State<INPUT>, INPUT> {

    private final int sampleSize;
    private final RandomGenerator randomGenerator;

    SampleFixedSizeGatherer(final int sampleSize, final RandomGenerator randomGenerator) {
        if (sampleSize < 1) {
            throw new IllegalArgumentException("sampleSize must be at least 1");
        }
        this.sampleSize = sampleSize;
        this.randomGenerator = mustNotBeNull(randomGenerator, "RandomGenerator must not be null");
    }

    @Override
    public Supplier<State<INPUT>> initializer() {
        return () -> new State<>(sampleSize, randomGenerator);
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

    public static class State<INPUT extends @Nullable Object> {
        private final List<INPUT> elements = new ArrayList<>();
        private final int sampleSize;
        private int index = 0;
        private final RandomGenerator randomGenerator;

        State(final int sampleSize, final RandomGenerator randomGenerator) {
            this.sampleSize = sampleSize;
            this.randomGenerator = randomGenerator;
        }

        void take(final @Nullable INPUT element) {
            if (index < sampleSize) {
                elements.add(element);
            } else {
                int n = randomGenerator.nextInt(0, index + 1);
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
