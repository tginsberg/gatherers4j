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
import java.time.Duration;
import java.util.Comparator;
import java.util.Iterator;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;
import java.util.stream.Gatherer;
import java.util.stream.Stream;

import static com.ginsberg.gatherers4j.GathererUtils.mustNotBeNull;

/// This is the main entry-point for the Gatherers4j library. All available gatherers
/// are created from static methods on this class.
public abstract class Gatherers4j {

    /// Cross every element of the input stream with every element of the given `Iterable`, emitting them
    /// to the output stream as a `Pair<INPUT, CROSS>`.
    ///
    /// @param <INPUT> Type of element in the input stream
    /// @param <CROSS> Type of element in the cross `Iterable`
    /// @param crossWith The Iterable to cross with
    /// @return A non-null Gatherer
    public static <INPUT extends @Nullable Object, CROSS extends @Nullable Object> Gatherer<INPUT, ?, Pair<INPUT, CROSS>> cross(
            final Iterable<CROSS> crossWith
    ) {
        return CrossGatherer.of(crossWith);
    }

    /// Cross every element of the input stream with every element of the given `Iterator`, emitting them
    /// to the output stream as a `Pair<INPUT, CROSS>`.
    ///
    /// Note: the Iterator is consumed fully and stored as a List in memory.
    ///
    /// @param <INPUT> Type of element in the input stream
    /// @param <CROSS> Type of element in the cross `Iterator`
    /// @param crossWith The Iterator to cross with
    /// @return A non-null Gatherer
    public static <INPUT extends @Nullable Object, CROSS extends @Nullable Object> Gatherer<INPUT, ?, Pair<INPUT, CROSS>> cross(
            final Iterator<CROSS> crossWith
    ) {
        return CrossGatherer.of(crossWith);
    }

    /// Cross every element of the input stream with every element of the given `Stream`, emitting them
    /// to the output stream as a `Pair<INPUT, CROSS>`.
    ///
    /// Note: the Iterator is consumed fully and stored as a List in memory.
    /// Note: The Ghostbusters warned us about this and I hereby absolve myself of any responsibility if you cause some kind of cataclysm.
    ///
    /// @param <INPUT> Type of element in the input stream
    /// @param <CROSS> Type of element in the cross `Iterator`
    /// @param crossWith The Stream to cross with
    /// @return A non-null Gatherer
    public static <INPUT extends @Nullable Object, CROSS extends @Nullable Object> Gatherer<INPUT, ?, Pair<INPUT, CROSS>> cross(
            final Stream<CROSS> crossWith
    ) {
        return CrossGatherer.of(crossWith);
    }

    /// Limit the number of elements in the stream to some number per period, dropping anything over the
    /// limit during the period.
    ///
    /// @param amount   A positive number of elements to allow per period
    /// @param duration A positive duration for the length of the period
    /// @param <INPUT>  Type of elements in both the input and output streams
    /// @return A non-null `ThrottlingGatherer`
    public static <INPUT extends @Nullable Object> ThrottlingGatherer<INPUT> debounce(
            final int amount,
            final Duration duration
    ) {
        return new ThrottlingGatherer<>(ThrottlingGatherer.LimitRule.Drop, amount, duration);
    }

    /// Convert the input stream of `Comparable` objects into lists of strictly decreasing objects. The lists
    /// emitted to the output stream are unmodifiable.
    ///
    /// ```java
    /// Stream.of(3, 2, 1, 3, 2)
    ///     .gather(decreasing())
    ///     .toList();
    ///
    /// // [[3, 2, 1], [3, 2]]
    /// ```
    ///
    /// @param <INPUT> Type of elements in the input stream
    /// @return A non-null Gatherer
    public static <INPUT extends Comparable<INPUT>> IncreasingDecreasingComparableGatherer<INPUT> decreasing() {
        return new IncreasingDecreasingComparableGatherer<>(IncreasingDecreasingComparatorGatherer.Operation.Decreasing);
    }

    /// Convert the input stream of objects into lists of strictly decreasing objects, as measured by the given `Comparator`.
    /// The lists emitted to the output stream are unmodifiable.
    ///
    /// ```java
    /// Stream.of("ABC", "AB", "A", "ABC", "AB")
    ///     .gather(decreasing(Comparator.comparingInt(String::length)))
    ///     .toList();
    ///
    /// // [["ABC", "AB", "A"], ["ABC", "AB"]]
    /// ```
    ///
    /// @param <INPUT> Type of elements in the input stream
    /// @param comparator The non-null comparator used to compare stream elements
    /// @return A non-null Gatherer
    public static <INPUT> IncreasingDecreasingComparatorGatherer<INPUT> decreasing(final Comparator<INPUT> comparator) {
        return new IncreasingDecreasingComparatorGatherer<>(IncreasingDecreasingComparatorGatherer.Operation.Decreasing, comparator);
    }

    /// Remove consecutive duplicate elements from a stream according `Object.equals(Object)`
    ///
    /// @param <INPUT> Type of elements in both the input and output streams
    /// @return A non-null `DedupeConsecutiveGatherer`
    public static <INPUT extends @Nullable Object> DedupeConsecutiveGatherer<INPUT> dedupeConsecutive() {
        return new DedupeConsecutiveGatherer<>();
    }

