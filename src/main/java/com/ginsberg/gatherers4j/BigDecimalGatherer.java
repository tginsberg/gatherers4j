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
import java.util.function.Function;
import java.util.stream.Gatherer;

import static com.ginsberg.gatherers4j.GathererUtils.mustNotBeNull;

abstract public class BigDecimalGatherer<INPUT extends @Nullable Object>
        implements Gatherer<INPUT, BigDecimalGatherer.State, BigDecimal> {
    private final Function<INPUT, @Nullable BigDecimal> mappingFunction;
    private MathContext mathContext = MathContext.DECIMAL64;
    private @Nullable BigDecimal nullReplacement;

    BigDecimalGatherer(final Function<INPUT, @Nullable BigDecimal> mappingFunction) {
        mustNotBeNull(mappingFunction, "Mapping function must not be null");
        this.mappingFunction = mappingFunction;
    }

    @Override
    public Integrator<BigDecimalGatherer.State, INPUT, BigDecimal> integrator() {
        return Integrator.ofGreedy((state, element, downstream) -> {
            final BigDecimal mappedElement = element == null ? nullReplacement : mappingFunction.apply(element);
            if (mappedElement != null) {
                state.add(mappedElement, mathContext);
                if (state.canCalculate()) {
                    return downstream.push(state.calculate());
                }
            }
            return !downstream.isRejecting();
        });
    }

    /// When encountering a `null` value in a stream, treat it as `BigDecimal.ZERO` instead.
    public BigDecimalGatherer<INPUT> treatNullAsZero() {
        return treatNullAs(BigDecimal.ZERO);
    }

    /// When encountering a `null` value in a stream, treat it as the given `replacement` value instead.
    ///
    /// @param replacement The value to replace `null` with
    public BigDecimalGatherer<INPUT> treatNullAs(final @Nullable BigDecimal replacement) {
        this.nullReplacement = replacement;
        return this;
    }

    /// Replace the `MathContext` used for all mathematical operations in this class.
    ///
    /// @param mathContext A non-null `MathContext`
    public BigDecimalGatherer<INPUT> withMathContext(final MathContext mathContext) {
        mustNotBeNull(mathContext, "MathContext must not be null");
        this.mathContext = mathContext;
        return this;
    }

    /// Include the original input value from the stream in addition to the calculated average.
    public WithOriginalGatherer<INPUT, BigDecimalGatherer.State, BigDecimal> withOriginal() {
        return new WithOriginalGatherer<>(this);
    }

    public interface State {
        void add(final BigDecimal element, final MathContext mathContext);

        default boolean canCalculate() {
            return true;
        }

        BigDecimal calculate();
    }

}