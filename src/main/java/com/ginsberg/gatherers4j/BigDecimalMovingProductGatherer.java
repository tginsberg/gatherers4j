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

public class BigDecimalMovingProductGatherer<INPUT extends @Nullable Object>
        extends BigDecimalGatherer<INPUT> {

    private final int windowSize;
    private boolean includePartialValues = false;

    BigDecimalMovingProductGatherer(
            final Function<INPUT, @Nullable BigDecimal> mappingFunction,
            final int windowSize) {
        super(mappingFunction);
        if (windowSize <= 0) {
            throw new IllegalArgumentException("Window size must be positive");
        }
        this.windowSize = windowSize;
    }

    @Override
    public Supplier<BigDecimalGatherer.State> initializer() {
        return () -> new BigDecimalMovingProductGatherer.State(windowSize, includePartialValues);
    }

    /// When creating a moving product and the full size of the window has not yet been reached, the
    /// gatherer should emit the product for what it has.
    ///
    /// For example, if the trailing product is over 10 values, but the stream has only emitted two
    /// values, the gatherer should calculate the two values and emit the answer. The default is to not
    /// emit anything until the full size of the window has been seen.
    public BigDecimalMovingProductGatherer<INPUT> includePartialValues() {
        includePartialValues = true;
        return this;
    }

    /// When encountering a `null` value in a stream, treat it as `BigDecimal.ONE` instead.
    public BigDecimalGatherer<INPUT> treatNullAsOne() {
        return treatNullAs(BigDecimal.ONE);
    }

    static class State implements BigDecimalGatherer.State {
        final boolean includePartialValues;
        final BigDecimal[] series;
        BigDecimal product = BigDecimal.ONE;
        int index = 0;

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
            product = product.divide(series[index % series.length], mathContext).multiply(element, mathContext);
            series[index % series.length] = element;
            index++;
        }

        @Override
        public BigDecimal calculate() {
            return product;
        }
    }
}
