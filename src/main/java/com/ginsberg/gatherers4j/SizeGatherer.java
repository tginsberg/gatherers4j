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

public class SizeGatherer<INPUT> implements Gatherer<INPUT, SizeGatherer.State<INPUT>, INPUT> {

    private final long targetSize;

    SizeGatherer(long targetSize) {
        if (targetSize < 0) {
            throw new IllegalArgumentException("Target size cannot be negative");
        }
        this.targetSize = targetSize;
    }

    @Override
    public Supplier<State<INPUT>> initializer() {
        return State::new;
    }

    @Override
    public Integrator<State<INPUT>, INPUT, INPUT> integrator() {
        return (state, element, downstream) -> {
            state.elements.add(element);
            if (state.elements.size() > targetSize) {
                fail();
            }
            return !downstream.isRejecting();
        };
    }

    @Override
    public BiConsumer<State<INPUT>, Downstream<? super INPUT>> finisher() {
        return (state, downstream) -> {
            if (state.elements.size() == targetSize) {
                state.elements.forEach(downstream::push);
            } else {
                fail();
            }
        };
    }

    private void fail() {
        throw new IllegalStateException("Size must be exactly " + targetSize);
    }

    public static class State<INPUT> {
        final List<INPUT> elements = new ArrayList<>();
    }
}
