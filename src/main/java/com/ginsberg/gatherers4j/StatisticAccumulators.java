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

import com.ginsberg.gatherers4j.dto.StatisticalSummary;
import com.ginsberg.gatherers4j.enums.Dataset;
import com.ginsberg.gatherers4j.util.MathUtils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.ginsberg.gatherers4j.util.GathererUtils.mustNotBeNull;

class StatisticAccumulators {

    final static class CustomWeightedMeanAccumulator implements StatisticAccumulator<BigDecimal> {
        private final List<BigDecimal> weights;
        private final BigDecimal[] weightSumFrom;
        private final ArrayDeque<BigDecimal> window = new ArrayDeque<>();

        /// @param validWeights Weights, oldest to newest, already checked by [#validatedWeights(List)]
        CustomWeightedMeanAccumulator(final List<BigDecimal> validWeights) {
            this.weights = validWeights;
            // weightSumFrom[i] is the sum of weights[i..n-1], the divisor when the window holds n-i values.
            this.weightSumFrom = new BigDecimal[validWeights.size()];
            BigDecimal running = BigDecimal.ZERO;
            for (int i = validWeights.size() - 1; i >= 0; i--) {
                running = running.add(validWeights.get(i));
                weightSumFrom[i] = running;
            }
        }

        /// Validate the weights, and return an immutable copy of them.
        static List<BigDecimal> validatedWeights(final List<BigDecimal> weights) {
            mustNotBeNull(weights, "Weights must not be null");
            if (weights.size() < 2) {
                throw new IllegalArgumentException("Weights must contain at least 2 values");
            }
            BigDecimal sum = BigDecimal.ZERO;
            for (final BigDecimal weight : weights) {
                mustNotBeNull(weight, "Weights must not contain null");
                if (weight.signum() < 0) {
                    throw new IllegalArgumentException("Weights must not be negative");
                }
                sum = sum.add(weight);
            }
            if (sum.signum() == 0) {
                throw new IllegalArgumentException("Weights must not sum to zero");
            }
            return List.copyOf(weights);
        }

        @Override
        public void add(final BigDecimal value, final MathContext mc) {
            window.addLast(value.round(mc));
        }

        @Override
        public void evict(final BigDecimal oldest, final MathContext mc) {
            window.removeFirst();
        }

        @Override
        public boolean isReady() {
            // A partial window uses the newest weights, which sum to zero if they are all zero.
            return !window.isEmpty() && weightSumFrom[weights.size() - window.size()].signum() != 0;
        }

        @Override
        public BigDecimal value(final MathContext mc) {
            final int offset = weights.size() - window.size();
            BigDecimal weighted = BigDecimal.ZERO;
            int i = offset;
            for (final BigDecimal value : window) {
                weighted = weighted.add(value.multiply(weights.get(i++)));
            }
            return weighted.divide(weightSumFrom[offset], mc);
        }
    }

    final static class EmaAccumulator implements StatisticAccumulator<BigDecimal> {
        private final BigDecimal alpha;
        private final BigDecimal oneMinusAlpha;
        private boolean first = true;
        private BigDecimal ema = BigDecimal.ZERO;

        EmaAccumulator(final double alpha) {
            if (alpha <= 0 || alpha >= 1.0) {
                throw new IllegalArgumentException("alpha must be between 0.0 and 1.0, exclusive, got " + alpha);
            }
            this.alpha = BigDecimal.valueOf(alpha);
            this.oneMinusAlpha = BigDecimal.ONE.subtract(this.alpha);
        }

        @Override
        public void add(final BigDecimal value, final MathContext mc) {
            if (first) {
                first = false;
                ema = value;
            } else {
                ema = value.multiply(alpha).add(ema.multiply(oneMinusAlpha)).round(mc);
            }
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public BigDecimal value(final MathContext mc) {
            return ema.round(mc);
        }
    }

    final static class GeometricMeanAccumulator implements StatisticAccumulator<BigDecimal> {
        private final Product state;

        private GeometricMeanAccumulator(final boolean moving) {
            this.state = new Product(moving);
        }

        static GeometricMeanAccumulator moving() {
            return new GeometricMeanAccumulator(true);
        }

