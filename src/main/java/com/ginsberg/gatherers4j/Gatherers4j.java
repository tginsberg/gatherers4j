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
import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.random.RandomGenerator;
import java.util.stream.Gatherer;
import java.util.stream.Stream;

import static com.ginsberg.gatherers4j.GathererUtils.mustNotBeNull;

public class Gatherers4j {

    /// Limit the number of elements in the stream to some number per period, dropping anything over the
    /// limit during the period.
    ///
    /// @param amount   A positive number of elements to allow per period
    /// @param duration A positive duration for the length of the period
    /// @param <INPUT>  Type of elements in the stream
    public static <INPUT> ThrottlingGatherer<INPUT> debounce(final int amount, final Duration duration) {
        return new ThrottlingGatherer<>(ThrottlingGatherer.LimitRule.Drop, amount, duration);
    }
    
    /// Remove consecutive duplicate elements from a stream according `Object.equals(Object)`
    public static <INPUT> Gatherer<INPUT, ?, INPUT> dedupeConsecutive() {
        return new DedupeConsecutiveGatherer<>(null);
    }

    /// Remove consecutive duplicates from a stream where duplication is measured by the given `function`.
    ///
    /// @param mappingFunction A non-null function, the results of which will be used to check for consecutive duplication.
    public static <INPUT> Gatherer<INPUT, ?, INPUT> dedupeConsecutiveBy(final Function<INPUT, Object> mappingFunction) {
        mustNotBeNull(mappingFunction, "Mapping function must not be null");
        return new DedupeConsecutiveGatherer<>(mappingFunction);
    }

    /// Filter a stream such that it only contains distinct elements measured by the given `function`.
    ///
    /// @param mappingFunction A non-null mapping function, the results of which will be used to check for distinct elements
    public static <INPUT, OUTPUT> Gatherer<INPUT, ?, INPUT> distinctBy(final Function<INPUT, OUTPUT> mappingFunction) {
        mustNotBeNull(mappingFunction, "Mapping function must not be null");
        return new DistinctGatherer<>(mappingFunction);
    }

    /// Keep all elements except the last `count` elements of the stream.
    ///
    /// @param count A positive number of elements to drop from the end of the stream
    public static <INPUT> DropLastGatherer<INPUT> dropLast(final int count) {
        return new DropLastGatherer<>(count);
    }

    /// Ensure the input stream is exactly `size` elements long, and emit all elements if so.
    /// If not, throw an `IllegalStateException`.
    ///
    /// @param size Exact number of elements the stream must have
    public static <INPUT> SizeGatherer<INPUT> exactSize(final long size) {
        return new SizeGatherer<>(size);
    }

    /// Filter a stream according to the given `predicate`, which takes both the item being examined, 
    /// and its index.
    ///
    /// @param predicate A non-null `BiPredicate<Long,INPUT>` where the `Long` is 
    ///                  the zero-based index of the element being filtered, and the `INPUT` is 
    ///                  the element itself.
    public static <INPUT> FilteringWithIndexGatherer<INPUT> filterWithIndex(
            final BiPredicate<Long, INPUT> predicate
    ) {
        return new FilteringWithIndexGatherer<>(predicate);
    }

    /// Turn a `Stream<INPUT>` into a `Stream<List<INPUT>>` where consecutive
    /// equal elements, where equality is measured by `Object.equals(Object)`.
    public static <INPUT> GroupingByGatherer<INPUT> grouping() {
        return new GroupingByGatherer<>(it -> it);
    }

    /// Turn a `Stream<INPUT>` into a `Stream<List<INPUT>>` where consecutive
    /// equal elements, where equality is measured by the given `mappingFunction`, are in the same `List`.
    ///
    /// @param mappingFunction A non-null function, the results of which are used to measure equality of consecutive elements.
    public static <INPUT> GroupingByGatherer<INPUT> groupingBy(final Function<INPUT, Object> mappingFunction) {
        return new GroupingByGatherer<>(mappingFunction);
    }

