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

import java.util.Objects;
import java.util.Spliterator;
import java.util.stream.Gatherer;
import java.util.stream.Stream;

public class InterleavingGatherer<INPUT> implements Gatherer<INPUT, Void, INPUT> {

    private final Spliterator<INPUT> otherSpliterator;

    InterleavingGatherer(final Stream<INPUT> other) {
        Objects.requireNonNull(other, "Other stream must not be null");
        otherSpliterator = other.spliterator();
    }

    @Override
    public Integrator<Void, INPUT, INPUT> integrator() {
        return (_, element, downstream) -> {
            downstream.push(element);
            return otherSpliterator.tryAdvance(downstream::push) && !downstream.isRejecting();
        };
    }
}
