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

import com.ginsberg.gatherers4j.enums.Frequency;
import com.ginsberg.gatherers4j.enums.Order;
import com.ginsberg.gatherers4j.enums.Rotate;
import com.ginsberg.gatherers4j.enums.Size;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;
import java.util.stream.Gatherer;
import java.util.stream.Stream;

import static com.ginsberg.gatherers4j.GathererUtils.equalityOnlyComparator;
import static com.ginsberg.gatherers4j.GathererUtils.mustNotBeNull;

/// This is the main entry-point for the Gatherers4j library. All available gatherers
/// are created from static methods on this class.
public abstract class Gatherers4j {

    private Gatherers4j() {
        // No
    }

    /// Cross every element of the input stream with every element of the given `Iterable`, emitting them
    /// to the output stream as a `Pair<INPUT, CROSS>`.
    ///
    /// @param <INPUT> Type of element in the input stream
    /// @param <CROSS> Type of element in the crossWith `Iterable`
    /// @param source The Iterable to source with
    /// @return A non-null Gatherer
    public static <INPUT extends @Nullable Object, CROSS extends @Nullable Object> Gatherer<INPUT, ?, Pair<INPUT, CROSS>> crossWith(
            final Iterable<CROSS> source
    ) {
        return CrossGatherer.of(source);
    }

    /// Cross every element of the input stream with every element of the given `Iterator`, emitting them
    /// to the output stream as a `Pair<INPUT, CROSS>`.
    ///
    /// Note: the Iterator is consumed fully and stored as a List in memory.
    ///
    /// @param <INPUT> Type of element in the input stream
    /// @param <CROSS> Type of element in the crossWith `Iterator`
    /// @param source The Iterator to cross with
    /// @return A non-null Gatherer
    public static <INPUT extends @Nullable Object, CROSS extends @Nullable Object> Gatherer<INPUT, ?, Pair<INPUT, CROSS>> crossWith(
            final Iterator<CROSS> source
    ) {
        return CrossGatherer.of(source);
    }

    /// Cross every element of the input stream with every element of the given `Stream`, emitting them
    /// to the output stream as a `Pair<INPUT, CROSS>`.
    ///
    /// Note: the Iterator is consumed fully and stored as a List in memory.
    /// Note: The Ghostbusters warned us about this and I hereby absolve myself of any responsibility if you cause some kind of cataclysm.
    ///
    /// @param <INPUT> Type of element in the input stream
    /// @param <CROSS> Type of element in the crossWith `Iterator`
    /// @param source The Stream to cross with
    /// @return A non-null Gatherer
    public static <INPUT extends @Nullable Object, CROSS extends @Nullable Object> Gatherer<INPUT, ?, Pair<INPUT, CROSS>> crossWith(
            final Stream<CROSS> source
    ) {
        return CrossGatherer.of(source);
    }