    /// Remove consecutive duplicates from a stream where duplication is measured by the given `function`.
    ///
    /// @param mappingFunction A non-null function, the results of which will be used to check for consecutive duplication.
    /// @param <INPUT>         Type of elements in both the input and output streams
    /// @return A non-null `DedupeConsecutiveGatherer`
    public static <INPUT extends @Nullable Object> DedupeConsecutiveGatherer<INPUT> dedupeConsecutiveBy(
            final Function<INPUT, Object> mappingFunction
    ) {
        mustNotBeNull(mappingFunction, "Mapping function must not be null");
        return new DedupeConsecutiveGatherer<>(mappingFunction);
    }

    /// Filter a stream such that it only contains distinct elements measured by the given `function`.
    ///
    /// @param mappingFunction A non-null mapping function, the results of which will be used to check for distinct elements
    /// @param <INPUT>         Type of elements in both the input and output streams
    /// @return A non-null `DistinctGatherer`
    public static <INPUT extends @Nullable Object> DistinctGatherer<INPUT> distinctBy(
            final Function<INPUT, @Nullable Object> mappingFunction
    ) {
        return new DistinctGatherer<>(mappingFunction);
    }

    /// Keep all elements except the last `count` elements of the stream.
    ///
    /// @param count   A positive number of elements to drop from the end of the stream
    /// @param <INPUT> Type of elements in both the input and output streams
    /// @return A non-null `DropLastGatherer`
    public static <INPUT extends @Nullable Object> DropLastGatherer<INPUT> dropLast(final int count) {
        return new DropLastGatherer<>(count);
    }

    /// Ensure that the `Comparable` elements in the input stream are strictly decreasing, and fail otherwise.
    ///
    /// @param <INPUT> Type of elements in the input stream
    /// @return A non-null Gatherer
    public static <INPUT extends Comparable<INPUT>> Gatherer<INPUT, ?, INPUT> ensureDecreasing() {
        return new IncreasingDecreasingComparableGatherer<INPUT>(IncreasingDecreasingComparatorGatherer.Operation.Decreasing)
                .andThen(new FlattenSingleOrFail<>("Elements are not strictly decreasing"));
    }

    /// Ensure that the elements in the input stream are strictly decreasing as measured by the given `Comparator`, and fail otherwise.
    ///
    /// @param <INPUT> Type of elements in the input stream
    /// @param comparator The non-null comparator used to compare stream elements
    /// @return A non-null Gatherer
    public static <INPUT> Gatherer<INPUT, ?, INPUT> ensureDecreasing(final Comparator<INPUT> comparator) {
        return new IncreasingDecreasingComparatorGatherer<>(IncreasingDecreasingComparatorGatherer.Operation.Decreasing, comparator)
                .andThen(new FlattenSingleOrFail<>("Elements are not strictly decreasing"));
    }

    /// Ensure that the `Comparable` elements in the input stream are strictly increasing, and fail otherwise.
    ///
    /// @param <INPUT> Type of elements in the input stream
    /// @return A non-null Gatherer
    public static <INPUT extends Comparable<INPUT>> Gatherer<INPUT, ?, INPUT> ensureIncreasing() {
        return new IncreasingDecreasingComparableGatherer<INPUT>(IncreasingDecreasingComparatorGatherer.Operation.Increasing)
                .andThen(new FlattenSingleOrFail<>("Elements are not strictly increasing"));
    }

    /// Ensure that the elements in the input stream are strictly increasing as measured by the given `Comparator`, and fail otherwise.
    ///
    /// @param <INPUT> Type of elements in the input stream
    /// @param comparator The non-null comparator used to compare stream elements
    /// @return A non-null Gatherer
    public static <INPUT> Gatherer<INPUT, ?, INPUT> ensureIncreasing(final Comparator<INPUT> comparator) {
        return new IncreasingDecreasingComparatorGatherer<>(IncreasingDecreasingComparatorGatherer.Operation.Increasing, comparator)
                .andThen(new FlattenSingleOrFail<>("Elements are not strictly increasing"));
    }

    /// Ensure that the `Comparable` elements in the input stream are not strictly decreasing, and fail otherwise.
    ///
    /// @param <INPUT> Type of elements in the input stream
    /// @throws IllegalStateException If the stream contains elements in a not strictly decreasing order
    /// @return A non-null Gatherer
    public static <INPUT extends Comparable<INPUT>> Gatherer<INPUT, ?, INPUT> ensureNonDecreasing() {
        return new IncreasingDecreasingComparableGatherer<INPUT>(IncreasingDecreasingComparatorGatherer.Operation.NonDecreasing)
                .andThen(new FlattenSingleOrFail<>("Elements are decreasing"));
    }

    /// Ensure that the elements in the input stream are not strictly decreasing as measured by the given `Comparator`, and fail otherwise.
    ///
    /// @param <INPUT> Type of elements in the input stream
    /// @param comparator The non-null comparator used to compare stream elements
    /// @throws IllegalStateException If the stream contains elements in a not strictly decreasing order
    /// @return A non-null Gatherer
    public static <INPUT> Gatherer<INPUT, ?, INPUT> ensureNonDecreasing(final Comparator<INPUT> comparator) {
        return new IncreasingDecreasingComparatorGatherer<>(IncreasingDecreasingComparatorGatherer.Operation.NonDecreasing, comparator)
                .andThen(new FlattenSingleOrFail<>("Elements are decreasing"));
    }

    /// Ensure that the `Comparable` elements in the input stream are not in a strictly increasing order, and fail otherwise.
    ///
    /// @param <INPUT> Type of elements in the input stream
    /// @return A non-null Gatherer
    public static <INPUT extends Comparable<INPUT>> Gatherer<INPUT, ?, INPUT> ensureNonIncreasing() {
        return new IncreasingDecreasingComparableGatherer<INPUT>(IncreasingDecreasingComparatorGatherer.Operation.NonIncreasing)
                .andThen(new FlattenSingleOrFail<>("Elements are increasing"));
    }