        static GeometricMeanAccumulator running() {
            return new GeometricMeanAccumulator(false);
        }

        @Override
        public void add(final BigDecimal value, final MathContext mc) {
            state.add(value.round(mc), mc);
        }

        @Override
        public void evict(final BigDecimal oldest, final MathContext mc) {
            state.evict(oldest.round(mc), mc);
        }

        @Override
        public boolean isReady() {
            return state.count() > 0;
        }

        @Override
        public BigDecimal value(final MathContext mc) {
            return MathUtils.nthRoot(state.product(), state.count(), mc);
        }
    }

    final static class HarmonicMeanAccumulator implements StatisticAccumulator<BigDecimal> {
        private final CountSum state = new CountSum();

        @Override
        public void add(final BigDecimal value, final MathContext mc) {
            state.add(BigDecimal.ONE.divide(value, mc));
        }

        @Override
        public void evict(final BigDecimal oldest, final MathContext mc) {
            state.evict(BigDecimal.ONE.divide(oldest, mc));
        }

        @Override
        public boolean isReady() {
            return state.count() > 0;
        }

        @Override
        public BigDecimal value(final MathContext mc) {
            return BigDecimal.valueOf(state.count()).divide(state.sum, mc);
        }
    }

    final static class MeanAccumulator implements StatisticAccumulator<BigDecimal> {
        private final CountSum state = new CountSum();

        @Override
        public void add(final BigDecimal value, final MathContext mc) {
            state.add(value.round(mc));
        }

        @Override
        public void evict(final BigDecimal oldest, final MathContext mc) {
            state.evict(oldest.round(mc));
        }

        @Override
        public boolean isReady() {
            return state.count() > 0;
        }

        @Override
        public BigDecimal value(final MathContext mc) {
            return state.mean(mc);
        }
    }

    final static class PercentileAccumulator implements StatisticAccumulator<BigDecimal> {
        static final BigDecimal MEDIAN = BigDecimal.valueOf(50);

        private final SortedWindow state = new SortedWindow();
        private final BigDecimal percentile;

        // Expects a percentile already validated by percentileOf
        PercentileAccumulator(final BigDecimal percentile) {
            this.percentile = percentile;
        }

        static BigDecimal percentileOf(final double percentile) {
            if (Double.isNaN(percentile) || percentile < 0 || percentile > 100) {
                throw new IllegalArgumentException("Percentile must be between 0 and 100, inclusive");
            }
            return BigDecimal.valueOf(percentile);
        }

        @Override
        public void add(final BigDecimal value, final MathContext mc) {
            state.add(value.round(mc));
        }

        @Override
        public void evict(final BigDecimal oldest, final MathContext mc) {
            state.evict(oldest.round(mc));
        }

        @Override
        public boolean isReady() {
            return state.size() > 0;
        }

        @Override
        public BigDecimal value(final MathContext mc) {
            return state.percentile(percentile, mc);
        }
    }

    final static class ProductAccumulator implements StatisticAccumulator<BigDecimal> {
        private final Product state;

        private ProductAccumulator(final boolean moving) {
            this.state = new Product(moving);
        }

        static ProductAccumulator moving() {
            return new ProductAccumulator(true);
        }

        static ProductAccumulator running() {
            return new ProductAccumulator(false);
        }

        @Override
        public void add(final BigDecimal value, final MathContext mc) {
            state.add(value.round(mc), mc);
        }

        @Override
        public void evict(final BigDecimal oldest, final MathContext mc) {
            state.evict(oldest.round(mc), mc);
        }

        @Override
        public boolean isReady() {
            return state.count() > 0;
        }

        @Override
        public BigDecimal value(final MathContext mc) {
            return state.product();
        }
    }

    final static class RootMeanSquareAccumulator implements StatisticAccumulator<BigDecimal> {
        private final CountSum state = new CountSum();

        @Override
        public void add(final BigDecimal value, final MathContext mc) {
            state.add(value.pow(2).round(mc));
        }

        @Override
        public void evict(final BigDecimal oldest, final MathContext mc) {
            state.evict(oldest.pow(2).round(mc));
        }

        @Override
        public boolean isReady() {
            return state.count() > 0;
        }

