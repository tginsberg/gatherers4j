/*
 * Copyright 2024 Todd Ginsberg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ginsberg.gatherers4j;

import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.function.Function;

abstract class GathererUtils {

    final static long NANOS_PER_MILLISECOND = 1_000_000;

    static void mustNotBeNull(@Nullable final Object subject, final String message) {
        if (subject == null) {
            throw new IllegalArgumentException(message);
        }
    }

    static boolean safeEquals(@Nullable final Object left, @Nullable final Object right) {
        if (left == null && right == null) {
            return true;
        } else if (left == null || right == null) {
            return false;
        }
        return left.equals(right);
    }

    // Yes, I realize this is not to contract, but I only want it to measure equality in a narrow case
    // in which I only care about certain outputs.
    @SuppressWarnings("ComparatorMethodParameterNotUsed")
    static <T> Comparator<T> equalityOnlyComparator() {
        return (o1, o2) -> safeEquals(o1, o2) ? 0 : -1;
    }

    // Yes, I realize this is not to contract, but I only want it to measure equality in a narrow case
    // in which I only care about certain outputs.
    @SuppressWarnings("ComparatorMethodParameterNotUsed")
    static <T,R> Comparator<T> equalityOnlyComparator(final Function<T, R> mappingFunction) {
        return (o1, o2) -> safeEquals(mappingFunction.apply(o1), mappingFunction.apply(o2)) ? 0 : -1;
    }
}
