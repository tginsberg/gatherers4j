/*
 * Copyright 2025 Todd Ginsberg
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class BigDecimalMedianGatherer<INPUT extends @Nullable Object>
        extends BigDecimalGatherer<INPUT> {

    private final int windowSize;
    private boolean includePartialValues = false;

    BigDecimalMedianGatherer(
            final int windowSize,
            final Function<INPUT, @Nullable BigDecimal> mappingFunction
    ) {
        super(mappingFunction);
        this.windowSize = windowSize;
    }

    @Override
    public Supplier<BigDecimalGatherer.State> initializer() {
        return () -> windowSize > 0 ? new WindowedState(windowSize, includePartialValues) : new RunningState();
    }

    /// When creating a moving median and the full size of the window has not yet been reached, the
    /// gatherer should emit the median for what it has.
    ///
    /// For example, if the trailing median is over 10 values, but the stream has only emitted two
    /// values, the gatherer should calculate the median with two values and emit the answer. The default is to not
    /// emit anything until the full size of the window has been seen.
    public BigDecimalMedianGatherer<INPUT> includePartialValues() {
        includePartialValues = true;
        return this;
    }

    static class RunningState implements BigDecimalGatherer.State {
        final List<BigDecimal> series = new ArrayList<>();
        BigDecimal median = BigDecimal.ZERO;

        @Override
        public void add(final BigDecimal element, final MathContext mathContext) {
            addNewElement(element);
            final var seriesCopy = new ArrayList<>(series);
            Collections.sort(seriesCopy);
            final var midPoint = (seriesCopy.size() / 2);

            if (seriesCopy.size() % 2 == 0) {
                median = seriesCopy.get(midPoint - 1).add(seriesCopy.get(midPoint)).divide(BigDecimal.TWO, mathContext);
            } else {
                median = seriesCopy.get(midPoint);
            }
        }

        void addNewElement(final BigDecimal element) {
            series.addLast(element);
        }

        @Override
        public BigDecimal calculate() {
            return median;
        }
    }

    static class WindowedState extends RunningState {
        final int lookBack;
        final boolean includePartialValues;

        private WindowedState(final int lookBack, final boolean includePartialValues) {
            this.lookBack = lookBack;
            this.includePartialValues = includePartialValues;
        }

        @Override
        public boolean canCalculate() {
            return includePartialValues || series.size() == lookBack;
        }

        @Override
        void addNewElement(final BigDecimal element) {
            if (series.size() == lookBack) {
                series.removeFirst();
            }
            series.addLast(element);
        }
    }
}
