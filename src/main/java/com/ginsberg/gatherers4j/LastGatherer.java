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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Gatherer;

public class LastGatherer<INPUT extends @Nullable Object>
        implements Gatherer<INPUT, LastGatherer.State<INPUT>, INPUT> {

    private final int lastCount;

    LastGatherer(int lastCount) {
        if (lastCount < 0) {
            throw new IllegalArgumentException("Last count must not be negative");
        }
        this.lastCount = lastCount;
    }

    @Override
    public BiConsumer<State<INPUT>, Downstream<? super INPUT>> finisher() {
        return (state, downstream) -> {
            final Iterator<INPUT> iterator = state.elements.iterator();
            while (iterator.hasNext() && !downstream.isRejecting()) {
                downstream.push(iterator.next());
            }
        };
    }

    @Override
    public Supplier<State<INPUT>> initializer() {
        return State::new;
    }

    @Override
    public Integrator<State<INPUT>, INPUT, INPUT> integrator() {
        return Integrator.ofGreedy((state, element, downstream) -> {
            if (state.elements.size() == lastCount) {
                state.elements.removeFirst();
            }
            state.elements.add(element);
            return !downstream.isRejecting();
        });
    }

    public static class State<INPUT> {
        final Deque<INPUT> elements = new ArrayDeque<>();
    }
}
