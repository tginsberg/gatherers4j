/*
 * Copyright 2026 Todd Ginsberg
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

package com.ginsberg.gatherers4j.nullness;

import com.ginsberg.gatherers4j.Gatherers4j;
import com.ginsberg.gatherers4j.ZipWithGatherer;
import com.ginsberg.gatherers4j.dto.Pair;
import com.ginsberg.gatherers4j.dto.WithCount;
import com.ginsberg.gatherers4j.dto.WithIndex;
import com.ginsberg.gatherers4j.dto.WithOriginal;
import com.ginsberg.gatherers4j.enums.Dataset;
import com.ginsberg.gatherers4j.enums.Frequency;
import com.ginsberg.gatherers4j.enums.Order;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

// Each statement is a call that the library handles correctly at runtime, so its signature must accept it.
// NullAway (0.14.1, JSpecify mode) checks a method reference against the functional interface it becomes, but not
// generic type arguments or type variable bounds, so every function here is a method reference. The nullable streams
// at the bottom compile with or without the right bounds; they're here for a stricter checker, such as Kotlin's.
@SuppressWarnings({"ResultOfMethodCallIgnored", "unused"})
final class NullnessUsage {

    record Trade(String id, @Nullable BigDecimal price, @Nullable String desk) {}

    private NullnessUsage() {
    }

    // BigDecimal statistics skip a null mapped value, or replace it with treatNullAs*().
    static void bigDecimalMappingFunctionsMayReturnNull(final Stream<Trade> trades) {
        final List<BigDecimal> weights = List.of(BigDecimal.ONE, BigDecimal.TWO);

        trades.gather(Gatherers4j.exponentialMovingAverageWithAlphaBy(0.5, Trade::price));
        trades.gather(Gatherers4j.exponentialMovingAverageWithPeriodBy(3, Trade::price));
        trades.gather(Gatherers4j.movingGeometricMeanBy(3, Trade::price));
        trades.gather(Gatherers4j.movingHarmonicMeanBy(3, Trade::price));
        trades.gather(Gatherers4j.movingMeanBy(3, Trade::price));
        trades.gather(Gatherers4j.movingMedianBy(3, Trade::price));
        trades.gather(Gatherers4j.movingPercentileBy(95, 3, Trade::price));
        trades.gather(Gatherers4j.movingProductBy(3, Trade::price));
        trades.gather(Gatherers4j.movingRootMeanSquareBy(3, Trade::price));
        trades.gather(Gatherers4j.movingStandardDeviationBy(Dataset.Population, 3, Trade::price));
        trades.gather(Gatherers4j.movingStatisticsBy(3, Trade::price));
        trades.gather(Gatherers4j.movingSumBy(3, Trade::price).treatNullAsZero());
        trades.gather(Gatherers4j.movingVarianceBy(Dataset.Sample, 3, Trade::price));
        trades.gather(Gatherers4j.movingWeightedMeanBy(3, Trade::price));
        trades.gather(Gatherers4j.movingWeightedMeanBy(weights, Trade::price));
        trades.gather(Gatherers4j.runningGeometricMeanBy(Trade::price));
        trades.gather(Gatherers4j.runningHarmonicMeanBy(Trade::price));
        trades.gather(Gatherers4j.runningMeanBy(Trade::price));
        trades.gather(Gatherers4j.runningMedianBy(Trade::price));
        trades.gather(Gatherers4j.runningPercentileBy(95, Trade::price));
        trades.gather(Gatherers4j.runningProductBy(Trade::price));
        trades.gather(Gatherers4j.runningRootMeanSquareBy(Trade::price));
        trades.gather(Gatherers4j.runningStandardDeviationBy(Dataset.Population, Trade::price));
        trades.gather(Gatherers4j.runningStatisticsBy(Trade::price));
        trades.gather(Gatherers4j.runningSumBy(Trade::price).treatNullAsZero());
        trades.gather(Gatherers4j.runningVarianceBy(Dataset.Sample, Trade::price));
    }

    // Key functions compare keys with Objects.equals, so a null key is just another key.
    static void keyFunctionsMayReturnNull(final Stream<Trade> trades) {
        trades.gather(Gatherers4j.dedupeConsecutiveBy(Trade::desk));
        trades.gather(Gatherers4j.distinctBy(Trade::desk));
        trades.gather(Gatherers4j.distinctLatestBy(Trade::desk));
        trades.gather(Gatherers4j.ensureDistinctBy(Trade::desk));
        trades.gather(Gatherers4j.groupBy(Trade::desk));
        trades.gather(Gatherers4j.uniquelyOccurringBy(Trade::desk));
    }

    // On a stream without nulls, a key function need not accept null.
    static void keyFunctionsNeedNotAcceptNull(final Stream<String> strings) {
        strings.gather(Gatherers4j.groupBy(String::length));
    }

    // These gatherers pass null elements through, so they must accept a stream that has them.
    static void nullableStreams(final Stream<@Nullable String> strings) {
        final Comparator<@Nullable String> comparator = Comparator.nullsFirst(Comparator.naturalOrder());

        strings.gather(Gatherers4j.ensureOrderedBy(Order.Ascending, comparator));
        strings.gather(Gatherers4j.filterOrderedBy(Order.Ascending, comparator));
        strings.gather(Gatherers4j.takeLast(2));
    }

    // Record components are as nullable as their type arguments, so values from a stream without nulls need no check.
    static void dtoComponentsFollowTypeArguments(final Stream<String> strings, final Stream<Trade> trades) {
        final Pair<String, Integer> pair = strings.gather(Gatherers4j.zipWith(List.of(1))).toList().getFirst();
        final WithCount<String> withCount = Stream.of("A").gather(Gatherers4j.orderByFrequency(Frequency.Ascending)).toList().getFirst();
        final WithIndex<String> withIndex = Stream.of("A").gather(Gatherers4j.withIndex()).toList().getFirst();
        final WithOriginal<Trade, BigDecimal> withOriginal = trades.gather(Gatherers4j.runningSumBy(Trade::price).withOriginal()).toList().getFirst();

        pair.first().length();
        pair.second().intValue();
        withCount.value().length();
        withIndex.value().length();
        withOriginal.original().id().length();
        withOriginal.calculated().signum();
    }

    // zipWith() fills in null once one side runs out, so the type says that side is nullable. This checks the gatherer
    // rather than the stream, because NullAway drops the @Nullable type argument when inferring Stream.gather()'s result.
    static void zipWithNullFillIsNullable() {
        final ZipWithGatherer<String, @Nullable Integer> nullSecond =
                Gatherers4j.<String, Integer>zipWith(List.of(1)).nullArgumentWhenSourceLonger();
        final ZipWithGatherer<@Nullable String, Integer> nullFirst =
                Gatherers4j.<String, Integer>zipWith(List.of(1)).nullSourceWhenArgumentLonger();
    }
}
