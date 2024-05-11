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

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Gatherer;
import java.util.stream.Stream;

public class Gatherers4j {
    /**
     * <p>Given a stream of objects, filter the objects such that any consecutively appearing
     * after the first one are dropped.
     *
     * <p>Examples:<ul>
     * <li>[A, A, B] => [A, B]
     * <li>[A, B, A] => [A, B, A]
     * </ul>
     */
    public static <INPUT> Gatherer<INPUT, ?, INPUT> dedupeConsecutive() {
        return new DedupeConsecutiveGatherer<>(null);
    }

    /**
     *
     * @param function A mapping function used to compare objects in the stream for equality.
     */
    public static <INPUT> Gatherer<INPUT, ?, INPUT> dedupeConsecutiveBy(final Function<INPUT, Object> function) {
        Objects.requireNonNull(function, "Mapping function cannot be null");
        return new DedupeConsecutiveGatherer<>(function);
    }

    /**
     * Filter a stream to only distinct elements as described by the given function.
     * @param function The mapping function
     */
    public static <INPUT, OUTPUT> Gatherer<INPUT, ?, INPUT> distinctBy(final Function<INPUT, OUTPUT> function) {
        Objects.requireNonNull(function, "Mapping function cannot be null"); // TODO: Where should this go?
        return new DistinctGatherer<>(function);
    }

    public static <INPUT> Gatherer<INPUT, Void, INPUT> interleave(final Stream<INPUT> other) {
        return new InterleavingGatherer<>(other);
    }

    public static <INPUT,OUTPUT> Gatherer<INPUT, ?, IndexedValue<OUTPUT>> mapWithIndex(final Function<INPUT,OUTPUT> mappingFunction) {
        Objects.requireNonNull(mappingFunction, "Mapping function cannot be null");
        return new IndexingGatherer<>(mappingFunction);
    }

    public static <INPUT> Gatherer<INPUT, ?, IndexedValue<INPUT>> withIndex() {
        return new IndexingGatherer<>(Function.identity());
    }

    public static <FIRST,SECOND> Gatherer<FIRST, Void, Pair<FIRST,SECOND>> zip(final Stream<SECOND> other) {
        return new ZipGatherer<>(other);
    }

    public static <INPUT> Gatherer<INPUT,?, List<INPUT>> zipWithNext() {
        return new ZipWithNextGatherer<>();
    }
}
