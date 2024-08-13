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

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Gatherer;
import java.util.stream.Stream;

import static com.ginsberg.gatherers4j.GathererUtils.mustNotBeNull;

public class Gatherers4j {

    /**
     * Concatenate the given <code>Stream&lt;INPUT&gt;</code> to the end of the current stream, in order.
     *
     * @param concatThis A non-null <code>Stream&lt;INPUT&gt;</code> instance to concatenate.
     */
    public static <INPUT> ConcatenationGatherer<INPUT> concat(final Stream<INPUT> concatThis) {
        return new ConcatenationGatherer<>(concatThis);
    }

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
     * Remove consecutive duplicates from a stream where duplication is measured by the given <code>function</code>.
     *
     * @param function A mapping function used to compare objects in the stream for equality.
     */
    public static <INPUT> Gatherer<INPUT, ?, INPUT> dedupeConsecutiveBy(final Function<INPUT, Object> function) {
        mustNotBeNull(function, "Mapping function must not be null");
        return new DedupeConsecutiveGatherer<>(function);
    }

    /**
     * Filter a stream to only distinct elements as described by the given <code>function</code>.
     *
     * @param function The non-null mapping function
     */
    public static <INPUT, OUTPUT> Gatherer<INPUT, ?, INPUT> distinctBy(final Function<INPUT, OUTPUT> function) {
        mustNotBeNull(function, "Mapping function must not be null");
        return new DistinctGatherer<>(function);
    }

    /**
     * Creates a stream of alternating objects from the input stream and the argument stream
     *
     * @param other A non-null stream to interleave
     */
    public static <INPUT> Gatherer<INPUT, Void, INPUT> interleave(final Stream<INPUT> other) {
        return new InterleavingGatherer<>(other);
    }

    /**
     * Remove all but the last {@code count} elements from the stream.
     *
     * @param count A non-negative integer, the number of elements to return
     */
    public static <INPUT> LastGatherer<INPUT> last(final int count) {
        return new LastGatherer<>(count);
    }

    /**
     * Create a <code>Stream&lt;BigDecimal&gt;</code> that represents the running population standard deviation of a <code>Stream&lt;BigDecimal&gt;</code>.
     */
    public static BigDecimalStandardDeviationGatherer<BigDecimal> runningPopulationStandardDeviation() {
        return new BigDecimalStandardDeviationGatherer<>(
                BigDecimalStandardDeviationGatherer.Mode.Population,
                Function.identity()
        );
    }

    /**
     * Create a <code>Stream&lt;BigDecimal&gt;</code> that represents the running population standard deviation of a <code>BigDecimal</code> objects mapped
     * from a <code>Stream&lt;BigDecimal&gt;</code> via a <code>mappingFunction</code>.
     */
    public static <INPUT> BigDecimalStandardDeviationGatherer<INPUT> runningPopulationStandardDeviationBy(
            final Function<INPUT, BigDecimal> mappingFunction
    ) {
        return new BigDecimalStandardDeviationGatherer<>(
                BigDecimalStandardDeviationGatherer.Mode.Population,
                mappingFunction
        );
    }

    /**
     * Create a <code>Stream&lt;BigDecimal&gt;</code> that represents the running sample standard deviation of a <code>Stream&lt;BigDecimal&gt;</code>.
     */
    public static BigDecimalStandardDeviationGatherer<BigDecimal> runningSampleStandardDeviation() {
        return new BigDecimalStandardDeviationGatherer<>(
                BigDecimalStandardDeviationGatherer.Mode.Sample,
                Function.identity()
        );
    }

    /**
     * Create a <code>Stream&lt;BigDecimal&gt;</code> that represents the running sample standard deviation of a <code>BigDecimal</code> objects mapped
     * from a <code>Stream&lt;BigDecimal&gt;</code> via a <code>mappingFunction</code>.
     *
     * @param mappingFunction The non-null function to map from <code>&lt;INPUT&gt;</code> to <code>BigDecimal</code>.
     */
    public static <INPUT> BigDecimalStandardDeviationGatherer<INPUT> runningSampleStandardDeviationBy(
            final Function<INPUT, BigDecimal> mappingFunction
    ) {
        return new BigDecimalStandardDeviationGatherer<>(
                BigDecimalStandardDeviationGatherer.Mode.Sample,
                mappingFunction
        );
    }

    /**
     * Create a Stream that is the running average of <code>Stream&lt;BigDecimal&gt;</code>
     *
     * @return BigDecimalSimpleAverageGatherer
     */
    public static BigDecimalSimpleAverageGatherer<BigDecimal> simpleRunningAverage() {
        return simpleRunningAverageBy(Function.identity());
    }

    /**
     * Create a Stream that is the running average of <code>BigDecimal</code> objects as mapped by
     * the given function. This is useful when paired with the <code>withOriginal</code> function.
     *
     * @param mappingFunction A non-null function to map the <code>INPUT</code> type to <code>BigDecimal</code>
     * @return BigDecimalSimpleAverageGatherer
     */
    public static <INPUT> BigDecimalSimpleAverageGatherer<INPUT> simpleRunningAverageBy(
            final Function<INPUT, BigDecimal> mappingFunction
    ) {
        mustNotBeNull(mappingFunction, "Mapping function must not be null");
        return new BigDecimalSimpleAverageGatherer<>(mappingFunction);
    }

    /**
     * Create a Stream that represents the simple moving average of a <code>Stream&lt;BigDecimal&gt;</code> looking back `windowSize` number of elements.
     *
     * @param windowSize The number of elements to average, must be greater than 1.
     */
    public static BigDecimalSimpleMovingAverageGatherer<BigDecimal> simpleMovingAverage(final int windowSize) {
        return simpleMovingAverageBy(Function.identity(), windowSize);
    }

    /**
     * Create a Stream that represents the simple moving average of a <code>BigDecimal</code> objects mapped from a <code>Stream&lt;BigDecimal&gt;</code>
     * via a <code>mappingFunction</code> and looking back `windowSize` number of elements.
     *
     * @param mappingFunction The non-null function to map from <code>&lt;INPUT&gt;</code> to <code>BigDecimal</code>.
     * @param windowSize      The number of elements to average, must be greater than 1.
     */
    public static <INPUT> BigDecimalSimpleMovingAverageGatherer<INPUT> simpleMovingAverageBy(
            final Function<INPUT, BigDecimal> mappingFunction,
            final int windowSize
    ) {
        mustNotBeNull(mappingFunction, "Mapping function must not be null");
        return new BigDecimalSimpleMovingAverageGatherer<>(mappingFunction, windowSize);
    }

    /**
     * Maps all elements of the stream as-is, along with their 0-based index.
     */
    public static <INPUT> Gatherer<INPUT, ?, IndexedValue<INPUT>> withIndex() {
        return new IndexingGatherer<>();
    }

    /**
     * Creates a stream of `Pair` objects whose values come from the input stream and argument stream
     *
     * @param other A non-null stream to zip with
     */
    public static <FIRST, SECOND> Gatherer<FIRST, Void, Pair<FIRST, SECOND>> zipWith(final Stream<SECOND> other) {
        return new ZipWithGatherer<>(other);
    }

    /**
     * Creates a stream of `List` objects via a sliding window of width 2 and stepping 1
     */
    public static <INPUT> Gatherer<INPUT, ?, List<INPUT>> zipWithNext() {
        return new ZipWithNextGatherer<>();
    }
}