    /// Ensure that the elements in the input stream are not in a strictly increasing order as measured by the
    /// given `Comparator`, and fail otherwise.
    ///
    /// @param <INPUT> Type of elements in the input stream
    /// @param comparator The non-null comparator used to compare stream elements
    /// @return A non-null Gatherer
    public static <INPUT> Gatherer<INPUT, ?, INPUT> ensureNonIncreasing(final Comparator<INPUT> comparator) {
        return new IncreasingDecreasingComparatorGatherer<>(IncreasingDecreasingComparatorGatherer.Operation.NonIncreasing, comparator)
                .andThen(new FlattenSingleOrFail<>("Elements are increasing"));
    }

    /// Keep every nth element of the stream.
    ///
    /// @param count   The number of the elements to keep, must be at least 2
    /// @param <INPUT> Type of elements in both the input and output streams
    /// @return A non-null `EveryNthGatherer`
    public static <INPUT extends @Nullable Object> EveryNthGatherer<INPUT> everyNth(final int count) {
        return new EveryNthGatherer<>(count);
    }

    /// Filter the elements in the stream to only include elements of the given types.
    /// Note, due to how generics work you may end up with some... interesting stream types as a result
    ///
    /// @param <INPUT> Type of elements in the input stream
    /// @param <OUTPUT> Type of elements in the output stream
    /// @param validTypes A non-empty array of types to filter for
    /// @return A non-null `Gatherer`
    @SafeVarargs
    public static <INPUT extends @Nullable Object, OUTPUT extends @Nullable Object> Gatherer<INPUT, ?, OUTPUT> filterInstanceOf(
            final Class<? extends OUTPUT>... validTypes
    ) {
        return TypeFilteringGatherer.of(validTypes);
    }

    /// Filter a stream according to the given `predicate`, which takes both the item being examined,
    /// and its index.
    ///
    /// @param predicate A non-null `BiPredicate<Long,INPUT>` where the `Long` is the zero-based index of the element
    ///                  being filtered, and the `INPUT` is the element itself.
    /// @param <INPUT>   Type of elements in the input stream
    /// @return A non-null `FilteringWithIndexGatherer`
    public static <INPUT extends @Nullable Object> FilteringWithIndexGatherer<INPUT> filterWithIndex(
            final BiPredicate<Long, INPUT> predicate
    ) {
        return new FilteringWithIndexGatherer<>(predicate);
    }
    
    ///  Perform a fold over every element in the input stream along with its index
    ///
    /// @param <INPUT>      Type of elements in the input stream
    /// @param <OUTPUT>     Type elements are folded to (the accumulated value)
    /// @param initialValue Initial value of the fold
    /// @param foldFunction Function that performs the fold given an element, its index, and the carry value
    /// @return A non-null AccumulatingGatherer
    public static <INPUT extends @Nullable Object, OUTPUT extends @Nullable Object> AccumulatingGatherer<INPUT, OUTPUT> foldIndexed(
            final Supplier<OUTPUT> initialValue,
            final IndexedAccumulatorFunction<? super OUTPUT, ? super INPUT, ? extends OUTPUT> foldFunction
    ) {
        return new AccumulatingGatherer<>(false, initialValue, foldFunction);
    }

    /// Turn a `Stream<INPUT>` into a `Stream<List<INPUT>>` where consecutive
    /// equal elements, where equality is measured by `Object.equals(Object)`.
    ///
    /// @param <INPUT> Type of elements in the input stream
    /// @return A non-null `GroupingByGatherer`
    public static <INPUT extends @Nullable Object> GroupingByGatherer<INPUT> grouping() {
        return new GroupingByGatherer<>();
    }

    /// Turn a `Stream<INPUT>` into a `Stream<List<INPUT>>` where consecutive
    /// equal elements, where equality is measured by the given `mappingFunction`, are in the same `List`.
    ///
    /// @param mappingFunction A non-null function, the results of which are used to measure equality of consecutive elements.
    /// @param <INPUT>         Type of elements in the input stream
    /// @return A non-null `GroupingByGatherer`
    public static <INPUT extends @Nullable Object> GroupingByGatherer<INPUT> groupingBy(
            final Function<@Nullable INPUT, @Nullable Object> mappingFunction
    ) {
        return new GroupingByGatherer<>(mappingFunction);
    }

    /// Convert the input stream of `Comparable` objects into lists of strictly increasing objects. The lists
    /// emitted to the output stream are unmodifiable.
    ///
    /// ```java
    /// Stream.of(1, 2, 3, 2, 3)
    ///     .gather(increasing())
    ///     .toList();
    ///
    /// // [[1, 2, 3], [2, 3]]
    /// ```
    ///
    /// @param <INPUT> Type of elements in the input stream
    /// @return A non-null Gatherer
    public static <INPUT extends Comparable<INPUT>> IncreasingDecreasingComparableGatherer<INPUT> increasing() {
        return new IncreasingDecreasingComparableGatherer<>(IncreasingDecreasingComparatorGatherer.Operation.Increasing);
    }

