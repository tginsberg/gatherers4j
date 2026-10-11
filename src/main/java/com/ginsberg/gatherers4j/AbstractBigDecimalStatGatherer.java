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
import java.util.stream.Gatherer;

import static com.ginsberg.gatherers4j.util.GathererUtils.mustNotBeNull;

abstract class AbstractBigDecimalStatGatherer<INPUT extends @Nullable Object, OUTPUT extends @Nullable Object>
        implements Gatherer<INPUT, StatGathererState<OUTPUT>, OUTPUT> {

    private final Function<INPUT, @Nullable BigDecimal> mappingFunction;
    protected final Supplier<StatisticAccumulator<OUTPUT>> accumulatorSupplier;
    protected MathContext mathContext = MathContext.DECIMAL64;
    protected @Nullable BigDecimal nullReplacement;

    AbstractBigDecimalStatGatherer(
            final Function<INPUT, @Nullable BigDecimal> mappingFunction,
            final Supplier<StatisticAccumulator<OUTPUT>> accumulatorSupplier
    ) {
        this.mappingFunction = mustNotBeNull(mappingFunction, "Mapping function must not be null");
        this.accumulatorSupplier = accumulatorSupplier;
    }

    // Every divide and square root in the engine needs a finite precision; UNLIMITED (precision 0) would throw
    // ArithmeticException mid-stream for ordinary inputs such as the mean of 1, 1, and 2.
    protected static MathContext validMathContext(final @Nullable MathContext mathContext) {
        final MathContext checked = mustNotBeNull(mathContext, "MathContext must not be null");
        if (checked.getPrecision() <= 0) {
            throw new IllegalArgumentException("MathContext precision must be greater than 0");
        }
        return checked;
    }

    protected @Nullable BigDecimal getMappedElement(final INPUT element) {
        if(element == null) {
            return nullReplacement;
        }
        final @Nullable BigDecimal mapped = mappingFunction.apply(element);
        return mapped == null ? nullReplacement : mapped;
    }

    /// Include the original input value from the stream in addition to the calculated value.
    public WithOriginalGatherer<INPUT, ?, OUTPUT> withOriginal() {
        return new WithOriginalGatherer<>(this);
    }
}