    /// Creates a stream of alternating objects from the input stream and the argument iterable
    ///
    /// @param other A non-null Iterable to interleave
    public static <INPUT> InterleavingGatherer<INPUT> interleave(final Iterable<INPUT> other) {
        return new InterleavingGatherer<>(other);
    }

    /// Creates a stream of alternating objects from the input stream and the argument iterator
    ///
    /// @param other A non-null Iterator to interleave
    public static <INPUT> InterleavingGatherer<INPUT> interleave(final Iterator<INPUT> other) {
        return new InterleavingGatherer<>(other);
    }

    /// Creates a stream of alternating objects from the input stream and the argument stream
    ///
    /// @param other A non-null stream to interleave
    public static <INPUT> InterleavingGatherer<INPUT> interleave(final Stream<INPUT> other) {
        return new InterleavingGatherer<>(other);
    }

    /// Remove all but the last `count` elements from the stream.
    ///
    /// @param count A non-negative integer, the number of elements to return
    public static <INPUT> LastGatherer<INPUT> last(final int count) {
        return new LastGatherer<>(count);
    }

    /// Return a Stream containing the single maximum value of the input stream, according to
    /// the given mapping function. In the case where a stream has more than one mapped value
    /// that is the maximum, the first one encountered makes up the stream. This does not
    /// evaluate null values or null mappings.
    ///
    /// @param mappingFunction A mapping function, the results of which must implement `Comparable`
    public static <INPUT, MAPPED extends Comparable<MAPPED>> MinMaxGatherer<INPUT, MAPPED> maxBy(
            final Function<INPUT, MAPPED> mappingFunction
    ) {
        mustNotBeNull(mappingFunction, "Mapping function must not be null");
        return new MinMaxGatherer<>(true, mappingFunction);
    }

    /// Return a Stream containing the single minimum value of the input stream, according to
    /// the given mapping function. In the case where a stream has more than one mapped value
    /// that is the minimum, the first one encountered makes up the stream. This does not
    /// evaluate null values or null mappings.
    ///
    /// @param mappingFunction A mapping function, the results of which must implement `Comparable`
    public static <INPUT, MAPPED extends Comparable<MAPPED>> MinMaxGatherer<INPUT, MAPPED> minBy(
            final Function<INPUT, MAPPED> mappingFunction
    ) {
        mustNotBeNull(mappingFunction, "Mapping function must not be null");
        return new MinMaxGatherer<>(false, mappingFunction);
    }

    /// Reverse the order of the input Stream.
    ///
    /// Note: This consumes the entire stream and holds it in memory, so it will not work on infinite
    /// streams and may cause memory pressure on very large streams.
    public static <INPUT> ReversingGatherer<INPUT> reverse() {
        return new ReversingGatherer<>();
    }

    /// Create a `Stream<BigDecimal>` that represents the running population standard 
    /// deviation of a `Stream<BigDecimal>`.
    public static BigDecimalStandardDeviationGatherer<BigDecimal> runningPopulationStandardDeviation() {
        return new BigDecimalStandardDeviationGatherer<>(
                BigDecimalStandardDeviationGatherer.Mode.Population,
                Function.identity()
        );
    }

    /// Create a `Stream<BigDecimal>` that represents the running population standard deviation of a `BigDecimal` objects mapped
    /// from a `Stream<BigDecimal>` via a `mappingFunction`.
    ///
    /// @param mappingFunction A function to map `<INPUT>` objects to `BigDecimal`, the results of which will be used
    ///                        in the standard deviation calculation
    public static <INPUT> BigDecimalStandardDeviationGatherer<INPUT> runningPopulationStandardDeviationBy(
            final Function<INPUT, BigDecimal> mappingFunction
    ) {
        return new BigDecimalStandardDeviationGatherer<>(
                BigDecimalStandardDeviationGatherer.Mode.Population,
                mappingFunction
        );
    }

