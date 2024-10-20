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

import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.stream.Gatherer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.ginsberg.gatherers4j.GathererUtils.mustNotBeNull;

public class ZipWithGatherer<FIRST, SECOND> implements Gatherer<FIRST, Void, Pair<FIRST, SECOND>> {
    private final Spliterator<SECOND> otherSpliterator;

    ZipWithGatherer(final Collection<SECOND> other) {
        mustNotBeNull(other, "Other collection must not be null");
        this(other.stream());
    }

    ZipWithGatherer(final Iterator<SECOND> other) {
        mustNotBeNull(other, "Other iterator must not be null");
        final Iterable<SECOND> iterable = () -> other;
        this(StreamSupport.stream(iterable.spliterator(), false));
    }

    ZipWithGatherer(final Stream<SECOND> other) {
        mustNotBeNull(other, "Other stream must not be null");
        otherSpliterator = other.spliterator();
    }

    @Override
    public Integrator<Void, FIRST, Pair<FIRST, SECOND>> integrator() {
        return (_, element, downstream) -> otherSpliterator
                .tryAdvance(
                        it -> downstream.push(new Pair<>(element, it))
                ) && !downstream.isRejecting();
    }
}
