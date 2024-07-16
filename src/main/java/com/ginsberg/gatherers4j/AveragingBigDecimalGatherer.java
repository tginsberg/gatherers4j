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

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Gatherer;

import static com.ginsberg.gatherers4j.GathererUtils.mustNotBeNull;

public class AveragingBigDecimalGatherer<INPUT>
        implements Gatherer<INPUT, AveragingBigDecimalGatherer.State, BigDecimal> {

    private final Function<INPUT, BigDecimal> mappingFunction;
    private RoundingMode roundingMode = RoundingMode.HALF_UP;
    private MathContext mathContext = MathContext.DECIMAL64;
    private BigDecimal nullReplacement;
    private int trailingCount = 1;
    private boolean includePartialValues;

    AveragingBigDecimalGatherer(final Function<INPUT, BigDecimal> mappingFunction) {
        super();
        this.mappingFunction = mappingFunction;
    }

    @Override
    public Supplier<State> initializer() {
        return trailingCount == 1 ? State::new : () -> new TrailingState(trailingCount);
    }

    @Override
    public Integrator<AveragingBigDecimalGatherer.State, INPUT, BigDecimal> integrator() {
        return (state, element, downstream) -> {
            final BigDecimal mappedElement = element == null ? nullReplacement : mappingFunction.apply(element);
            if (mappedElement != null) {
                state.add(mappedElement, mathContext);
                if (state.canCalculate(includePartialValues)) {
                    return downstream.push(state.average(roundingMode, mathContext.getPrecision()));
                }
            }
            return !downstream.isRejecting();
        };
    }

    public AveragingBigDecimalGatherer<INPUT> trailing(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Trailing count must be positive");
        }
        trailingCount = count;
        return this;
    }

    public AveragingBigDecimalGatherer<INPUT> includePartialTailingValues() {
        includePartialValues = true;
        return this;
    }

    public AveragingBigDecimalGatherer<INPUT> treatNullAsZero() {
        return treatNullAs(BigDecimal.ZERO);
    }

    public AveragingBigDecimalGatherer<INPUT> treatNullAs(final BigDecimal rule) {
        this.nullReplacement = rule;
        return this;
    }

    public AveragingBigDecimalGatherer<INPUT> withMathContext(final MathContext mathContext) {
        mustNotBeNull(mathContext, "MathContext must not be null");
        this.mathContext = mathContext;
        return this;
    }

    public AveragingBigDecimalGatherer<INPUT> withRoundingMode(final RoundingMode roundingMode) {
        mustNotBeNull(roundingMode, "RoundingMode must not be null");
        this.roundingMode = roundingMode;
        return this;
    }

    public WithOriginalGatherer<INPUT, State, BigDecimal> withOriginal() {
        return new WithOriginalGatherer<>(this);
    }

    public static class State {
        long count;
        BigDecimal sum = BigDecimal.ZERO;

        void add(final BigDecimal element, final MathContext mathContext) {
            count++;
            sum = sum.add(element, mathContext);
        }

        boolean canCalculate(final boolean allowPartial) {
            return true;
        }

        BigDecimal average(final RoundingMode roundingMode, int precision) {
            if (sum.equals(BigDecimal.ZERO)) {
                return BigDecimal.ZERO;
            } else {
                return sum.divide(BigDecimal.valueOf(count), precision, roundingMode);
            }
        }
    }

    public static class TrailingState extends State {
        final BigDecimal[] series;
        int index = 0;

        private TrailingState(int lookBack) {
            this.series = new BigDecimal[lookBack];
            Arrays.fill(series, BigDecimal.ZERO);
        }

        @Override
        boolean canCalculate(final boolean allowPartial) {
            return allowPartial || count >= series.length;
        }

        @Override
        void add(final BigDecimal element, final MathContext mathContext) {
            sum = sum.subtract(series[index]).add(element, mathContext);
            series[index] = element;
            index = (index + 1) % series.length;
            if (count < series.length) {
                count++;
            }
        }
    }
}
