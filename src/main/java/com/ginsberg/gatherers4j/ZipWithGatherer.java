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

import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Gatherer;
import java.util.stream.Stream;

import static com.ginsberg.gatherers4j.GathererUtils.mustNotBeNull;

public class ZipWithGatherer<FIRST extends @Nullable Object, SECOND extends @Nullable Object>
        implements Gatherer<FIRST, Void, Pair<FIRST, SECOND>> {
    private final Spliterator<SECOND> otherSpliterator;
    private @Nullable Function<SECOND, FIRST> sourceWhenArgumentLonger;
    private @Nullable Function<FIRST, SECOND> argumentWhenSourceLonger;

    ZipWithGatherer(final Iterable<SECOND> other) {
        mustNotBeNull(other, "Other iterable must not be null");
        otherSpliterator = other.spliterator();
    }

    ZipWithGatherer(final Iterator<SECOND> other) {
        mustNotBeNull(other, "Other iterator must not be null");
        final Iterable<SECOND> iterable = () -> other;
        otherSpliterator = iterable.spliterator();
    }

    ZipWithGatherer(final Stream<SECOND> other) {
        mustNotBeNull(other, "Other stream must not be null");
        otherSpliterator = other.spliterator();
    }

    @SafeVarargs
    ZipWithGatherer(final SECOND... other) {
        mustNotBeNull(other, "Other stream must not be null");
        otherSpliterator = Arrays.spliterator(other);
    }

    /// When the argument `Iterable`, `Iterator` or `Stream` runs out of elements before the source stream does,
    /// use the result of the `function` provided for the remaining `SECOND` elements of each `Pair`
    /// until the source is exhausted.
    ///
    /// Note: You may need a type witness when using this:
    ///
    /// `source.gather(Gatherers4j.<String, Integer>zipWith(right).argumentWhenSourceLonger(String::length))`
    ///
    /// @param mappingFunction A non-null function which takes a possibly null `<FIRST>`
    ///                        and emits a possibly null `<SECOND>`
    public ZipWithGatherer<FIRST, SECOND> argumentWhenSourceLonger(final Function<FIRST, SECOND> mappingFunction) {
        mustNotBeNull(mappingFunction, "Mapping function must not be null, use nullArgumentWhenSourceLonger() to insert nulls");
        argumentWhenSourceLonger = mappingFunction;
        return this;
    }

    /// When the source stream runs out of elements before the argument `Iterable`, `Iterator` or `Stream` does,
    /// use the result of the `function` provided for the remaining `FIRST` elements of each `Pair`
    /// until the argument is exhausted.
    ///
    /// Note: You may need a type witness when using this:
    ///
    /// `source.gather(Gatherers4j.<String, Integer>zipWith(right).sourceWhenArgumentLonger(String::valueOf))`
    ///
    /// @param mappingFunction A non-null function which takes a possibly null `<SECOND>`
    ///                        and emits a possibly null `<FIRST>`
    public ZipWithGatherer<FIRST, SECOND> sourceWhenArgumentLonger(final Function<SECOND, FIRST> mappingFunction) {
        mustNotBeNull(mappingFunction, "Mapping function must not be null, use nullSourceWhenArgumentLonger() to insert nulls");
        sourceWhenArgumentLonger = mappingFunction;
        return this;
    }

    /// When the argument `Iterable`, `Iterator` or `Stream` runs out of elements before the source stream does,
    /// use `null` for the remaining `SECOND` elements of each `Pair` until the source is exhausted.
    ///
    /// Note: You may need a type witness when using this:
    ///
    /// `source.gather(Gatherers4j.<String, Integer>zipWith(right).nullArgumentWhenSourceLonger())`
    ///
    public ZipWithGatherer<FIRST, SECOND> nullArgumentWhenSourceLonger() {
        argumentWhenSourceLonger = _ -> null;
        return this;
    }
    
    /// When the source stream runs out of elements before the argument `Iterable`, `Iterator` or `Stream` does,
    /// use `null` for the remaining `FIRST` elements of each `Pair` until the argument is exhausted.
    ///
    /// Note: You may need a type witness when using this:
    ///
    /// `source.gather(Gatherers4j.<String, Integer>zipWith(right).nullSourceWhenArgumentLonger())`
    public ZipWithGatherer<FIRST, SECOND> nullSourceWhenArgumentLonger() {
        sourceWhenArgumentLonger = _ -> null;
        return this;
    }

    @Override
    public Integrator<Void, FIRST, Pair<FIRST, SECOND>> integrator() {
        return (_, element, downstream) -> {
            boolean advanced = otherSpliterator.tryAdvance(it -> downstream.push(new Pair<>(element, it)));
            if (!advanced && argumentWhenSourceLonger != null) {
                return downstream.push(new Pair<>(element, argumentWhenSourceLonger.apply(element)));
            }
            return advanced && !downstream.isRejecting();
        };
    }

    @Override
    @SuppressWarnings("NullAway")
    public BiConsumer<Void, Downstream<? super Pair<FIRST, SECOND>>> finisher() {
        return (_, downstream) -> {
            if(sourceWhenArgumentLonger != null) {
                boolean downstreamIsRejecting = downstream.isRejecting();
                while (!downstreamIsRejecting) {
                    downstreamIsRejecting = !otherSpliterator.tryAdvance(arg ->
                            downstream.push(new Pair<>(sourceWhenArgumentLonger.apply(arg), arg))
                    );
                }
            }
        };
    }
}
