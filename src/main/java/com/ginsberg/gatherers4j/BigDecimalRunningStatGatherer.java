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

import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.ginsberg.gatherers4j.util.GathererUtils.mustNotBeNull;

public final class BigDecimalRunningStatGatherer<INPUT extends @Nullable Object, OUTPUT extends @Nullable Object>
        extends AbstractBigDecimalStatGatherer<INPUT, OUTPUT> {

    BigDecimalRunningStatGatherer
            (final Function<INPUT, @Nullable BigDecimal> mappingFunction,
             final Supplier<StatisticAccumulator<OUTPUT>> statisticAccumulatorSupplier
            ) {
        super(mappingFunction, statisticAccumulatorSupplier);
    }

    @Override
    public Supplier<StatGathererState<OUTPUT>> initializer() {
        return () -> StatGathererState.ofRunning(accumulatorSupplier.get());
    }

    @Override
    public Integrator<StatGathererState<OUTPUT>, INPUT, OUTPUT> integrator() {
        return Integrator.ofGreedy((state, element, downstream) -> {
            final BigDecimal mappedElement = getMappedElement(element);
            if (mappedElement != null) {
                state.accumulator().add(mappedElement, mathContext);
                if (state.accumulator().isReady()) {
                    return downstream.push(state.accumulator().value(mathContext));
                }
            }
            return !downstream.isRejecting();
        });
    }

    /// When encountering a `null` value in a stream, treat it as the given `replacement` value instead.
    ///
    /// @param replacement The value to replace `null` with
    public BigDecimalRunningStatGatherer<INPUT, OUTPUT> treatNullAs(@Nullable final BigDecimal replacement) {
        this.nullReplacement = replacement;
        return this;
    }

    /// When encountering a `null` value in a stream, treat it as `BigDecimal.ZERO` instead.
    public BigDecimalRunningStatGatherer<INPUT, OUTPUT> treatNullAsZero() {
        return treatNullAs(BigDecimal.ZERO);
    }

    /// When encountering a `null` value in a stream, treat it as `BigDecimal.ONE` instead.
    public BigDecimalRunningStatGatherer<INPUT, OUTPUT> treatNullAsOne()  {
        return treatNullAs(BigDecimal.ONE);
    }

    /// Replace the `MathContext` used for all mathematical operations in this gatherer.
    ///
    /// @param mathContext A non-null `MathContext`
    public BigDecimalRunningStatGatherer<INPUT, OUTPUT> withMathContext(final MathContext mathContext) {
        this.mathContext = mustNotBeNull(mathContext, "MathContext must not be null");
        return this;
    }

}
