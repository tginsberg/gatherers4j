/*
 * Copyright 2024-2026 Todd Ginsberg
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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Gatherer;

import static com.ginsberg.gatherers4j.util.GathererUtils.mustNotBeNull;

/// Keeps one entry per key, in the order each key was first seen, and emits them from the finisher. What happens
/// when a key repeats is decided by an `OnRepeat` policy. This backs `distinctLatestBy()`, `ensureDistinct()`,
/// `ensureDistinctBy()`, `uniquelyOccurring()` and `uniquelyOccurringBy()`.
///
/// The combiner applies the same policy to the right-hand entries. That is exact because every policy gives the
/// same answer no matter how a run of repeated keys is split between partitions.
///
/// Under `OnRepeat.Fail`, a repeated key marks the state failed and stops the stream. The finisher then calls
/// `onFailure()` instead of emitting anything, so a subclass can replace the exception with a fallback.
class DistinctKeyGatherer<INPUT extends @Nullable Object>
        implements Gatherer<INPUT, DistinctKeyGatherer.State, INPUT> {

    // Marks a key whose elements must all be dropped. Not null, because null is a valid element.
    private static final Object REMOVED = new Object();

    private final Function<? super INPUT, ? extends @Nullable Object> mappingFunction;
    private final OnRepeat onRepeat;

    DistinctKeyGatherer(
            final Function<? super INPUT, ? extends @Nullable Object> mappingFunction,
            final OnRepeat onRepeat
    ) {
        this.mappingFunction = mustNotBeNull(mappingFunction, "Mapping function must not be null");
        this.onRepeat = onRepeat;
    }

    @Override
    public BinaryOperator<State> combiner() {
        return (left, right) -> {
            if (right.failed) {
                left.failed = true;
            }
            if (!left.failed) {
                right.entries.forEach((key, value) -> left.add(key, value, onRepeat));
            }
            return left;
        };
    }

    @Override
    @SuppressWarnings({"unchecked", "ReferenceEquality"})
    public BiConsumer<State, Downstream<? super INPUT>> finisher() {
        return (state, downstream) -> {
            if (state.failed) {
                onFailure(downstream);
                return;
            }
            for (final @Nullable Object value : state.entries.values()) {
                if (downstream.isRejecting()) {
                    return;
                }
                // Identity on purpose: an element's equals() must never match the marker
                if (value != REMOVED) {
                    downstream.push((INPUT) value);
                }
            }
        };
    }

    @Override
    public Supplier<State> initializer() {
        return State::new;
    }

    @Override
    public Integrator<State, INPUT, INPUT> integrator() {
        if (onRepeat == OnRepeat.Fail) {
            return Integrator.of((state, element, downstream) -> {
                state.add(mappingFunction.apply(element), element, onRepeat);
                return !state.failed && !downstream.isRejecting();
            });
        }
        return Integrator.ofGreedy((state, element, downstream) -> {
            state.add(mappingFunction.apply(element), element, onRepeat);
            return !downstream.isRejecting();
        });
    }

    void onFailure(final Downstream<? super INPUT> downstream) {
        throw new IllegalStateException("Elements are not distinct");
    }

    enum OnRepeat {
        Fail,
        KeepLatest,
        RemoveAll
    }

    static class State {
        // Re-inserting a key keeps its original position, so output follows each key's first appearance
        final Map<@Nullable Object, @Nullable Object> entries = new LinkedHashMap<>();
        boolean failed = false;

        void add(final @Nullable Object key, final @Nullable Object value, final OnRepeat onRepeat) {
            if (!entries.containsKey(key)) {
                entries.put(key, value);
                return;
            }
            switch (onRepeat) {
                case Fail -> failed = true;
                case KeepLatest -> entries.put(key, value);
                case RemoveAll -> entries.put(key, REMOVED);
            }
        }
    }
}
