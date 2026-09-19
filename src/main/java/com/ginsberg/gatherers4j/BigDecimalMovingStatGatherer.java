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

import com.ginsberg.gatherers4j.util.CircularBuffer;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.ginsberg.gatherers4j.util.GathererUtils.mustNotBeNull;

public final class BigDecimalMovingStatGatherer<INPUT extends @Nullable Object, OUTPUT extends @Nullable Object>
        extends AbstractBigDecimalStatGatherer<INPUT, OUTPUT> {

    private final int windowSize;
    private boolean includePartialValues = true;

    BigDecimalMovingStatGatherer(
            final int windowSize,
            final Function<INPUT, @Nullable BigDecimal> mappingFunction,
            final Supplier<StatisticAccumulator<OUTPUT>> statisticAccumulatorSupplier
    ) {
        if (windowSize <= 1) {
            throw new IllegalArgumentException("Window size must be greater than 1");
        }
        this.windowSize = windowSize;
        super(mappingFunction, statisticAccumulatorSupplier);
    }

    @Override
    public Supplier<StatGathererState<OUTPUT>> initializer() {
        return () -> StatGathererState.ofMoving(accumulatorSupplier.get(), new CircularBuffer<>(windowSize));
    }

    @Override
    public Integrator<StatGathererState<OUTPUT>, INPUT, OUTPUT> integrator() {
        return Integrator.ofGreedy((state, element, downstream) -> {
            final BigDecimal mappedElement = getMappedElement(element);
            if (mappedElement != null && state.window() != null) {
                final BigDecimal evicted = state.window().add(mappedElement);
                if (evicted != null) {
                    state.accumulator().evict(evicted, mathContext);
                }
                state.accumulator().add(mappedElement, mathContext);

                if (state.accumulator().isReady() && (includePartialValues || state.window().size() == windowSize)) {
                    return downstream.push(state.accumulator().value(mathContext));
                }
            }
            return !downstream.isRejecting();
        });
    }

    /// When encountering a `null` value in a stream, treat it as the given `replacement` value instead.
    ///
    /// @param replacement The value to replace `null` with
    public BigDecimalMovingStatGatherer<INPUT, OUTPUT> treatNullAs(@Nullable final BigDecimal replacement) {
        this.nullReplacement = replacement;
        return this;
    }

    /// When encountering a `null` value in a stream, treat it as `BigDecimal.ZERO` instead.
    public BigDecimalMovingStatGatherer<INPUT, OUTPUT> treatNullAsZero() {
        return treatNullAs(BigDecimal.ZERO);
    }

    /// When encountering a `null` value in a stream, treat it as `BigDecimal.ONE` instead.
    public BigDecimalMovingStatGatherer<INPUT, OUTPUT> treatNullAsOne() {
        return treatNullAs(BigDecimal.ONE);
    }

    /// Replace the `MathContext` used for all mathematical operations in this gatherer.
    ///
    /// @param mathContext A non-null `MathContext`
    public BigDecimalMovingStatGatherer<INPUT, OUTPUT> withMathContext(final MathContext mathContext) {
        this.mathContext = mustNotBeNull(mathContext, "MathContext must not be null");
        return this;
    }

    /// When creating a moving window and the full size of the window has not yet been reached, do
    /// not emit partially calculated values to the downstream.
    ///
    /// For example, if the trailing calculation is over 10 values, but the upstream has only emitted two
    /// values, this gatherer should not emit any partially calculated values. The default is for
    /// partially calculated values to be emitted.
    public BigDecimalMovingStatGatherer<INPUT, OUTPUT> excludePartialValues() {
        this.includePartialValues = false;
        return this;
    }

}