    /// Cross every element of the input stream with every element provided, emitting them
    /// to the output stream as a `Pair<INPUT, CROSS>`.
    ///
    /// Note: the Iterator is consumed fully and stored as a List in memory.
    /// Note: The Ghostbusters warned us about this and I hereby absolve myself of any responsibility if you cause some kind of cataclysm.
    ///
    /// @param <INPUT> Type of element in the input stream
    /// @param <CROSS> Type of element in the crossWith `Iterator`
    /// @param source Elements to cross with the input stream
    /// @return A non-null Gatherer
    @SafeVarargs
    public static <INPUT extends @Nullable Object, CROSS extends @Nullable Object> Gatherer<INPUT, ?, Pair<INPUT, CROSS>> crossWith(
            final CROSS... source
    ) {
        return CrossGatherer.of(source);
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

    /// Remove consecutive duplicate elements from a stream as measured by `Object.equals(Object)`
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

    /// Drop every nth element of the stream.
    ///
    /// @param count   The number of the elements to drop, must be at least 2
    /// @param <INPUT> Type of elements in both the input and output streams
    /// @return A non-null `Gatherer`
    public static <INPUT extends @Nullable Object> Gatherer<INPUT, ?, INPUT> dropEveryNth(final int count) {
        if (count < 2) {
            throw new IllegalArgumentException("Count must be a minimum of 2");
        }
        return filterIndexed((index, _) -> index % count != 0);
    }

    /// Keep all elements except the last `count` elements of the stream.
    ///
    /// @param count   A positive number of elements to drop from the end of the stream
    /// @param <INPUT> Type of elements in both the input and output streams
    /// @return A non-null `DropLastGatherer`
    public static <INPUT extends @Nullable Object> DropLastGatherer<INPUT> dropLast(final int count) {
        return new DropLastGatherer<>(count);
    }

    /// Ensure that the `Comparable` elements in the input stream are in the given `Order`, and fail exceptionally if they are not.
    ///
    /// @param <INPUT> Type of elements in the input stream
    /// @param order The non-null order the stream must be in.
    /// @return A non-null Gatherer
    public static <INPUT extends Comparable<INPUT>> Gatherer<INPUT, ?, INPUT> ensureOrdered(final Order order) {
        final Gatherer<INPUT, ?, List<INPUT>> generic = GroupChangingGatherer.usingComparable(order);
        return generic.andThen(new FlattenSingleOrFail<>("Elements not in proper order: " + order.name()));
    }

    /// Ensure that the elements in the input stream are in the given `Order` as measured by the given `Comparator`, and fail exceptionally if they are not.
    ///
    /// @param <INPUT> Type of elements in the input stream
    /// @param order The non-null order the stream must be in.
    /// @param comparator The non-null comparator used to compare stream elements
    /// @return A non-null Gatherer
    public static <INPUT> Gatherer<INPUT, ?, INPUT> ensureOrderedBy(final Order order, final Comparator<INPUT> comparator) {
        return GroupChangingGatherer.usingComparator(order, comparator)
                .andThen(new FlattenSingleOrFail<>("Elements not in proper order: " + order.name()));
    }

    /// Ensure the input stream's meets the given `size` criteria, and emit all elements if so.
    /// If not, throw an `IllegalStateException`.
    ///
    /// @param size The Size to measure the stream length against
    /// @param length    Number to compare stream length against
    /// @param <INPUT> Type of elements in both the input and output streams
    /// @return A non-null `SizeGatherer`
    /// @throws IllegalStateException when the input stream is not exactly `size` elements long
    public static <INPUT extends @Nullable Object> SizeGatherer<INPUT> ensureSize(final Size size, final long length) {
        return new SizeGatherer<>(size, length);
    }

    /// Filter a stream according to the given `predicate`, which takes both the item being examined,
    /// and its index.
    ///
    /// @param predicate A non-null `BiPredicate<Long,INPUT>` where the `Long` is the zero-based index of the element
    ///                  being filtered, and the `INPUT` is the element itself.
    /// @param <INPUT>   Type of elements in the input stream
    /// @return A non-null `Gatherer`
    public static <INPUT extends @Nullable Object> Gatherer<INPUT, ?, INPUT> filterIndexed(
            final BiPredicate<Long, INPUT> predicate
    ) {
        return new FilteringWithIndexGatherer<>(predicate);
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

    /// Filter the input stream so that it contains `Comparable` elements in the `order` specified. Anything not matching
    /// that order is removed as it is encountered.
    ///
    /// @param <INPUT> Type of elements in the input and output stream
    /// @return A non-null gatherer
    public static <INPUT extends Comparable<INPUT>> Gatherer<INPUT, ?, INPUT> filterOrdered(final Order order) {
        return FilterChangingGatherer.usingComparable(order);
    }

    /// Filter the input stream so that it contains elements in the `order` specified as measured by the given `Comparator`.
    /// Anything not matching that order is removed as it is encountered.
    ///
    /// @param <INPUT> Type of elements in the input and output stream
    /// @param comparator A non-null `Comparator` to compare stream elements
    /// @return A non-null gatherer
    public static <INPUT> Gatherer<INPUT, ?, INPUT> filterOrderedBy(final Order order, final Comparator<INPUT> comparator) {
        return FilterChangingGatherer.usingComparator(order, comparator);
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

    /// Turn a `Stream<INPUT>` into a `Stream<List<INPUT>>` where adjacent equal elements are in the same `List`
    /// and equality is measured by `Object.equals(Object)`. The lists emitted to the output stream are unmodifiable.
    ///
    /// @param <INPUT> Type of elements in the input stream
    /// @return A non-null `GroupingByGatherer`
    public static <INPUT extends @Nullable Object> Gatherer<INPUT, ?, List<INPUT>> group() {
        return GroupChangingGatherer.usingComparator(Order.Equal, equalityOnlyComparator());
    }

    /// Turn a `Stream<INPUT>` into a `Stream<List<INPUT>>` where adjacent equal elements are in the same `List`
    /// and equality is measured by the given `mappingFunction`. The lists emitted to the output stream are unmodifiable.
    ///
    /// @param mappingFunction A non-null function, the results of which are used to measure equality of consecutive elements.
    /// @param <INPUT>         Type of elements in the input stream
    /// @return A non-null `GroupingByGatherer`
    public static <INPUT extends @Nullable Object> Gatherer<INPUT, ?, List<INPUT>> groupBy(
            final Function<@Nullable INPUT, @Nullable Object> mappingFunction
    ) {
        mustNotBeNull(mappingFunction, "mappingFunction must not be null");
        return GroupChangingGatherer.usingComparator(Order.Equal, equalityOnlyComparator(mappingFunction));
    }

    /// Turn a `Stream<Comparable>` into a `Stream<List<>>` where adjacent equal elements are in the same `List`
    /// and order is measured by the order imposed by the `Comparable`. The lists emitted to the output stream are unmodifiable.
    ///
    /// @param <INPUT> Type of elements in the input stream, implementing `Comparable`
    /// @return A non-null `Gatherer`
    public static <INPUT extends @Nullable Comparable<INPUT>> Gatherer<INPUT, ?, List<INPUT>> groupOrdered(final Order order) {
        return GroupChangingGatherer.usingComparable(order);
    }

    /// Turn a `Stream<INPUT>` into a `Stream<List<INPUT>>` where adjacent equal elements are in the same `List`
    /// and order is measured by the given `Comparator`. The lists emitted to the output stream are unmodifiable.
    ///
    /// @param comparator A non-null function, the results of which are used to measure equality of consecutive elements.
    /// @param <INPUT>         Type of elements in the input stream
    /// @return A non-null `Gatherer`
    public static <INPUT extends @Nullable Object> Gatherer<INPUT, ?, List<INPUT>> groupOrderedBy(
            final Order order,
            final Comparator<INPUT> comparator
    ) {
        return GroupChangingGatherer.usingComparator(order, comparator);
    }

    /// Creates a stream of alternating objects from the input stream and the argument iterable
    ///
    /// @param other   A non-null Iterable to interleave
    /// @param <INPUT> Type of elements in both the input stream and argument iterable
    /// @return A non-null `InterleavingGatherer`
    public static <INPUT extends @Nullable Object> InterleavingGatherer<INPUT> interleaveWith(final Iterable<INPUT> other) {
        return new InterleavingGatherer<>(other);
    }

    /// Creates a stream of alternating objects from the input stream and the argument iterator
    ///
    /// @param other   A non-null Iterator to interleave
    /// @param <INPUT> Type of elements in both the input stream and argument iterator
    /// @return A non-null `InterleavingGatherer`
    public static <INPUT extends @Nullable Object> InterleavingGatherer<INPUT> interleaveWith(final Iterator<INPUT> other) {
        return new InterleavingGatherer<>(other);
    }

    /// Creates a stream of alternating objects from the input stream and the argument stream
    ///
    /// @param other   A non-null stream to interleave
    /// @param <INPUT> Type of elements in both the input and argument streams
    /// @return A non-null `InterleavingGatherer`
    public static <INPUT extends @Nullable Object> InterleavingGatherer<INPUT> interleaveWith(final Stream<INPUT> other) {
        return new InterleavingGatherer<>(other);
    }

    /// Creates a stream of alternating objects from the input stream and the provided elements
    ///
    /// @param other   Non-null elements to interleave
    /// @param <INPUT> Type of elements in both the input stream and argument iterator
    /// @return A non-null `InterleavingGatherer`
    @SafeVarargs
    public static <INPUT extends @Nullable Object> InterleavingGatherer<INPUT> interleaveWith(final INPUT... other) {
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

    /// Note: This consumes the entire stream and holds it in memory, so it will not work on infinite
    /// streams and may cause memory pressure on very large streams.
    ///
    /// @param <INPUT> Type of elements in the input stream
    /// @return A non-null `Gatherer`
    public static <INPUT extends @Nullable Object> Gatherer<INPUT, ?, WithCount<INPUT>> orderByFrequency(final Frequency order) {
        return new FrequencyGatherer<>(order);
    }

    /// Emit elements in the input stream ordered by frequency in the direction specified. Elements are emitted wrapped
    /// in `WithCount<INPUT>` objects that carry the element and the number of occurrences.
    ///
    /// Repeatedly emit the input stream to the output stream a given number of times.
    /// Note: This implementation consumes the entire input stream into memory, so it must be used on finite streams.
    ///
    /// @param <INPUT> Type of elements in the input and output stream
    /// @param repeats Number of repeats, must be greater than 1
    /// @return A non-null `RepeatingGatherer`
    public static <INPUT extends @Nullable Object> RepeatingGatherer<INPUT> repeat(final int repeats) {
        return RepeatingGatherer.ofFinite(repeats);
    }

    /// Repeatedly emit the input stream to the output stream infinitely.
    /// Note: This implementation consumes the entire input stream into memory, so it must be used on finite streams.
    ///
    /// @param <INPUT> Type of elements in the input and output stream
    /// @return A non-null `RepeatingGatherer`
    public static <INPUT extends @Nullable Object> RepeatingGatherer<INPUT> repeatInfinitely() {
        return RepeatingGatherer.ofInfinite();
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

    /// Consume the entire stream and emit its elements rotated in the direction specified `distance` number of spaces
    ///
    /// @param <INPUT> Type of elements in the input and output stream
    /// @param direction Which direction to rotate the stream in
    /// @param distance Distance to rotate elements
    /// @return A non-null RotateGatherer
    public static <INPUT extends @Nullable Object> RotateGatherer<INPUT> rotate(final Rotate direction, final int distance) {
        return new RotateGatherer<>(direction, distance);
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

    /// Take every nth element of the stream.
    ///
    /// @param count   The number of the elements to keep, must be at least 2
    /// @param <INPUT> Type of elements in both the input and output streams
    /// @return A non-null `Gatherer`
    public static <INPUT extends @Nullable Object> Gatherer<INPUT, ?, INPUT> takeEveryNth(final int count) {
        if (count < 2) {
            throw new IllegalArgumentException("Count must be a minimum of 2");
        }
        return filterIndexed((index, _) -> index % count == 0);
    }

    /// Emit the last `count` elements from the stream. If there are fewer than `count` elements they are all emitted.
    ///
    /// @param count   A non-negative integer, the number of elements to return
    /// @param <INPUT> Type of elements in the input stream
    /// @return A non-null `LastGatherer`
    public static <INPUT> LastGatherer<INPUT> takeLast(final int count) {
        return new LastGatherer<>(count);
    }

    /// Take elements from the input stream until the `predicate` is met, including the first element that
    /// matches the `predicate`.
    ///
    /// @param predicate A non-null predicate function
    /// @param <INPUT>   Type of elements in both the input and output streams
    /// @return A non-null `Gatherer`
    public static <INPUT extends @Nullable Object> Gatherer<INPUT, ?, INPUT> takeUntil(
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
    /// @return A non-null `Gatherer`
    public static <INPUT extends @Nullable Object> Gatherer<INPUT, ?, INPUT> uniquelyOccurring() {
        return new UniquelyOccurringGatherer<>();
    }

    /// Create windows over the elements of the input stream that are `windowSize` in length, sliding over `stepping` number of elements
    /// and optionally including partial windows at the end of ths stream.
    ///
    /// @param <INPUT> Type of elements in the input and output stream
    /// @param windowSize Size of the window, must be greater than 0
    /// @param stepping Number of elements to slide over each time a window has filled, must be greater than 0
    /// @param includePartials To include left-over partial windows at the end of the stream or not
    /// @return A non-null `WindowGatherer`
    public static <INPUT extends @Nullable Object> WindowGatherer<INPUT> window(int windowSize, int stepping, boolean includePartials) {
        return new WindowGatherer<>(windowSize, stepping, includePartials);
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

    /// Creates a stream of `Pair<FIRST,SECOND>` objects whose values come from the stream this is called on
    /// and the argument elements provide as a varargs
    ///
    /// @param other    A non-zero number of elements to zip with
    /// @param <FIRST>  Type of object in the source stream
    /// @param <SECOND> Type of object in the argument `Stream`
    /// @return A non-null `ZipWithGatherer`
    @SafeVarargs
    public static <FIRST extends @Nullable Object, SECOND extends @Nullable Object> ZipWithGatherer<FIRST, SECOND> zipWith(
            final SECOND... other
    ) {
        return new ZipWithGatherer<>(other);
    }

    /// Creates a stream of `List` objects which contain each two adjacent elements in the input stream.
    ///
    /// @param <INPUT> Type of elements in the input stream
    /// @return A non-null `Gatherer`
    public static <INPUT extends @Nullable Object> Gatherer<INPUT, ?, List<INPUT>> zipWithNext() {
        return new ZipWithNextGatherer<>();
    }
}
