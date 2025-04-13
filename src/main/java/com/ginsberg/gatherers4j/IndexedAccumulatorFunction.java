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

import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface IndexedAccumulatorFunction<
        A extends @Nullable Object,
        B extends @Nullable Object,
        R extends @Nullable Object> {

    ///  Applies this function to the given arguments
    ///
    /// @param index the index of the function invocation
    /// @param carry the accumulated value
    /// @param next  the next value to accumulate
    /// @return the function result
    R apply(int index, @Nullable A carry, @Nullable B next);
}