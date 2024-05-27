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

public class TakingGatherer<INPUT> implements Gatherer<INPUT, TakingGatherer.State, INPUT> {

    private final long takeCount;

    public TakingGatherer(long takeCount) {
        if (takeCount < 0) {
            throw new IllegalArgumentException("Take count cannot be negative");
        }
        this.takeCount = takeCount;
    }

    @Override
    public Supplier<TakingGatherer.State> initializer() {
        return State::new;
    }

    @Override
    public Integrator<TakingGatherer.State, INPUT, INPUT> integrator() {
        return (state, element, downstream) -> {
            if (state.count++ < takeCount) {
                return downstream.push(element);
            }
            return !downstream.isRejecting();
        };
    }

    public static class State {
        long count;
    }
}
