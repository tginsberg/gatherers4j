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

import com.ginsberg.gatherers4j.util.CircularBuffer;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;
import java.util.stream.Gatherer;

public class DropLastGatherer<INPUT extends @Nullable Object> implements Gatherer<INPUT, DropLastGatherer.State<INPUT>, INPUT> {

    private final int count;

    DropLastGatherer(final int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("DropLast count must be positive");
        }
        this.count = count;
    }

    @Override
    public Supplier<State<INPUT>> initializer() {
        return () -> new State<>(count);
    }

    @Override
    public Integrator<State<INPUT>, INPUT, INPUT> integrator() {
        return Integrator.ofGreedy((state, element, downstream) -> {
            if (state.elements.size() == count) {
                downstream.push(state.elements.removeFirst());
            }
            state.elements.add(element);
            return !downstream.isRejecting();
        });
    }

    public static class State<INPUT extends @Nullable Object> {
        final CircularBuffer<INPUT> elements;

        State(int capacity) {
            elements = new CircularBuffer<>(capacity);
        }
    }
}