        @Override
        public BigDecimal value(final MathContext mc) {
            return state.mean(mc).sqrt(mc);
        }
    }

    final static class StatisticalSummaryAccumulator implements StatisticAccumulator<StatisticalSummary> {
        private final Moments moments = new Moments();
        private final Extremes extremes;

        StatisticalSummaryAccumulator(final boolean windowed) {
            this.extremes = new Extremes(windowed);
        }

        @Override
        public void add(final BigDecimal value, final MathContext mc) {
            final BigDecimal rounded = value.round(mc);
            moments.add(rounded);
            extremes.add(rounded);
        }

        @Override
        public void evict(final BigDecimal oldest, final MathContext mc) {
            final BigDecimal rounded = oldest.round(mc);
            moments.evict(rounded);
            extremes.evict(rounded);
        }

        @Override
        public boolean isReady() {
            return moments.count() > 0;
        }

        @Override
        public StatisticalSummary value(final MathContext mc) {
            final BigDecimal populationVariance = moments.populationVariance(mc);
            final BigDecimal sampleVariance = moments.count() > 1 ? moments.sampleVariance(mc) : null;

            return new StatisticalSummary(
                    moments.count(),
                    moments.sum().round(mc),
                    moments.mean(mc),
                    extremes.min(),
                    extremes.max(),
                    populationVariance,
                    populationVariance.sqrt(mc),
                    sampleVariance,
                    sampleVariance == null ? null : sampleVariance.sqrt(mc)
            );
        }
    }

    final static class StdDevAccumulator implements StatisticAccumulator<BigDecimal> {
        private final Moments state = new Moments();
        private final Dataset mode;

        StdDevAccumulator(final Dataset mode) {
            this.mode = mustNotBeNull(mode, "Must specify a mode for Standard Deviation");
        }

        @Override
        public void add(final BigDecimal value, final MathContext mc) {
            state.add(value.round(mc));
        }

        @Override
        public void evict(final BigDecimal oldest, final MathContext mc) {
            state.evict(oldest.round(mc));
        }

        @Override
        public boolean isReady() {
            return state.count() > (mode == Dataset.Sample ? 1 : 0);
        }

        @Override
        public BigDecimal value(final MathContext mc) {
            return mode == Dataset.Population
                    ? state.populationStandardDeviation(mc)
                    : state.sampleStandardDeviation(mc);
        }
    }

    final static class SumAccumulator implements StatisticAccumulator<BigDecimal> {
        private final CountSum state = new CountSum();

        @Override
        public void add(final BigDecimal value, final MathContext mc) {
            state.add(value.round(mc));
        }

        @Override
        public void evict(final BigDecimal oldest, final MathContext mc) {
            state.evict(oldest.round(mc));
        }

        @Override
        public boolean isReady() {
            return state.count() > 0;
        }

        @Override
        public BigDecimal value(final MathContext mc) {
            return state.sum().round(mc);
        }
    }

    final static class VarianceAccumulator implements StatisticAccumulator<BigDecimal> {
        private final Moments state = new Moments();
        private final Dataset mode;

        VarianceAccumulator(final Dataset mode) {
            this.mode = mustNotBeNull(mode, "Must specify a mode for Variance");
        }

        @Override
        public void add(final BigDecimal value, final MathContext mc) {
            state.add(value.round(mc));
        }

        @Override
        public void evict(final BigDecimal oldest, final MathContext mc) {
            state.evict(oldest.round(mc));
        }

        @Override
        public boolean isReady() {
            return state.count() > (mode == Dataset.Sample ? 1 : 0);
        }

        @Override
        public BigDecimal value(final MathContext mc) {
            return mode == Dataset.Population
                    ? state.populationVariance(mc)
                    : state.sampleVariance(mc);
        }
    }

    /// Linearly weighted moving mean over a window of size n. The weights are 1 through n from oldest to newest,
    /// and a partial window of k values uses the newest k weights, (n-k+1) through n. This gives the same results as
    /// [CustomWeightedMeanAccumulator] with the weights 1 through n. Keeps the window sum and the weighted sum, so
    /// each step is O(1).
    ///
    /// The integrator evicts the oldest value of a full window before adding the new value. A full window's oldest
    /// value has weight 1, so evicting it removes exactly its value from the weighted sum. Adding a value lowers the
    /// weight of every existing value by one, which is the same as subtracting the window sum, and gives the new
    /// value weight n.
    final static class WeightedMeanAccumulator implements StatisticAccumulator<BigDecimal> {
        private final BigDecimal windowSize;
        private long count = 0;
        private BigDecimal sum = BigDecimal.ZERO;
        private BigDecimal weightedSum = BigDecimal.ZERO;

