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
import java.util.function.Supplier;

import static com.ginsberg.gatherers4j.util.GathererUtils.mustNotBeNull;

public final class BigDecimalSimpleAverageGatherer<INPUT extends @Nullable Object> extends BigDecimalGatherer<INPUT> {

    BigDecimalSimpleAverageGatherer(final Function<INPUT, @Nullable BigDecimal> mappingFunction) {
        mustNotBeNull(mappingFunction, "Mapping function must not be null");
        super(mappingFunction);
    }

    @Override
    public Supplier<BigDecimalGatherer.State> initializer() {
        return State::new;
    }

    static class State implements BigDecimalGatherer.State {
        long count;
        BigDecimal average = BigDecimal.ZERO;

        @Override
        public void add(final BigDecimal element, final MathContext mathContext) {
            count++;
            average = average.add(element.subtract(average).divide(BigDecimal.valueOf(count), mathContext));
        }

        @Override
        public BigDecimal calculate() {
            return average;
        }
    }
}
