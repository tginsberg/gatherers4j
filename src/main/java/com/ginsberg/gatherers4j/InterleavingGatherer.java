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

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.stream.Gatherer;
import java.util.stream.Stream;

import static com.ginsberg.gatherers4j.GathererUtils.mustNotBeNull;

public class InterleavingGatherer<INPUT> implements Gatherer<INPUT, Void, INPUT> {

    private final Spliterator<INPUT> otherSpliterator;
    private boolean appendArgumentIfLonger;
    private boolean appendSourceIfLonger;

    InterleavingGatherer(final Iterable<INPUT> other) {
        mustNotBeNull(other, "Other iterable must not be null");
        otherSpliterator = other.spliterator();
    }

    InterleavingGatherer(final Iterator<INPUT> other) {
        mustNotBeNull(other, "Other iterable must not be null");
        final Iterable<INPUT> iterable = () -> other;
        otherSpliterator = iterable.spliterator();
    }

    InterleavingGatherer(final Stream<INPUT> other) {
        mustNotBeNull(other, "Other stream must not be null");
        otherSpliterator = other.spliterator();
    }

    /// If the source stream and the argument stream/iterator/iterable provide a different
    /// number of elements, append all the remaining elements from either one to the output stream.
    public InterleavingGatherer<INPUT> appendLonger() {
        this.appendArgumentIfLonger = true;
        this.appendSourceIfLonger = true;
        return this;
    }

    /// If the argument stream/iterator/iterable provides more elements than the source stream,
    /// append all remaining elements from the argument stream/iterator/iterable to the output stream.
    public InterleavingGatherer<INPUT> appendArgumentIfLonger() {
        this.appendArgumentIfLonger = true;
        return this;
    }

    /// If the source stream provides more elements than the argument stream/iterator/iterable,
    /// append all the remaining elements to the output stream.
    public InterleavingGatherer<INPUT> appendSourceIfLonger() {
        this.appendSourceIfLonger = true;
        return this;
    }

    @Override
    public Integrator<Void, INPUT, INPUT> integrator() {
        return (_, element, downstream) -> {
            downstream.push(element);
            if (appendSourceIfLonger) {
                otherSpliterator.tryAdvance(downstream::push);
                return !downstream.isRejecting();
            } else {
                // End immediately if we are not appending source if it is longer and other is finished
                return otherSpliterator.tryAdvance(downstream::push) && !downstream.isRejecting();
            }
        };
    }

    @Override
    public BiConsumer<Void, Downstream<? super INPUT>> finisher() {
        return (_, downstream) -> {
            boolean downstreamRejecting = downstream.isRejecting();
            while (appendArgumentIfLonger && !downstreamRejecting) {
                downstreamRejecting = !otherSpliterator.tryAdvance(downstream::push);
            }
        };
    }
}