        WeightedMeanAccumulator(final int windowSize) {
            this.windowSize = BigDecimal.valueOf(windowSize);
        }

        @Override
        public void add(final BigDecimal value, final MathContext mc) {
            final BigDecimal rounded = value.round(mc);
            weightedSum = weightedSum.subtract(sum).add(rounded.multiply(windowSize));
            sum = sum.add(rounded);
            count++;
        }

        @Override
        public void evict(final BigDecimal oldest, final MathContext mc) {
            final BigDecimal rounded = oldest.round(mc);
            weightedSum = weightedSum.subtract(rounded);
            sum = sum.subtract(rounded);
            count--;
        }

        @Override
        public boolean isReady() {
            return count > 0;
        }

        @Override
        public BigDecimal value(final MathContext mc) {
            // The newest `count` weights of 1..n sum to count * (2n - count + 1) / 2
            final BigDecimal k = BigDecimal.valueOf(count);
            final BigDecimal weightTotal = k
                    .multiply(windowSize.multiply(BigDecimal.TWO).subtract(k).add(BigDecimal.ONE))
                    .divide(BigDecimal.TWO);
            return weightedSum.divide(weightTotal, mc);
        }
    }

    private final static class CountSum {
        private long count = 0;
        private BigDecimal sum = BigDecimal.ZERO;

        void add(final BigDecimal roundedValue) {
            count++;
            sum = sum.add(roundedValue);
        }

        void evict(final BigDecimal roundedValue) {
            count--;
            sum = sum.subtract(roundedValue);
        }

        long count() {
            return count;
        }

        BigDecimal sum() {
            return sum;
        }

        BigDecimal mean(final MathContext mc) {
            return sum.divide(BigDecimal.valueOf(count), mc);
        }
    }

    private final static class Extremes {
        private final ArrayDeque<BigDecimal> maximums = new ArrayDeque<>();
        private final ArrayDeque<BigDecimal> minimums = new ArrayDeque<>();
        private final boolean windowed;

        Extremes(final boolean windowed) {
            this.windowed = windowed;
        }

        void add(final BigDecimal roundedValue) {
            while (!minimums.isEmpty() && minimums.getLast().compareTo(roundedValue) > 0) {
                minimums.removeLast();
            }
            if (windowed || minimums.isEmpty()) {
                minimums.addLast(roundedValue);
            }
            while (!maximums.isEmpty() && maximums.getLast().compareTo(roundedValue) < 0) {
                maximums.removeLast();
            }
            if (windowed || maximums.isEmpty()) {
                maximums.addLast(roundedValue);
            }
        }

        void evict(final BigDecimal roundedValue) {
            if (minimums.getFirst().compareTo(roundedValue) == 0) {
                minimums.removeFirst();
            }
            if (maximums.getFirst().compareTo(roundedValue) == 0) {
                maximums.removeFirst();
            }
        }

        BigDecimal max() {
            return maximums.getFirst();
        }

        BigDecimal min() {
            return minimums.getFirst();
        }
    }

    private final static class Moments {
        private final CountSum countSum = new CountSum();
        private BigDecimal sumOfSquares = BigDecimal.ZERO;

        void add(final BigDecimal roundedValue) {
            countSum.add(roundedValue);
            sumOfSquares = sumOfSquares.add(roundedValue.multiply(roundedValue));
        }

        void evict(final BigDecimal roundedValue) {
            countSum.evict(roundedValue);
            sumOfSquares = sumOfSquares.subtract(roundedValue.multiply(roundedValue));
        }

        long count() {
            return countSum.count();
        }

        BigDecimal mean(final MathContext mc) {
            return countSum.mean(mc);
        }

        BigDecimal sum() {
            return countSum.sum();
        }

