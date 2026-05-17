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

import com.ginsberg.gatherers4j.util.MathUtils;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A gatherer that calculates the moving geometric mean of BigDecimals.
 *
 * @param <INPUT> the type of the input elements
 */
public class BigDecimalMovingGeometricMeanGatherer<INPUT extends @Nullable Object>
        extends BigDecimalGatherer<INPUT> {

    private final int windowSize;
    private boolean includePartialValues = true;

    BigDecimalMovingGeometricMeanGatherer(
            final int windowSize,
            final Function<INPUT, @Nullable BigDecimal> mappingFunction
    ) {
        super(mappingFunction);
        if (windowSize <= 1) {
            throw new IllegalArgumentException("Window size must be greater than 1");
        }
        this.windowSize = windowSize;
    }

    @Override
    public Supplier<BigDecimalGatherer.State> initializer() {
        return () -> new BigDecimalMovingGeometricMeanGatherer.State(windowSize, includePartialValues);
    }

    /**
     * When creating a moving geometric mean and the full size of the window has not yet been reached, do
     * not emit partially calculated values to the downstream.
     *
     * @return this gatherer
     */
    public BigDecimalMovingGeometricMeanGatherer<INPUT> excludePartialValues() {
        includePartialValues = false;
        return this;
    }

    /**
     * When encountering a `null` value in a stream, treat it as `BigDecimal.ONE` instead.
     *
     * @return this gatherer
     */
    public BigDecimalGatherer<INPUT> treatNullAsOne() {
        return treatNullAs(BigDecimal.ONE);
    }

    static class State implements BigDecimalGatherer.State {
        final boolean includePartialValues;
        final BigDecimal[] series;
        BigDecimal product = BigDecimal.ONE;
        int index = 0;
        int zeroCount = 0;
        private BigDecimal nthRoot = BigDecimal.ZERO;

        private State(final int lookBack, final boolean includePartialValues) {
            this.includePartialValues = includePartialValues;
            this.series = new BigDecimal[lookBack];
            Arrays.fill(series, BigDecimal.ONE);
        }

        @Override
        public boolean canCalculate() {
            return includePartialValues || index >= series.length;
        }

        @Override
        public void add(final BigDecimal element, final MathContext mathContext) {
            final int windowIndex = index % series.length;
            final BigDecimal outgoing = series[windowIndex];

            if (outgoing.compareTo(BigDecimal.ZERO) == 0) {
                zeroCount--;
            } else {
                product = product.divide(outgoing, mathContext);
            }

            if (element.compareTo(BigDecimal.ZERO) == 0) {
                zeroCount++;
            } else {
                product = product.multiply(element, mathContext);
            }

            series[windowIndex] = element;
            final int count = Math.min(++index, series.length);
            if (count == 0 || zeroCount > 0) {
                nthRoot = BigDecimal.ZERO;
            } else {
                nthRoot = MathUtils.nthRoot(product, count, mathContext);
            }
        }

        @Override
        public BigDecimal calculate() {
            return nthRoot;
        }
    }
}
