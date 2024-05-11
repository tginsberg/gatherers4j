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

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Gatherer;

public class IndexingGatherer<INPUT, OUTPUT>
        implements Gatherer<INPUT, IndexingGatherer.State, IndexedValue<OUTPUT>> {

    private final Function<INPUT, OUTPUT> mappingFunction;

    IndexingGatherer(final Function<INPUT, OUTPUT> function) {
        this.mappingFunction = function;
    }

    @Override
    public Supplier<IndexingGatherer.State> initializer() {
        return State::new;
    }

    @Override
    public Integrator<IndexingGatherer.State, INPUT, IndexedValue<OUTPUT>> integrator() {
        return (state, element, downstream) -> downstream
                .push(new IndexedValue<>(state.index++, mappingFunction.apply(element)));
    }

    public static class State {
        int index;
    }
}
