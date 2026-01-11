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

import com.ginsberg.gatherers4j.util.MathUtils;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.function.Function;
import java.util.function.Supplier;

public final class BigDecimalGeometricMeanGatherer<INPUT extends @Nullable Object> extends BigDecimalGatherer<INPUT> {

    BigDecimalGeometricMeanGatherer(final Function<INPUT, @Nullable BigDecimal> mappingFunction) {
        super(mappingFunction);
    }

    @Override
    public Supplier<BigDecimalGatherer.State> initializer() {
        return State::new;
    }

    /// When encountering a `null` value in a stream, treat it as `BigDecimal.ONE` instead.
    public BigDecimalGatherer<INPUT> treatNullAsOne() {
        return treatNullAs(BigDecimal.ONE);
    }

    static class State implements BigDecimalGatherer.State {
        long count;
        BigDecimal product = BigDecimal.ONE;
        MathContext mathContext = MathContext.DECIMAL64;

        @Override
        public void add(final BigDecimal element, final MathContext mathContext) {
            this.mathContext = mathContext;
            count++;
            product = product.multiply(element, mathContext);
        }

        @Override
        public BigDecimal calculate() {
            if (count == 0) {
                return BigDecimal.ZERO;
            }
            return MathUtils.nthRoot(product, count, mathContext);
        }
    }
}
