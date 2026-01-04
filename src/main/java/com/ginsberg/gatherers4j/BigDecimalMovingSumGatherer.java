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

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;

public class BigDecimalMovingSumGatherer<INPUT extends @Nullable Object>
        extends BigDecimalGatherer<INPUT> {

    private final int windowSize;
        private boolean includePartialValues = true;

    BigDecimalMovingSumGatherer(
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
        return () -> new BigDecimalMovingSumGatherer.State(windowSize, includePartialValues);
    }

    /// When creating a moving sum and the full size of the window has not yet been reached, do
    /// not emit partially calculated values to the downstream.
    ///
    /// For example, if the trailing sum is over 10 values, but the upstream has only emitted two
    /// values, this gatherer should not emit any partially calculated values. The default is for
    /// partially calculated values to be emitted.
    public BigDecimalMovingSumGatherer<INPUT> excludePartialValues() {
        includePartialValues = false;
        return this;
    }

    static class State implements BigDecimalGatherer.State {
        final boolean includePartialValues;
        final BigDecimal[] series;
        BigDecimal sum = BigDecimal.ZERO;
        int index = 0;

        private State(final int lookBack, final boolean includePartialValues) {
            this.includePartialValues = includePartialValues;
            this.series = new BigDecimal[lookBack];
            Arrays.fill(series, BigDecimal.ZERO);
        }

        @Override
        public boolean canCalculate() {
            return includePartialValues || index >= series.length;
        }

        @Override
        public void add(final BigDecimal element, final MathContext mathContext) {
            sum = sum.subtract(series[index % series.length]).add(element, mathContext);
            series[index % series.length] = element;
            index++;
        }

        @Override
        public BigDecimal calculate() {
            return sum;
        }
    }
}
