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

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Gatherer;

public class ZipWithNextGatherer<INPUT> implements Gatherer<INPUT, ZipWithNextGatherer.State<INPUT>, List<INPUT>> {
    @Override
    public Supplier<State<INPUT>> initializer() {
        return State::new;
    }

    @Override
    public Integrator<State<INPUT>, INPUT, List<INPUT>> integrator() {
        return (state, element, downstream) -> {
            if (!state.hasValue) {
                state.hasValue = true;
            } else {
                downstream.push(List.of(state.value, element));
            }
            state.value = element;
            return !downstream.isRejecting();
        };

    }

    public static class State<INPUT> {
        INPUT value;
        boolean hasValue;
    }
}
