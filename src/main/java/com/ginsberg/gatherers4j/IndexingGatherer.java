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

public class IndexingGatherer<INPUT>
        implements Gatherer<INPUT, IndexingGatherer.State, IndexedValue<INPUT>> {

    private long start = 0;

    IndexingGatherer() {
    }

    public IndexingGatherer<INPUT> startingAt(long start) {
        this.start = start;
        return this;
    }

    @Override
    public Supplier<IndexingGatherer.State> initializer() {
        return () -> new State(start);
    }

    @Override
    public Integrator<IndexingGatherer.State, INPUT, IndexedValue<INPUT>> integrator() {
        return (state, element, downstream) -> downstream.push(new IndexedValue<>(state.index++, element));
    }

    public static class State {
        long index;

        public State(long start) {
            this.index = start;
        }
    }
}