        BigDecimal populationVariance(final MathContext mc) {
            final BigDecimal n = BigDecimal.valueOf(count());
            final BigDecimal numerator = sumOfSquares.multiply(n).subtract(sum().multiply(sum()));
            return numerator.divide(n.multiply(n), mc);
        }

        BigDecimal sampleVariance(final MathContext mc) {
            final BigDecimal n = BigDecimal.valueOf(count());
            final BigDecimal numerator = sumOfSquares.multiply(n).subtract(sum().multiply(sum()));
            return numerator.divide(n.multiply(n.subtract(BigDecimal.ONE)), mc);
        }

        BigDecimal populationStandardDeviation(final MathContext mc) {
            return populationVariance(mc).sqrt(mc);
        }

        BigDecimal sampleStandardDeviation(final MathContext mc) {
            return sampleVariance(mc).sqrt(mc);
        }
    }

    private static class Product {
        // Holds the window's values in a moving window only, so a running product keeps constant memory.
        private final ArrayDeque<BigDecimal> window = new ArrayDeque<>();
        private final boolean moving;
        private long count = 0;
        private long zeroCount = 0;
        private BigDecimal product = BigDecimal.ONE;

        Product(final boolean moving) {
            this.moving = moving;
        }

        void add(final BigDecimal value, final MathContext mathContext) {
            count++;
            if (moving) {
                window.addLast(value);
            }
            if (value.compareTo(BigDecimal.ZERO) == 0) {
                zeroCount++;
            } else {
                product = product.multiply(value, mathContext);
            }
        }

        long count() {
            return count;
        }

        // Dividing the departing value out would round, and that error would build up over the whole stream.
        // Rebuilding the product from the values still in the window keeps the error bounded by the window size.
        void evict(final BigDecimal value, final MathContext mathContext) {
            count--;
            window.removeFirst();
            if (value.compareTo(BigDecimal.ZERO) == 0) {
                zeroCount--;
            }
            product = BigDecimal.ONE;
            for (final BigDecimal remaining : window) {
                if (remaining.compareTo(BigDecimal.ZERO) != 0) {
                    product = product.multiply(remaining, mathContext);
                }
            }
        }

        BigDecimal product() {
            return zeroCount > 0 ? BigDecimal.ZERO : product;
        }
    }

    private static final class SortedWindow {
        private final List<BigDecimal> sorted = new ArrayList<>();

        void add(final BigDecimal value) {
            sorted.add(insertionPoint(value), value);
        }

        void evict(final BigDecimal value) {
            final int index = Collections.binarySearch(sorted, value);
            sorted.remove(index);
        }

        int size() {
            return sorted.size();
        }

        // R-7 / PERCENTILE.INC, p in [0, 100]
        BigDecimal percentile(final BigDecimal p, final MathContext mc) {
            final BigDecimal rank = BigDecimal.valueOf(sorted.size() - 1).multiply(p).movePointLeft(2);
            final BigDecimal lowerRank = rank.setScale(0, RoundingMode.FLOOR);
            final BigDecimal weight = rank.subtract(lowerRank).stripTrailingZeros();
            final BigDecimal low = sorted.get(lowerRank.intValueExact());
            if (weight.signum() == 0) {
                return low;
            }
            final BigDecimal high = sorted.get(lowerRank.intValueExact() + 1);
            final BigDecimal interpolated = low.add(high.subtract(low).multiply(weight)).round(mc);
            // Multiplying by the weight adds trailing zeros (1.0 + 1.0 * 0.5 = 1.50) that say nothing about the
            // data. Drop them, but keep at least as many decimal places as the two values had (1.10, 1.10 -> 1.10).
            // Only zeros are removed, so the value is unchanged and still fits the MathContext.
            final int dataScale = Math.max(low.scale(), high.scale());
            final int strippedScale = interpolated.stripTrailingZeros().scale();
            final int scale = Math.min(interpolated.scale(), Math.max(strippedScale, dataScale));
            return interpolated.setScale(scale, RoundingMode.UNNECESSARY);
        }

        private int insertionPoint(final BigDecimal value) {
            final int i = Collections.binarySearch(sorted, value);
            return i >= 0 ? i : -(i + 1);
        }
    }
}
