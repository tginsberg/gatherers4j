/*
 * Copyright 2025 Todd Ginsberg
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

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Gatherer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.ginsberg.gatherers4j.GathererUtils.mustNotBeNull;

public class CrossGatherer<INPUT extends @Nullable Object, CROSS extends @Nullable Object>
        implements Gatherer<INPUT, Void, Pair<INPUT, CROSS>> {

    private final Iterable<CROSS> crossWith;

    CrossGatherer(final Iterator<CROSS> crossWith) {
        mustNotBeNull(crossWith, "crossWith iterator must not be null");
        this.crossWith = StreamSupport.stream(Spliterators.spliteratorUnknownSize(crossWith, Spliterator.ORDERED), false).toList();
    }

    CrossGatherer(final Iterable<CROSS> crossWith) {
        mustNotBeNull(crossWith, "crossWith list must not be null");
        this.crossWith = crossWith;
    }

    CrossGatherer(final Stream<CROSS> crossWith) {
        mustNotBeNull(crossWith, "crossWith stream must not be null");
        this.crossWith = crossWith.toList();
    }

    @Override
    public Integrator<Void, INPUT, Pair<INPUT, CROSS>> integrator() {
        return Integrator.ofGreedy((_, element, downstream) -> {
            for (final CROSS cross : crossWith) {
                downstream.push(new Pair<>(element, cross));
            }
            return !downstream.isRejecting();
        });
    }

}