    /// Convert the input stream of objects into lists of strictly increasing objects, as measured by the given `Comparator`.
    /// The lists emitted to the output stream are unmodifiable.
    ///
    /// ```java
    /// Stream.of("A", "AB", "ABC", "AB", "ABC")
    ///     .gather(increasing(Comparator.comparingInt(String::length)))
    ///     .toList();
    ///
    /// // [["A", "AB", "ABC"], ["AB", "ABC"]]
    /// ```
    ///
    /// @param <INPUT> Type of elements in the input stream
    /// @param comparator The non-null comparator used to compare stream elements
    /// @return A non-null Gatherer
    public static <INPUT> IncreasingDecreasingComparatorGatherer<INPUT> increasing(final Comparator<INPUT> comparator) {
        return new IncreasingDecreasingComparatorGatherer<>(IncreasingDecreasingComparatorGatherer.Operation.Increasing, comparator);
    }

    /// Creates a stream of alternating objects from the input stream and the argument iterable
    ///
    /// @param other   A non-null Iterable to interleave
    /// @param <INPUT> Type of elements in both the input stream and argument iterable
    /// @return A non-null `InterleavingGatherer`
    public static <INPUT extends @Nullable Object> InterleavingGatherer<INPUT> interleave(final Iterable<INPUT> other) {
        return new InterleavingGatherer<>(other);
    }

    /// Creates a stream of alternating objects from the input stream and the argument iterator
    ///
    /// @param other   A non-null Iterator to interleave
    /// @param <INPUT> Type of elements in both the input stream and argument iterator
    /// @return A non-null `InterleavingGatherer`
    public static <INPUT extends @Nullable Object> InterleavingGatherer<INPUT> interleave(final Iterator<INPUT> other) {
        return new InterleavingGatherer<>(other);
    }

    /// Creates a stream of alternating objects from the input stream and the argument stream
    ///
    /// @param other   A non-null stream to interleave
    /// @param <INPUT> Type of elements in both the input and argument streams
    /// @return A non-null `InterleavingGatherer`
    public static <INPUT extends @Nullable Object> InterleavingGatherer<INPUT> interleave(final Stream<INPUT> other) {
        return new InterleavingGatherer<>(other);
    }

    /// Intersperse the given `intersperseElement` between each element of the input stream.
    ///
    /// @param intersperseElement The element to intersperse, which may be null
    /// @param <INPUT> The type of elements in the stream and the element to intersperse
    /// @return A non-null IntersperseGatherer
    public static <INPUT extends @Nullable Object> IntersperseGatherer<INPUT> intersperse(final INPUT intersperseElement) {
        return new IntersperseGatherer<>(intersperseElement);
    }

    /// Remove all but the last `count` elements from the stream.
    ///
    /// @param count   A non-negative integer, the number of elements to return
    /// @param <INPUT> Type of elements in the input stream
    /// @return A non-null `LastGatherer`
    public static <INPUT> LastGatherer<INPUT> last(final int count) {
        return new LastGatherer<>(count);
    }

    /// Create a Stream that represents the moving product of a `Stream<BigDecimal>` looking
    /// back `windowSize` number of elements.
    ///
    /// @param windowSize The trailing number of elements to multiply, must be greater than 1.
    /// @return A non-null `BigDecimalMovingProductGatherer`
    public static BigDecimalMovingProductGatherer<@Nullable BigDecimal> movingProduct(final int windowSize) {
        return new BigDecimalMovingProductGatherer<>(windowSize, Function.identity());
    }

    /// Create a Stream that represents the moving product of a `BigDecimal` objects mapped from a `Stream<T>`
    /// via a `mappingFunction` and looking back `windowSize` number of elements.
    ///
    /// @param windowSize      The trailing number of elements to multiply, must be greater than 1.
    /// @param mappingFunction A function to map `<INPUT>` objects to `BigDecimal`, the results of which will be used
    ///                        in the moving product calculation
    /// @param <INPUT>         Type of elements in the input stream, to be remapped to `BigDecimal` by the `mappingFunction`
    /// @return A non-null `BigDecimalMovingProductGatherer`
    public static <INPUT extends @Nullable Object> BigDecimalMovingProductGatherer<INPUT> movingProductBy(
            final int windowSize,
            final Function<INPUT, BigDecimal> mappingFunction
    ) {
        return new BigDecimalMovingProductGatherer<>(windowSize, mappingFunction);
    }

    /// Create a Stream that represents the moving sum of a `Stream<BigDecimal>` looking
    /// back `windowSize` number of elements.
    ///
    /// @param windowSize The trailing number of elements to add, must be greater than 1.
    /// @return A non-null `BigDecimalMovingSumGatherer`
    public static BigDecimalMovingSumGatherer<@Nullable BigDecimal> movingSum(final int windowSize) {
        return new BigDecimalMovingSumGatherer<>(windowSize, Function.identity());
    }

    /// Create a Stream that represents the moving sum of a `BigDecimal` objects mapped from a `Stream<T>`
    /// via a `mappingFunction` and looking back `windowSize` number of elements.
    ///
    /// @param windowSize      The trailing number of elements to multiply, must be greater than 1.
    /// @param mappingFunction A function to map `<INPUT>` objects to `BigDecimal`, the results of which will be used
    ///                        in the moving sum calculation
    /// @param <INPUT>         Type of elements in the input stream, to be remapped to `BigDecimal` by the `mappingFunction`
    /// @return A non-null `BigDecimalMovingSumGatherer`
    public static <INPUT extends @Nullable Object> BigDecimalMovingSumGatherer<INPUT> movingSumBy(
            final int windowSize,
            final Function<INPUT, BigDecimal> mappingFunction
    ) {
        return new BigDecimalMovingSumGatherer<>(windowSize, mappingFunction);
    }

