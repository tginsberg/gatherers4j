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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Gatherer;

import static com.ginsberg.gatherers4j.util.GathererUtils.mustNotBeNull;
import static com.ginsberg.gatherers4j.util.GathererUtils.pushAll;

// Both gatherers hold the current run of elements that match the predicate. A non-matching element ends the run.
class LastWhileGatherers {

    private LastWhileGatherers() {
    }

    // Emits elements as they arrive, except the current run, which is emitted once a non-matching element ends it
    // and discarded if the stream ends first.
    static <INPUT extends @Nullable Object> Gatherer<INPUT, ?, INPUT> dropLastWhile(
            final Predicate<? super INPUT> predicate
    ) {
        mustNotBeNull(predicate, "Predicate must not be null");
        return Gatherer.ofSequential(
                State<INPUT>::new,
                Gatherer.Integrator.ofGreedy((state, element, downstream) -> {
                    if (predicate.test(element)) {
                        state.run.add(element);
                        return true;
                    }
                    pushAll(state.run, downstream);
                    state.run.clear();
                    return !downstream.isRejecting() && downstream.push(element);
                })
        );
    }

    // Emits the final run when the stream ends. A partition whose elements all match extends the run of the
    // partition before it, so the state merges exactly.
    static <INPUT extends @Nullable Object> Gatherer<INPUT, ?, INPUT> takeLastWhile(
            final Predicate<? super INPUT> predicate
    ) {
        mustNotBeNull(predicate, "Predicate must not be null");
        return Gatherer.of(
                State<INPUT>::new,
                Gatherer.Integrator.ofGreedy((state, element, downstream) -> {
                    if (predicate.test(element)) {
                        state.run.add(element);
                    } else {
                        state.run.clear();
                        state.allMatched = false;
                    }
                    return !downstream.isRejecting();
                }),
                (left, right) -> {
                    if (right.allMatched) {
                        left.run.addAll(right.run);
                        return left;
                    }
                    return right;
                },
                (state, downstream) -> pushAll(state.run, downstream)
        );
    }

    private static class State<INPUT extends @Nullable Object> {
        boolean allMatched = true;
        final List<INPUT> run = new ArrayList<>();
    }
}
