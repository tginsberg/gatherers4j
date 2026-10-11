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

package com.ginsberg.gatherers4j.dto;

import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;

/// Summary statistics over the values seen so far (running) or the values in the current window (moving).
///
/// @param count                       The number of values summarized. Skipped `null` values are not counted.
/// @param sum                         The sum of the values
/// @param mean                        The arithmetic mean of the values
/// @param min                         The smallest value
/// @param max                         The largest value
/// @param populationVariance          The variance, treating the values as the entire population
/// @param populationStandardDeviation The standard deviation, treating the values as the entire population
/// @param sampleVariance              The variance, treating the values as a sample of a larger population.
///                                    `null` when fewer than two values have been summarized.
/// @param sampleStandardDeviation     The standard deviation, treating the values as a sample of a larger
///                                    population. `null` when fewer than two values have been summarized.
public record StatisticalSummary(
        long count,
        BigDecimal sum,
        BigDecimal mean,
        BigDecimal min,
        BigDecimal max,
        BigDecimal populationVariance,
        BigDecimal populationStandardDeviation,
        @Nullable BigDecimal sampleVariance,
        @Nullable BigDecimal sampleStandardDeviation
) {
}