    /// Create a `Stream<BigDecimal>` that represents the running product of a `Stream<BigDecimal>`.
    public static BigDecimalProductGatherer<BigDecimal> runningProduct() {
        return new BigDecimalProductGatherer<>(Function.identity());
    }

    /// Create a `Stream<BigDecimal>` that represents the running product of `BigDecimal` objects mapped
    /// from a `Stream<INPUT>` via a `mappingFunction`.
    ///
    /// @param mappingFunction A function to map `<INPUT>` objects to `BigDecimal`, the results of which will be used
    ///                        in the product calculation
    public static <INPUT> BigDecimalProductGatherer<INPUT> runningProductBy(
            final Function<INPUT, BigDecimal> mappingFunction
    ) {
        return new BigDecimalProductGatherer<>(mappingFunction);
    }

    /// Create a `Stream<BigDecimal>` that represents the running sample standard deviation of a `Stream<BigDecimal>`.
    public static BigDecimalStandardDeviationGatherer<BigDecimal> runningSampleStandardDeviation() {
        return new BigDecimalStandardDeviationGatherer<>(
                BigDecimalStandardDeviationGatherer.Mode.Sample,
                Function.identity()
        );
    }

    /// Create a `Stream<BigDecimal>` that represents the running sample standard deviation of `BigDecimal` objects mapped
    /// from a `Stream<INPUT>` via a `mappingFunction`.
    ///
    /// @param mappingFunction A function to map `<INPUT>` objects to `BigDecimal`, the results of which will be used
    ///                        in the standard deviation calculation
    public static <INPUT> BigDecimalStandardDeviationGatherer<INPUT> runningSampleStandardDeviationBy(
            final Function<INPUT, BigDecimal> mappingFunction
    ) {
        return new BigDecimalStandardDeviationGatherer<>(
                BigDecimalStandardDeviationGatherer.Mode.Sample,
                mappingFunction
        );
    }

    /// Create a `Stream<BigDecimal>` that represents the running sum of a `Stream<BigDecimal>`.
    public static BigDecimalSumGatherer<BigDecimal> runningSum() {
        return new BigDecimalSumGatherer<>(Function.identity());
    }

    /// Create a `Stream<BigDecimal>` that represents the running sum of `BigDecimal` objects mapped
    /// from a `Stream<INPUT>` via a `mappingFunction`.
    ///
    /// @param mappingFunction A function to map `<INPUT>` objects to `BigDecimal`, the results of which will be used
    ///                        in the running sum calculation
    public static <INPUT> BigDecimalSumGatherer<INPUT> runningSumBy(
            final Function<INPUT, BigDecimal> mappingFunction
    ) {
        return new BigDecimalSumGatherer<>(mappingFunction);
    }

    /// Create a Stream that is the running average of `Stream<BigDecimal>`
    ///
    /// @return BigDecimalSimpleAverageGatherer
    public static BigDecimalSimpleAverageGatherer<BigDecimal> simpleRunningAverage() {
        return simpleRunningAverageBy(Function.identity());
    }

    /// Shuffle the input stream into a random order.
    ///
    /// Note: This consumes the entire stream and holds it in memory, so it will not work on infinite
    /// streams and may cause memory pressure on very large streams.
    public static <INPUT> ShufflingGatherer<INPUT> shuffle() {
        return new ShufflingGatherer<>(RandomGenerator.getDefault());
    }

    /// Shuffle the input stream into a random order.
    ///
    /// Note: This consumes the entire stream and holds it in memory, so it will not work on infinite
    /// streams and may cause memory pressure on very large streams.
    ///
    /// @param randomGenerator A non-null `RandomGenerator` to use as a random source for the shuffle
    public static <INPUT> ShufflingGatherer<INPUT> shuffle(final RandomGenerator randomGenerator) {
        return new ShufflingGatherer<>(randomGenerator);
    }

