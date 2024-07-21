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
    private int windowSize = 1;
    private boolean includePartialValues;

    AveragingBigDecimalGatherer(final Function<INPUT, BigDecimal> mappingFunction) {
        super();
        this.mappingFunction = mappingFunction;
    }

    @Override
    public Supplier<State> initializer() {
        return windowSize == 1 ? State::new : () -> new TrailingState(windowSize);
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

    /**
     * Construct a moving average, with a window of size <code>window</code>.
     *
     * @param window The size of the window to average values over, must be a positive number.
     */
    public AveragingBigDecimalGatherer<INPUT> simpleMovingAverage(int window) {
        if (window <= 0) {
            throw new IllegalArgumentException("Moving window size must be positive");
        }
        windowSize = window;
        return this;
    }

    /**
     * When creating a moving average and the full size of the window has not yet been reached, the
     * gatherer should emit averages for what it has.
     * For example, if the trailing average is over 10 values, but the stream has only emitted two
     * values, the gatherer should average the two values and emit the answer. The default is to not
     * emit anything until the full size of the window has been seen.
     */
    public AveragingBigDecimalGatherer<INPUT> includePartialValues() {
        includePartialValues = true;
        return this;
    }

    /**
     * When encountering a <code>null</code> value in a stream, treat it as `BigDecimal.ZERO` instead.
     */
    public AveragingBigDecimalGatherer<INPUT> treatNullAsZero() {
        return treatNullAs(BigDecimal.ZERO);
    }

    /**
     * When encountering a <code>null</code> value in a stream, treat it as the given `rule` value instead.
     *
     * @param rule The value to replace null with
     */
    public AveragingBigDecimalGatherer<INPUT> treatNullAs(final BigDecimal rule) {
        this.nullReplacement = rule;
        return this;
    }

    /**
     * Replace the <code>MathContext</code> used for all mathematical operations in this class.
     *
     * @param mathContext A non-null <code>MathContext</code>
     */
    public AveragingBigDecimalGatherer<INPUT> withMathContext(final MathContext mathContext) {
        mustNotBeNull(mathContext, "MathContext must not be null");
        this.mathContext = mathContext;
        return this;
    }

    /**
     * Replace the <code>RoundingMode</code> used for all mathematical operations in this class.
     *
     * @param roundingMode A non-null <code>RoundingMode</code>
     */
    public AveragingBigDecimalGatherer<INPUT> withRoundingMode(final RoundingMode roundingMode) {
        mustNotBeNull(roundingMode, "RoundingMode must not be null");
        this.roundingMode = roundingMode;
        return this;
    }

    /**
     * Include the original input value from the stream in addition to the calculated average.
     */
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
