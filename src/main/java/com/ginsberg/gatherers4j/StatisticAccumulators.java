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

import com.ginsberg.gatherers4j.enums.StandardDeviation;
import com.ginsberg.gatherers4j.util.MathUtils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.ginsberg.gatherers4j.util.GathererUtils.mustNotBeNull;

class StatisticAccumulators {

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
                ema = value.multiply(alpha).add(ema.multiply(oneMinusAlpha));
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
        final Product state = new Product();

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
        private final SortedWindow state = new SortedWindow();
        private final double percentile;

        PercentileAccumulator(final double percentile) {
            if (percentile < 0 || percentile > 100) {
                throw new IllegalArgumentException("Percentile must be between 0 and 100, inclusive");
            }
            this.percentile = percentile;
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
        private final Product state = new Product();

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

    final static class StdDevAccumulator implements StatisticAccumulator<BigDecimal> {
        private final Moments state = new Moments();
        private final StandardDeviation mode;

        StdDevAccumulator(final StandardDeviation mode) {
            this.mode = mustNotBeNull(mode, "Must specify a mode for Standard Deviation");
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
            return mode == StandardDeviation.Population
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
            return state.sum();
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

    private final static class Moments {
        private final CountSum countSum = new CountSum();
        private BigDecimal sumOfSquares = BigDecimal.ZERO;

        void add(final BigDecimal roundedValue, final MathContext mc) {
            countSum.add(roundedValue);
            sumOfSquares = sumOfSquares.add(roundedValue.multiply(roundedValue, mc));
        }

        void evict(final BigDecimal roundedValue, final MathContext mc) {
            countSum.evict(roundedValue);
            sumOfSquares = sumOfSquares.subtract(roundedValue.multiply(roundedValue, mc));
        }

        long count() {
            return countSum.count();
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
            if (count() <= 1) {
                return BigDecimal.ZERO;
            }
            final BigDecimal n = BigDecimal.valueOf(count());
            final BigDecimal numerator = sumOfSquares.multiply(n).subtract(sum().multiply(sum()));
            return numerator.divide(n.multiply(n.subtract(BigDecimal.ONE)), mc);
        }

        BigDecimal populationStandardDeviation(final MathContext mc) {
            return populationVariance(mc).sqrt(mc);
        }

        BigDecimal sampleStandardDeviation(final MathContext mc) {
            return count() <= 1 ? BigDecimal.ZERO : sampleVariance(mc).sqrt(mc);
        }
    }

    private static class Product {
        private long count = 0;
        private long zeroCount = 0;
        private BigDecimal product = BigDecimal.ONE;

        void add(final BigDecimal value, final MathContext mathContext) {
            count++;
            if (value.compareTo(BigDecimal.ZERO) == 0) {
                zeroCount++;
            } else {
                product = product.multiply(value, mathContext);
            }
        }

        long count() {
            return count;
        }

        void evict(final BigDecimal value, final MathContext mathContext) {
            count--;
            if (value.compareTo(BigDecimal.ZERO) == 0) {
                zeroCount--;
            } else {
                product = product.divide(value, mathContext);
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
        BigDecimal percentile(final double p, final MathContext mc) {
            final int n = sorted.size();
            final double h = (n - 1) * (p / 100.0);
            final int lower = (int) Math.floor(h);
            final int upper = (int) Math.ceil(h);

            if (lower == upper) {
                return sorted.get(lower);
            }
            final BigDecimal weight = BigDecimal.valueOf(h - lower);
            final BigDecimal low = sorted.get(lower);
            final BigDecimal high = sorted.get(upper);
            return low.add(high.subtract(low).multiply(weight, mc), mc);
        }

        private int insertionPoint(final BigDecimal value) {
            final int i = Collections.binarySearch(sorted, value);
            return i >= 0 ? i : -(i + 1);
        }
    }
}
