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

public class ReversingGatherer<INPUT> implements Gatherer<INPUT, ReversingGatherer.State<INPUT>, INPUT> {

    @Override
    public Supplier<ReversingGatherer.State<INPUT>> initializer() {
        return State::new;
    }

    @Override
    public Integrator<State<INPUT>, INPUT, INPUT> integrator() {
        return Integrator.ofGreedy((state, element, downstream) -> {
            state.inputs.add(element);
            return !downstream.isRejecting();
        });
    }

    @Override
    public BiConsumer<ReversingGatherer.State<INPUT>, Downstream<? super INPUT>> finisher() {
        return (state, downstream) -> {
            for (int i = state.inputs.size() - 1; i >= 0 && !downstream.isRejecting(); i--) {
                downstream.push(state.inputs.get(i));
            }
        };
    }

    public static class State<INPUT> {
        List<INPUT> inputs = new ArrayList<>();
    }
}