    /// Convert the input stream of `Comparable` objects into lists of non-decreasing objects. The lists
    /// emitted to the output stream are unmodifiable.
    ///
    /// ```java
    /// Stream.of(2, 3, 3, 2, 3)
    ///     .gather(nonDecreasing())
    ///     .toList();
    ///
    /// // [[2, 3, 3], [2, 3]]
    /// ```
    ///
    /// @param <INPUT> Type of elements in the input stream
    /// @return A non-null Gatherer
    public static <INPUT extends Comparable<INPUT>> IncreasingDecreasingComparableGatherer<INPUT> nonDecreasing() {
        return new IncreasingDecreasingComparableGatherer<>(IncreasingDecreasingComparatorGatherer.Operation.NonDecreasing);
    }

    /// Convert the input stream of objects into lists of non-decreasing objects, as measured by the given `Comparator`.
    /// The lists emitted to the output stream are unmodifiable.
    ///
    /// ```java
    /// Stream.of("A", "AB", "AB", "A", "AB")
    ///     .gather(decreasing(Comparator.comparingInt(String::length)))
    ///     .toList();
    ///
    /// // [["A", "AB", "AB"], ["A", "AB"]]
    /// ```
    ///
    /// @param <INPUT> Type of elements in the input stream
    /// @param comparator The non-null comparator used to compare stream elements
    /// @return A non-null Gatherer
    public static <INPUT> IncreasingDecreasingComparatorGatherer<INPUT> nonDecreasing(final Comparator<INPUT> comparator) {
        return new IncreasingDecreasingComparatorGatherer<>(IncreasingDecreasingComparatorGatherer.Operation.NonDecreasing, comparator);
    }

    /// Convert the input stream of `Comparable` objects into lists of non-increasing objects. The lists
    /// emitted to the output stream are unmodifiable.
    ///
    /// ```java
    /// Stream.of(3, 2, 2, 3, 2)
    ///     .gather(nonIncreasing())
    ///     .toList();
    ///
    /// // [[3, 2, 2], [3, 2]]
    /// ```
    ///
    /// @param <INPUT> Type of elements in the input stream
    /// @return A non-null Gatherer
    public static <INPUT extends Comparable<INPUT>> IncreasingDecreasingComparableGatherer<INPUT> nonIncreasing() {
        return new IncreasingDecreasingComparableGatherer<>(IncreasingDecreasingComparatorGatherer.Operation.NonIncreasing);
    }

    /// Convert the input stream of objects into lists of non-increasing objects, as measured by the given `Comparator`.
    /// The lists emitted to the output stream are unmodifiable.
    ///
    /// ```java
    /// Stream.of("ABC", "AB", "AB", "ABC", "AB")
    ///     .gather(nonIncreasing(Comparator.comparingInt(String::length)))
    ///     .toList();
    ///
    /// // [["ABC", "AB", "AB"], ["ABC", "AB"]]
    /// ```
    ///
    /// @param <INPUT> Type of elements in the input stream
    /// @param comparator The non-null comparator used to compare stream elements
    /// @return A non-null Gatherer
    public static <INPUT> IncreasingDecreasingComparatorGatherer<INPUT> nonIncreasing(final Comparator<INPUT> comparator) {
        return new IncreasingDecreasingComparatorGatherer<>(IncreasingDecreasingComparatorGatherer.Operation.NonIncreasing, comparator);
    }

    /// Emit elements in the input stream ordered by frequency from least frequently occurring
    /// to most frequently occurring. Elements are emitted wrapped in `WithCount<INPUT>` objects
    /// that carry the element and the number of occurrences.
    ///
    /// Example:
    /// ```
    /// Stream.of("A", "A", "A", "B", "B", "C")
    ///       .gather(orderByFrequencyAscending())
    ///       .toList();
    ///
    /// // Produces:
    ///[WithCount("C", 1), WithCount("B", 2), WithCount("A", 4)]
    ///```
    ///
    /// Note: This consumes the entire stream and holds it in memory, so it will not work on infinite
    /// streams and may cause memory pressure on very large streams.
    ///
    /// @param <INPUT> Type of elements in the input stream
    /// @return A non-null `FrequencyGatherer`
    public static <INPUT extends @Nullable Object> FrequencyGatherer<INPUT> orderByFrequencyAscending() {
        return new FrequencyGatherer<>(FrequencyGatherer.Order.Ascending);
    }

    /// Emit elements in the input stream ordered by frequency from most frequently occurring
    /// to least frequently occurring. Elements are emitted wrapped in `WithCount<INPUT>` objects
    /// that carry the element and the number of occurrences.
    ///
    /// Example:
    /// ```
    /// Stream.of("A", "A", "A", "B", "B", "C")
    ///       .gather(orderByFrequencyDescending())
    ///       .toList();
    ///
    /// // Produces:
    ///[WithCount("A", 4), WithCount("B", 2), WithCount("C", 1)]
    ///```
    ///
    /// Note: This consumes the entire stream and holds it in memory, so it will not work on infinite
    /// streams and may cause memory pressure on very large streams.
    ///
    /// @param <INPUT> Type of elements in the input stream
    /// @return A non-null `FrequencyGatherer`
    public static <INPUT extends @Nullable Object> FrequencyGatherer<INPUT> orderByFrequencyDescending() {
        return new FrequencyGatherer<>(FrequencyGatherer.Order.Descending);
    }

    /// Reverse the order of the input Stream.
    ///
    /// Note: This consumes the entire stream and holds it in memory, so it will not work on infinite
    /// streams and may cause memory pressure on very large streams.
    ///
    /// @param <INPUT> Type of elements in the input stream
    /// @return A non-null `ReversingGatherer`
    public static <INPUT extends @Nullable Object> ReversingGatherer<INPUT> reverse() {
        return new ReversingGatherer<>();
    }

