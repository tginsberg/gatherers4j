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

public class DropLastGatherer<INPUT> implements Gatherer<INPUT, DropLastGatherer.State<INPUT>, INPUT> {

    private final int count;

    DropLastGatherer(final int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("DropLast count must be positive");
        }
        this.count = count;
    }

    @Override
    public Supplier<State<INPUT>> initializer() {
        return State::new;
    }

    @Override
    public Integrator<State<INPUT>, INPUT, INPUT> integrator() {
        return (state, element, downstream) -> {
            state.elements.add(element);
            return !downstream.isRejecting();
        };
    }

    @Override
    public BiConsumer<State<INPUT>, Downstream<? super INPUT>> finisher() {
        return (inputState, downstream) -> {
            for (int i = 0; i < inputState.elements.size() - count && !downstream.isRejecting(); i++) {
                downstream.push(inputState.elements.get(i));
            }
        };
    }

    public static class State<INPUT> {
        final List<INPUT> elements = new ArrayList<>();
    }
}