    /// Create a Stream that is the running average of `BigDecimal` objects as mapped by
    /// the given function. This is useful when paired with the `withOriginal` function.
    ///
    /// @param mappingFunction A function to map `<INPUT>` objects to `BigDecimal`, the results of which will be used
    ///                        in the running average calculation
    public static <INPUT> BigDecimalSimpleAverageGatherer<INPUT> simpleRunningAverageBy(
            final Function<INPUT, BigDecimal> mappingFunction
    ) {
        mustNotBeNull(mappingFunction, "Mapping function must not be null");
        return new BigDecimalSimpleAverageGatherer<>(mappingFunction);
    }

    /// Create a Stream that represents the simple moving average of a `Stream<BigDecimal>` looking
    /// back `windowSize` number of elements.
    ///
    /// @param windowSize The number of elements to average, must be greater than 1.
    public static BigDecimalSimpleMovingAverageGatherer<BigDecimal> simpleMovingAverage(final int windowSize) {
        return simpleMovingAverageBy(Function.identity(), windowSize);
    }

    /// Create a Stream that represents the simple moving average of a `BigDecimal` objects mapped from a `Stream<BigDecimal>`
    /// via a `mappingFunction` and looking back `windowSize` number of elements.
    ///
    /// @param mappingFunction A function to map `<INPUT>` objects to `BigDecimal`, the results of which will be used
    ///                        in the moving average calculation
    /// @param windowSize      The number of elements to average, must be greater than 1.
    public static <INPUT> BigDecimalSimpleMovingAverageGatherer<INPUT> simpleMovingAverageBy(
            final Function<INPUT, BigDecimal> mappingFunction,
            final int windowSize
    ) {
        mustNotBeNull(mappingFunction, "Mapping function must not be null");
        return new BigDecimalSimpleMovingAverageGatherer<>(mappingFunction, windowSize);
    }

    /// Limit the number of elements in the stream to some number per period. When the limit is reached,
    /// consumption is paused until a new period starts and the count resets.
    ///
    /// @param amount   A positive number of elements to allow per period
    /// @param duration A positive duration for the length of the period
    public static <INPUT> ThrottlingGatherer<INPUT> throttle(final int amount, final Duration duration) {
        return new ThrottlingGatherer<>(ThrottlingGatherer.LimitRule.Pause, amount, duration);
    }

    /// Maps all elements of the stream as-is along with their 0-based index.
    public static <INPUT> IndexingGatherer<INPUT> withIndex() {
        return new IndexingGatherer<>();
    }

    /// Creates a stream of `Pair<FIRST,SECOND>` objects whose values come from the stream this is called on
    /// and the argument collection
    ///
    /// @param other A non-null iterable to zip with
    public static <FIRST, SECOND> ZipWithGatherer<FIRST, SECOND> zipWith(final Iterable<SECOND> other) {
        return new ZipWithGatherer<>(other);
    }

    /// Creates a stream of `Pair<FIRST,SECOND>` objects whose values come from the stream this is called on
    /// and the argument iterator
    ///
    /// @param other A non-null iterator to zip with
    public static <FIRST, SECOND> ZipWithGatherer<FIRST, SECOND> zipWith(final Iterator<SECOND> other) {
        return new ZipWithGatherer<>(other);
    }

    /// Creates a stream of `Pair<FIRST,SECOND>` objects whose values come from the stream this is called on
    /// and the argument stream
    ///
    /// @param other A non-null stream to zip with
    public static <FIRST, SECOND> ZipWithGatherer<FIRST, SECOND> zipWith(final Stream<SECOND> other) {
        return new ZipWithGatherer<>(other);
    }
    
    /// Creates a stream of `List` objects which contain each two adjacent elements in the input stream.
    public static <INPUT> Gatherer<INPUT, ?, List<INPUT>> zipWithNext() {
        return new ZipWithNextGatherer<>();
    }
}
