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

import com.ginsberg.gatherers4j.util.CircularBuffer;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;

/// Per-stream state of the BigDecimal statistics gatherers. This type is public only because it appears in the
/// signatures of the public gatherer types. It has no public members.
///
/// @param <OUTPUT> Type of the statistic the gatherer emits
public final class StatGathererState<OUTPUT extends @Nullable Object> {

    private final StatisticAccumulator<OUTPUT> accumulator;
    private final @Nullable CircularBuffer<BigDecimal> window;

    private StatGathererState(
            final StatisticAccumulator<OUTPUT> accumulator,
            final @Nullable CircularBuffer<BigDecimal> window
    ) {
        this.accumulator = accumulator;
        this.window = window;
    }

    static <OUTPUT extends @Nullable Object> StatGathererState<OUTPUT> ofRunning(
            final StatisticAccumulator<OUTPUT> accumulator
    ) {
        return new StatGathererState<>(accumulator, null);
    }

    static <OUTPUT extends @Nullable Object> StatGathererState<OUTPUT> ofMoving(
            final StatisticAccumulator<OUTPUT> accumulator,
            final CircularBuffer<BigDecimal> window
    ) {
        return new StatGathererState<>(accumulator, window);
    }

    StatisticAccumulator<OUTPUT> accumulator() {
        return accumulator;
    }

    @Nullable CircularBuffer<BigDecimal> window() {
        return window;
    }
}
