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

import java.util.stream.Gatherer;

import static com.ginsberg.gatherers4j.util.GathererUtils.mustNotBeNull;

abstract class TypeFilteringGatherer {

    @SafeVarargs
    public static <INPUT, OUTPUT> Gatherer<INPUT, ?, OUTPUT> of(final Class<? extends OUTPUT>... validTypes) {
        mustNotBeNull(validTypes, "validTypes must not be null");
        if (validTypes.length == 0) {
            throw new IllegalArgumentException("Must provide at least one type");
        }

        return Gatherer.of((_, element, downstream) -> {
            for (final var type : validTypes) {
                if (type.isInstance(element)) {
                    return downstream.push(type.cast(element));
                }
            }
            return !downstream.isRejecting();
        });
    }
}
