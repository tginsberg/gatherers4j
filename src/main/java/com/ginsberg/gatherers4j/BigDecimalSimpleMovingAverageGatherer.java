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

public final class BigDecimalSimpleMovingAverageGatherer<INPUT extends @Nullable Object> extends BigDecimalGatherer<INPUT> {

    private final int windowSize;
    private boolean includePartialValues;

    BigDecimalSimpleMovingAverageGatherer(
            final Function<INPUT, @Nullable BigDecimal> mappingFunction,
            final int windowSize
    ) {
        super(mappingFunction);
        if (windowSize <= 1) {
            throw new IllegalArgumentException("Window size must be greater than 1");
        }
        this.windowSize = windowSize;
    }

    @Override
    public Supplier<BigDecimalGatherer.State> initializer() {
        return () -> new State(windowSize, includePartialValues);
    }

    /// When creating a moving average and the full size of the window has not yet been reached, the
    /// gatherer should emit averages for what it has.
    ///
    /// For example, if the trailing average is over 10 values, but the stream has only emitted two
    /// values, the gatherer should calculate the two values and emit the answer. The default is to not
    /// emit anything until the full size of the window has been seen.
    public BigDecimalSimpleMovingAverageGatherer<INPUT> includePartialValues() {
        includePartialValues = true;
        return this;
    }

    static class State implements BigDecimalGatherer.State {
        final boolean includePartialValues;
        final BigDecimal[] series;
        BigDecimal sum = BigDecimal.ZERO;
        BigDecimal count = BigDecimal.ZERO;
        BigDecimal average = BigDecimal.ZERO;
        int index = 0;

        private State(final int lookBack, final boolean includePartialValues) {
            this.includePartialValues = includePartialValues;
            this.series = new BigDecimal[lookBack];
            Arrays.fill(series, BigDecimal.ZERO);
        }

        @Override
        public boolean canCalculate() {
            return includePartialValues || count.intValue() >= series.length;
        }

        @Override
        public void add(final BigDecimal element, final MathContext mathContext) {
            sum = sum.subtract(series[index]).add(element, mathContext);
            series[index % series.length] = element;
            index = (index + 1) % series.length;
            if (count.intValue() < series.length) {
                count = count.add(BigDecimal.ONE);
            }
            average = sum.divide(count, mathContext);
        }

        @Override
        public BigDecimal calculate() {
            return average;
        }
    }
}