    /// Create a `Stream<BigDecimal>` that represents the running population standard
    /// deviation of a `Stream<BigDecimal>`.
    ///
    /// @return A non-null `BigDecimalStandardDeviationGatherer`
    public static BigDecimalStandardDeviationGatherer<@Nullable BigDecimal> runningPopulationStandardDeviation() {
        return new BigDecimalStandardDeviationGatherer<>(
                BigDecimalStandardDeviationGatherer.Mode.Population,
                Function.identity()
        );
    }

    /// Create a `Stream<BigDecimal>` that represents the running population standard deviation of a `BigDecimal`
    /// objects mapped from a `Stream<BigDecimal>` via a `mappingFunction`.
    ///
    /// @param mappingFunction A function to map `<INPUT>` objects to `BigDecimal`, the results of which will be used
    ///                        in the standard deviation calculation
    /// @param <INPUT>         Type of elements in the input stream, to be remapped to `BigDecimal` by the `mappingFunction`
    /// @return A non-null `BigDecimalStandardDeviationGatherer`
    public static <INPUT extends @Nullable Object> BigDecimalStandardDeviationGatherer<INPUT> runningPopulationStandardDeviationBy(
            final Function<INPUT, BigDecimal> mappingFunction
    ) {
        return new BigDecimalStandardDeviationGatherer<>(
                BigDecimalStandardDeviationGatherer.Mode.Population,
                mappingFunction
        );
    }

    /// Create a `Stream<BigDecimal>` that represents the running product of a `Stream<BigDecimal>`.
    ///
    /// @return A non-null `BigDecimalProductGatherer`
    public static BigDecimalProductGatherer<@Nullable BigDecimal> runningProduct() {
        return new BigDecimalProductGatherer<>(Function.identity());
    }

    /// Create a `Stream<BigDecimal>` that represents the running product of `BigDecimal` objects mapped
    /// from a `Stream<INPUT>` via a `mappingFunction`.
    ///
    /// @param mappingFunction A function to map `<INPUT>` objects to `BigDecimal`, the results of which will be used
    ///                        in the product calculation
    /// @param <INPUT>         Type of elements in the input stream, to be remapped to `BigDecimal` by the `mappingFunction`
    /// @return A non-null `BigDecimalProductGatherer`
    public static <INPUT extends @Nullable Object> BigDecimalProductGatherer<INPUT> runningProductBy(
            final Function<INPUT, BigDecimal> mappingFunction
    ) {
        return new BigDecimalProductGatherer<>(mappingFunction);
    }

