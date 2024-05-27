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

import java.util.function.Supplier;
import java.util.stream.Gatherer;

public class DroppingGatherer<INPUT> implements Gatherer<INPUT, DroppingGatherer.State, INPUT> {

    private final long dropCount;

    public DroppingGatherer(long dropCount) {
        if (dropCount < 0) {
            throw new IllegalArgumentException("Drop count cannot be negative");
        }
        this.dropCount = dropCount;
    }

    @Override
    public Supplier<DroppingGatherer.State> initializer() {
        return State::new;
    }

    @Override
    public Integrator<DroppingGatherer.State, INPUT, INPUT> integrator() {
        return (state, element, downstream) -> {
            if (state.count++ >= dropCount) {
                return downstream.push(element);
            }
            return !downstream.isRejecting();
        };
    }

    public static class State {
        long count;
    }
}
