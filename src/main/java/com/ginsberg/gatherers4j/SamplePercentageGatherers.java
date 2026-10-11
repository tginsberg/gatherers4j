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

import java.util.function.Supplier;
import java.util.random.RandomGenerator;
import java.util.stream.Gatherer;

public class SamplePercentageGatherers {

    // The supplier is called once per stream, so a generator is never shared between runs unless the supplier shares it
    static <INPUT extends @Nullable Object> Gatherer<INPUT, ?, INPUT> bernoulli(
            final double percentage,
            final Supplier<RandomGenerator> randomGeneratorSupplier
    ) {
        if (!(percentage > 0.0 && percentage <= 1.0)) {
            throw new IllegalArgumentException("percentage must be greater than 0 and 1.0 or less");
        }
        return Gatherer.ofSequential(
                randomGeneratorSupplier,
                Gatherer.Integrator.ofGreedy((randomGenerator, element, downstream) -> {
                    if (randomGenerator.nextDouble() < percentage) {
                        return downstream.push(element);
                    }
                    return !downstream.isRejecting();
                })
        );
    }
}