    /// Create a `Stream<BigDecimal>` that represents the running sample standard deviation of a `Stream<BigDecimal>`.
    ///
    /// @return A non-null `BigDecimalStandardDeviationGatherer`
    public static BigDecimalStandardDeviationGatherer<@Nullable BigDecimal> runningSampleStandardDeviation() {
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
    /// @param <INPUT>         Type of elements in the input stream, to be remapped to `BigDecimal` by the `mappingFunction`
    /// @return A non-null `BigDecimalStandardDeviationGatherer`
    public static <INPUT extends @Nullable Object> BigDecimalStandardDeviationGatherer<INPUT> runningSampleStandardDeviationBy(
            final Function<INPUT, BigDecimal> mappingFunction
    ) {
        return new BigDecimalStandardDeviationGatherer<>(
                BigDecimalStandardDeviationGatherer.Mode.Sample,
                mappingFunction
        );
    }

    /// Create a `Stream<BigDecimal>` that represents the running sum of a `Stream<BigDecimal>`.
    ///
    /// @return A non-null `BigDecimalSumGatherer`
    public static BigDecimalSumGatherer<@Nullable BigDecimal> runningSum() {
        return new BigDecimalSumGatherer<>(Function.identity());
    }

    /// Create a `Stream<BigDecimal>` that represents the running sum of `BigDecimal` objects mapped
    /// from a `Stream<INPUT>` via a `mappingFunction`.
    ///
    /// @param mappingFunction A function to map `<INPUT>` objects to `BigDecimal`, the results of which will be used
    ///                        in the running sum calculation
    /// @param <INPUT>         Type of elements in the input stream, to be remapped to `BigDecimal` by the `mappingFunction`
    /// @return A non-null `BigDecimalSumGatherer`
    public static <INPUT extends @Nullable Object> BigDecimalSumGatherer<INPUT> runningSumBy(
            final Function<INPUT, BigDecimal> mappingFunction
    ) {
        return new BigDecimalSumGatherer<>(mappingFunction);
    }

    ///  Perform a scan over every element in the input stream along with its index
    ///
    /// @param <INPUT>      Type of elements in the input stream
    /// @param <OUTPUT>     Type elements are accumulated to
    /// @param initialValue Initial value of the scan
    /// @param scanFunction Function that performs the accumulation given an element, its index, and the carry value
    /// @return A non-null AccumulatingGatherer
    public static <INPUT extends @Nullable Object, OUTPUT extends @Nullable Object> AccumulatingGatherer<INPUT, OUTPUT> scanIndexed(
            final Supplier<OUTPUT> initialValue,
            final IndexedAccumulatorFunction<OUTPUT, INPUT, OUTPUT> scanFunction
    ) {
        return new AccumulatingGatherer<>(true, initialValue, scanFunction);
    }

    /// Shuffle the input stream into a random order.
    ///
    /// Note: This consumes the entire stream and holds it in memory, so it will not work on infinite
    /// streams and may cause memory pressure on very large streams.
    ///
    /// @param <INPUT> Type of elements in the input stream
    /// @return A non-null `ShufflingGatherer`
    public static <INPUT extends @Nullable Object> ShufflingGatherer<INPUT> shuffle() {
        return new ShufflingGatherer<>(RandomGenerator.getDefault());
    }

    /// Shuffle the input stream into a random order.
    ///
    /// Note: This consumes the entire stream and holds it in memory, so it will not work on infinite
    /// streams and may cause memory pressure on very large streams.
    ///
    /// @param randomGenerator A non-null `RandomGenerator` to use as a random source for the shuffle
    /// @param <INPUT>         Type of elements in the input stream
    /// @return A non-null `ShufflingGatherer`
    public static <INPUT extends @Nullable Object> ShufflingGatherer<INPUT> shuffle(final RandomGenerator randomGenerator) {
        return new ShufflingGatherer<>(randomGenerator);
    }

    /// Create a Stream that represents the simple moving average of a `Stream<BigDecimal>` looking
    /// back `windowSize` number of elements.
    ///
    /// @param windowSize The number of elements to average, must be greater than 1.
    /// @return A non-null `BigDecimalSimpleMovingAverageGatherer`
    public static BigDecimalSimpleMovingAverageGatherer<@Nullable BigDecimal> simpleMovingAverage(final int windowSize) {
        return new BigDecimalSimpleMovingAverageGatherer<>(windowSize, Function.identity());
    }

    /// Create a Stream that represents the simple moving average of a `BigDecimal` objects mapped from a `Stream<T>`
    /// via a `mappingFunction` and looking back `windowSize` number of elements.
    ///
    /// @param windowSize      The number of elements to average, must be greater than 1.
    /// @param mappingFunction A function to map `<INPUT>` objects to `BigDecimal`, the results of which will be used
    ///                        in the moving average calculation
    /// @param <INPUT>         Type of elements in the input stream, to be remapped to `BigDecimal` by the `mappingFunction`
    /// @return A non-null `BigDecimalSimpleMovingAverageGatherer`
    public static <INPUT extends @Nullable Object> BigDecimalSimpleMovingAverageGatherer<INPUT> simpleMovingAverageBy(
            final int windowSize,
            final Function<INPUT, BigDecimal> mappingFunction
    ) {
        return new BigDecimalSimpleMovingAverageGatherer<>(windowSize, mappingFunction);
    }

    /// Create a Stream that is the running average of `Stream<BigDecimal>`
    ///
    /// @return BigDecimalSimpleAverageGatherer
    public static BigDecimalSimpleAverageGatherer<@Nullable BigDecimal> simpleRunningAverage() {
        return simpleRunningAverageBy(Function.identity());
    }

    /// Create a Stream that is the running average of `BigDecimal` objects as mapped by
    /// the given function. This is useful when paired with the `withOriginal` function.
    ///
    /// @param mappingFunction A function to map `<INPUT>` objects to `BigDecimal`, the results of which will be used
    ///                        in the running average calculation
    /// @param <INPUT>         Type of elements in the input stream, to be remapped to `BigDecimal` by the `mappingFunction`
    /// @return A non-null `BigDecimalSimpleAverageGatherer`
    public static <INPUT extends @Nullable Object> BigDecimalSimpleAverageGatherer<INPUT> simpleRunningAverageBy(
            final Function<INPUT, BigDecimal> mappingFunction
    ) {
        return new BigDecimalSimpleAverageGatherer<>(mappingFunction);
    }

    /// Ensure the input stream is exactly `size` elements long, and emit all elements if so.
    /// If not, throw an `IllegalStateException`.
    ///
    /// @param size    Exact number of elements the stream must have
    /// @param <INPUT> Type of elements in both the input and output streams
    /// @return A non-null `SizeGatherer`
    /// @throws IllegalStateException when the input stream is not exactly `size` elements long
    public static <INPUT extends @Nullable Object> SizeGatherer<INPUT> sizeExactly(final long size) {
        return new SizeGatherer<>(SizeGatherer.Operation.Equals, size);
    }

    /// Ensure the input stream is greater than `size` elements long, and emit all elements if so.
    /// If not, throw an `IllegalStateException`.
    ///
    /// @param size    The size the stream must be longer than
    /// @param <INPUT> Type of elements in both the input and output streams
    /// @return A non-null `SizeGatherer`
    /// @throws IllegalStateException when the input stream is not exactly `size` elements long
    public static <INPUT extends @Nullable Object> SizeGatherer<INPUT> sizeGreaterThan(final long size) {
        return new SizeGatherer<>(SizeGatherer.Operation.GreaterThan, size);
    }

    /// Ensure the input stream is greater than or equal to `size` elements long, and emit all elements if so.
    /// If not, throw an `IllegalStateException`.
    ///
    /// @param size    The minimum size of the stream
    /// @param <INPUT> Type of elements in both the input and output streams
    /// @return A non-null `SizeGatherer`
    /// @throws IllegalStateException when the input stream is not exactly `size` elements long
    public static <INPUT extends @Nullable Object> SizeGatherer<INPUT> sizeGreaterThanOrEqualTo(final long size) {
        return new SizeGatherer<>(SizeGatherer.Operation.GreaterThanOrEqualTo, size);
    }

    /// Ensure the input stream is less than `size` elements long, and emit all elements if so.
    /// If not, throw an `IllegalStateException`.
    ///
    /// @param size    The size the stream must be shorter than
    /// @param <INPUT> Type of elements in both the input and output streams
    /// @return A non-null `SizeGatherer`
    /// @throws IllegalStateException when the input stream is not exactly `size` elements long
    public static <INPUT extends @Nullable Object> SizeGatherer<INPUT> sizeLessThan(final long size) {
        return new SizeGatherer<>(SizeGatherer.Operation.LessThan, size);
    }

    /// Ensure the input stream is less than or equal to `size` elements long, and emit all elements if so.
    /// If not, throw an `IllegalStateException`.
    ///
    /// @param size    The maximum size the stream
    /// @param <INPUT> Type of elements in both the input and output streams
    /// @return A non-null `SizeGatherer`
    /// @throws IllegalStateException when the input stream is not exactly `size` elements long
    public static <INPUT extends @Nullable Object> SizeGatherer<INPUT> sizeLessThanOrEqualTo(final long size) {
        return new SizeGatherer<>(SizeGatherer.Operation.LessThanOrEqualTo, size);
    }

    /// Take elements from the input stream until the `predicate` is met, including the first element that
    /// matches the `predicate`.
    ///
    /// @param predicate A non-null predicate function
    /// @param <INPUT>   Type of elements in both the input and output streams
    /// @return A non-null `TakeUntilGatherer`
    public static <INPUT extends @Nullable Object> TakeUntilGatherer<INPUT> takeUntil(
            final Predicate<INPUT> predicate
    ) {
        return new TakeUntilGatherer<>(predicate);
    }

    /// Limit the number of elements in the stream to some number per period. When the limit is reached,
    /// consumption is paused until a new period starts and the count resets.
    ///
    /// @param amount   A positive number of elements to allow per period
    /// @param duration A positive duration for the length of the period
    /// @param <INPUT>  Type of elements in the input stream
    /// @return A non-null `ThrottlingGatherer`
    public static <INPUT extends @Nullable Object> ThrottlingGatherer<INPUT> throttle(
            final int amount,
            final Duration duration
    ) {
        return new ThrottlingGatherer<>(ThrottlingGatherer.LimitRule.Pause, amount, duration);
    }

    /// Emit only those elements that occur in the input stream a single time.
    ///
    /// @param <INPUT> Type of elements in the input stream
    /// @return A non-null `UniquelyOccurringGatherer`
    public static <INPUT extends @Nullable Object> UniquelyOccurringGatherer<INPUT> uniquelyOccurring() {
        return new UniquelyOccurringGatherer<>();
    }

    /// Create windows over the elements of the input stream that are `windowSize` in length, sliding over `stepping` number of elements
    /// and optionally including partial windows at the end of ths stream.
    ///
    /// @param <INPUT> Type of elements in the input and output stream
    /// @param windowSize Size of the window, must be greater than 0
    /// @param stepping Number of elements to slide over each time a window has filled, must be greater than 0
    /// @param includePartials To include left-over partial windows at the end of the stream or not
    /// @return A non-null `WindowedGatherer`
    public static <INPUT extends @Nullable Object> WindowedGatherer<INPUT> windowed(int windowSize, int stepping, boolean includePartials) {
        return new WindowedGatherer<>(windowSize, stepping, includePartials);
    }

    /// Maps all elements of the stream as-is along with their 0-based index.
    ///
    /// @param <INPUT> Type of elements in the input stream
    /// @return A non-null `IndexingGatherer`
    public static <INPUT extends @Nullable Object> IndexingGatherer<INPUT> withIndex() {
        return new IndexingGatherer<>();
    }

    /// Creates a stream of `Pair<FIRST,SECOND>` objects whose values come from the stream this is called on
    /// and the argument collection
    ///
    /// @param other    A non-null iterable to zip with
    /// @param <FIRST>  Type of object in the source stream
    /// @param <SECOND> Type of object in the argument `Iterable`
    /// @return A non-null `ZipWithGatherer`
    public static <FIRST extends @Nullable Object, SECOND extends @Nullable Object> ZipWithGatherer<FIRST, SECOND> zipWith(
            final Iterable<SECOND> other
    ) {
        return new ZipWithGatherer<>(other);
    }

    /// Creates a stream of `Pair<FIRST,SECOND>` objects whose values come from the stream this is called on
    /// and the argument iterator
    ///
    /// @param other    A non-null iterator to zip with
    /// @param <FIRST>  Type of object in the source stream
    /// @param <SECOND> Type of object in the argument `Iterator`
    /// @return A non-null `ZipWithGatherer`
    public static <FIRST extends @Nullable Object, SECOND extends @Nullable Object> ZipWithGatherer<FIRST, SECOND> zipWith(
            final Iterator<SECOND> other
    ) {
        return new ZipWithGatherer<>(other);
    }

    /// Creates a stream of `Pair<FIRST,SECOND>` objects whose values come from the stream this is called on
    /// and the argument stream
    ///
    /// @param other    A non-null stream to zip with
    /// @param <FIRST>  Type of object in the source stream
    /// @param <SECOND> Type of object in the argument `Stream`
    /// @return A non-null `ZipWithGatherer`
    public static <FIRST extends @Nullable Object, SECOND extends @Nullable Object> ZipWithGatherer<FIRST, SECOND> zipWith(
            final Stream<SECOND> other
    ) {
        return new ZipWithGatherer<>(other);
    }

    /// Creates a stream of `List` objects which contain each two adjacent elements in the input stream.
    ///
    /// @param <INPUT> Type of elements in the input stream
    /// @return A non-null `ZipWithNextGatherer`
    public static <INPUT extends @Nullable Object> ZipWithNextGatherer<INPUT> zipWithNext() {
        return new ZipWithNextGatherer<>();
    }
}
