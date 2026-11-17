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

import com.ginsberg.gatherers4j.dto.WithRank;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.function.Supplier;
import java.util.stream.Gatherer;

import static com.ginsberg.gatherers4j.util.GathererUtils.mustNotBeNull;

public class RankGatherer<INPUT extends @Nullable Object>
        implements Gatherer<INPUT, RankGatherer.State<INPUT>, WithRank<INPUT>> {

    private final Comparator<? super INPUT> comparator;
    private final boolean dense;

    private RankGatherer(final Comparator<? super INPUT> comparator, final boolean dense) {
        this.comparator = mustNotBeNull(comparator, "Comparator must not be null");
        this.dense = dense;
    }

    static <INPUT extends @Nullable Object> RankGatherer<INPUT> dense(final Comparator<? super INPUT> comparator) {
        return new RankGatherer<>(comparator, true);
    }

    static <INPUT extends @Nullable Object> RankGatherer<INPUT> standard(final Comparator<? super INPUT> comparator) {
        return new RankGatherer<>(comparator, false);
    }

    @Override
    public Supplier<State<INPUT>> initializer() {
        return State::new;
    }

    @Override
    public Integrator<State<INPUT>, INPUT, WithRank<INPUT>> integrator() {
        return Integrator.ofGreedy((state, element, downstream) -> {
            state.count++;
            if (state.count == 1) {
                state.rank = 1;
            } else if (comparator.compare(state.previous, element) != 0) {
                state.rank = dense ? state.rank + 1 : state.count;
            }
            state.previous = element;
            return downstream.push(new WithRank<>(state.rank, element));
        });
    }

    public static class State<INPUT extends @Nullable Object> {
        long count;
        @Nullable INPUT previous;
        long rank;
    }
}
