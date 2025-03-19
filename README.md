# Gatherers4j

A library of useful [Stream Gatherers](https://openjdk.org/jeps/485) (custom intermediate operations) for Java 24+.

See [the full set of documentation](https://tginsberg.github.io/gatherers4j/) for information on how to use Gatherers4j.

# Installing

To use this library, add it as a dependency to your build. This library has one transitive
dependency - the [JSpecify](https://jspecify.dev/) set of annotations for static analysis tools.

**Maven**

Add the following dependency to `pom.xml`.

```xml

<dependency>
    <groupId>com.ginsberg</groupId>
    <artifactId>gatherers4j</artifactId>
    <version>0.10.0</version>
</dependency>
```

**Gradle**

Add the following dependency to `build.gradle` or `build.gradle.kts`

```groovy
implementation("com.ginsberg:gatherers4j:0.10.0")
```


# Gatherers In This Library

For convenience, the full list of gatherers in this library are broken into five categories:

1. [Sequence Operations](#sequence-operations)
2. [Filtering and Selection](#filtering-and-selection)
3. [Grouping and Windowing](#grouping-and-windowing)
4. [Validation and Constraints](#validation-and-constraints)
5. [Mathematical Operations](#mathematical-operations)

## Sequence Operations

Gatherers that reorder, combine, or manipulate the sequence of elements.

| Function                                                                                                        | Purpose                                                                                                                                             |
|-----------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------|
| [`crossWith()`](https://tginsberg.github.io/gatherers4j/gatherers/sequence-operations/crosswith/)               | Emit each element of the source stream with each element of the given `iterable`, `iterator`, `stream`, or varargs as a `Pair` to the output stream |
| [`foldIndexed(fn)`](https://tginsberg.github.io/gatherers4j/gatherers/sequence-operations/foldindexed/)         | Perform a fold over the input stream where each element is included along with its zero-based index                                                 |
| [`interleaveWith()`](https://tginsberg.github.io/gatherers4j/gatherers/sequence-operations/interleavewith/)     | Creates a stream of alternating objects from the input stream and the argument `iterable`, `iterator`, `stream`, or varargs                         |
| [`mapIndexed()`](https://tginsberg.github.io/gatherers4j/gatherers/sequence-operations/mapindexed/)             | Perform a mapping operation given the element being mapped and its zero-based index.                                                                |
| [`orderByFrequency()`](https://tginsberg.github.io/gatherers4j/gatherers/sequence-operations/orderbyfrequency/) | Returns a stream where elements are ordered in either ascending or descending frequency contained in `WithCount<T>` wrapper objects.                |
| [`peekIndexed()`](https://tginsberg.github.io/gatherers4j/gatherers/sequence-operations/peekindexed/)           | Peek at each element of the stream along with its zero-based index                                                                                  |
| [`repeat(n)`](https://tginsberg.github.io/gatherers4j/gatherers/sequence-operations/repeat/)                    | Repeat the input stream `n` times to the output stream                                                                                              |
| [`repeatInfinitely()`](https://tginsberg.github.io/gatherers4j/gatherers/sequence-operations/repeatinfinitely/) | Repeat the input stream to the output stream forever (or until some downstream operation stops it)                                                  |
| [`reverse()`](https://tginsberg.github.io/gatherers4j/gatherers/sequence-operations/reverse/)                   | Reverse the order of the stream                                                                                                                     |
| [`rotate(direction, n)`](https://tginsberg.github.io/gatherers4j/gatherers/sequence-operations/rotate/)         | Rotate the stream `n` elements to direction specified. Stores entire stream into memory.                                                            |
| [`scanIndexed(fn)`](https://tginsberg.github.io/gatherers4j/gatherers/sequence-operations/scanindexed/)         | Performs a scan on the input stream using the given function, and includes the index of the elements                                                |
| [`shuffle()`](https://tginsberg.github.io/gatherers4j/gatherers/sequence-operations/shuffle/)                   | Shuffle the stream into a random order, optionally taking a `RandomGenerator`                                                                       |
| [`throttle(amount, duration)`](https://tginsberg.github.io/gatherers4j/gatherers/sequence-operations/throttle/) | Limit stream elements to `amount` elements over `duration`, pausing until a new `duration` period starts                                            |
| [`withIndex()`](https://tginsberg.github.io/gatherers4j/gatherers/sequence-operations/withindex/)               | Maps all elements of the stream as-is along with their 0-based index                                                                                |
| [`zipWith()`](https://tginsberg.github.io/gatherers4j/gatherers/sequence-operations/zipwith/)                   | Creates a stream of `Pair` objects whose values come from the input stream and argument `iterable`, `iterator`, `stream`, or varargs                |
| [`zipWithNext()`](https://tginsberg.github.io/gatherers4j/gatherers/sequence-operations/zipwithnext/)           | Creates a stream of `List` objects via a sliding window of width 2 and stepping 1                                                                   |      

## Filtering and Selection

Gatherers that select or remove elements based on some criteria.

| Function                                                                                                                           | Purpose                                                                                                                        |
|------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------|
| [`debounce(amount, duration)`](https://tginsberg.github.io/gatherers4j/gatherers/filtering-and-selection/debounce/)                | Limit stream elements to `amount` elements over `duration`, dropping any elements over the limit until a new `duration` starts |
| [`dedupeConsecutive()`](https://tginsberg.github.io/gatherers4j/gatherers/filtering-and-selection/dedupeconsecutive/)              | Remove consecutive duplicates from a stream                                                                                    |
| [`dedupeConsecutiveBy(fn)`](https://tginsberg.github.io/gatherers4j/gatherers/filtering-and-selection/dedupeconsecutiveby/)        | Remove consecutive duplicates from a stream as returned by `fn`                                                                |
| [`distinctBy(fn)`](https://tginsberg.github.io/gatherers4j/gatherers/filtering-and-selection/distinctby/)                          | Emit only distinct elements from the stream, as measured by `fn`                                                               |
| [`dropEveryNth(n)`](https://tginsberg.github.io/gatherers4j/gatherers/filtering-and-selection/dropeverynth/)                       | Drop every`n`<sup>th</sup> element from the input stream                                                                       |
| [`dropLast(n)`](https://tginsberg.github.io/gatherers4j/gatherers/filtering-and-selection/droplast/)                               | Keep all but the last `n` elements of the stream                                                                               |
| [`filterIndexed()`](https://tginsberg.github.io/gatherers4j/gatherers/filtering-and-selection/filterindexed/)                      | Filter a stream according to a given predicate, which takes both the item being examined and its zero-based index.             |
| [`filterInstanceOf`](https://tginsberg.github.io/gatherers4j/gatherers/filtering-and-selection/filterinstanceof/)                  | Filter the elements in the stream to only include elements of the given types.                                                 |
| [`filterOrdered(order)`](https://tginsberg.github.io/gatherers4j/gatherers/filtering-and-selection/filterordered/)                 | Filter the input stream of `Comparable` objects so that is strictly in the given `order`                                       |                                                                                           |
| [`filterOrderedBy(order, comparator)`](https://tginsberg.github.io/gatherers4j/gatherers/filtering-and-selection/filterorderedby/) | Filter the input stream of objects so that it contains only elements in the given `order`, as measured by a given `Comparator` |
| [`sampleFixedSize(n)`](https://tginsberg.github.io/gatherers4j/gatherers/filtering-and-selection/samplefixedsize/)                 | Perform a fixed size sampling over the input stream.                                                                           |
| [`samplePercentage(d)`](https://tginsberg.github.io/gatherers4j/gatherers/filtering-and-selection/samplepercentage/)               | Perform a percentage-based sampling over the input stream.                                                                     |                                                                                                                               | 
| [`takeEveryNth(n)`](https://tginsberg.github.io/gatherers4j/gatherers/filtering-and-selection/takeeverynth/)                       | Keep every`n`<sup>th</sup> element from the input stream                                                                       |
| [`takeLast(n)`](https://tginsberg.github.io/gatherers4j/gatherers/filtering-and-selection/takelast/)                               | Emit the last `n` values                                                                                                       |
| [`takeUntil(predicate)`](https://tginsberg.github.io/gatherers4j/gatherers/filtering-and-selection/takeuntil/)                     | Take elements from the input stream until the `predicate` is met, including the first element that matches the `preciate`      |
| [`uniquelyOccurring()`](https://tginsberg.github.io/gatherers4j/gatherers/filtering-and-selection/uniquelyoccurring/)              | Emit elements that occur a single time, dropping all others                                                                    |

## Grouping and Windowing

Functions that group input elements by varying criteria.

| Function                                                                                                                        | Purpose                                                                                                                                                                                             |
|---------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [`group()`](https://tginsberg.github.io/gatherers4j/gatherers/grouping-and-windowing/group/)                                    | Group adjacent equal elements into lists                                                                                                                                                            |
| [`groupBy(fn)`](https://tginsberg.github.io/gatherers4j/gatherers/grouping-and-windowing/groupby/)                              | Group adjacent elements that are equal according to `fn` into lists                                                                                                                                 | 
| [`groupOrdered(order)`](https://tginsberg.github.io/gatherers4j/gatherers/grouping-and-windowing/groupordered/)                 | Group `Comparable` elements in the input stream to lists in the given order                                                                                                                         |
| [`groupOrderedBy(order, comparator)`](https://tginsberg.github.io/gatherers4j/gatherers/grouping-and-windowing/grouporderedby/) | Group elements in the given `order` as measured by a `Comparator` to lists                                                                                                                          |
| [`window()`](https://tginsberg.github.io/gatherers4j/gatherers/grouping-and-windowing/window/)                                  | Create windows over the elements of the input stream that are `windowSize` in length, sliding over `stepping` number of elements and optionally including partial windows at the end of ths stream. |

## Validation and Constraints

Functions that enforce conditions on the stream.

| Function                                                                                                                      | Purpose                                                                                                                                                                                            |
|-------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [`ensureOrdered(orderType)`](https://tginsberg.github.io/gatherers4j/gatherers/validation-and-constraints/ensureordered/)     | Ensure that the input stream of `Comparable` objects is ordered according to the `orderType` (increasing, decreasing, non-increasing, non-decreasing), and fail otherwise.                         |                                                                                           |
| [`ensureOrderedBy(orderType)`](https://tginsberg.github.io/gatherers4j/gatherers/validation-and-constraints/ensureorderedby/) | Ensure that the input stream of objects is ordered according to the `orderType` (increasing, decreasing, non-increasing, non-decreasing), as measured by a given `Comparator`, and fail otherwise. |
| [`ensureSize(sizeType, n)`](https://tginsberg.github.io/gatherers4j/gatherers/validation-and-constraints/ensuresize/)         | Ensure the stream is `n` elements long with reference to `sizeType` (equals, less/greater than, less/greater than or equal to), and fail otherwise.                                                |

## Mathematical Operations

Functions performing calculations over the stream.

| Function                                                                                                                                           | Purpose                                                                                                                            |
|----------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------|
| [`movingProduct(window)`](https://tginsberg.github.io/gatherers4j/gatherers/mathematical/movingproduct/)                                           | Create a moving product of `BigDecimal` objects over the previous `window` values.                                                 |
| [`movingProductBy(window, fn)`](https://tginsberg.github.io/gatherers4j/gatherers/mathematical/movingproductby/)                                   | Create a moving product of `BigDecimal` objects over the previous `window` values, as mapped via `fn`.                             |
| [`movingSum(window)`](https://tginsberg.github.io/gatherers4j/gatherers/mathematical/movingsum/)                                                   | Create a moving sum of `BigDecimal` objects over the previous `window` values.                                                     |
| [`movingSumBy(window, fn)`](https://tginsberg.github.io/gatherers4j/gatherers/mathematical/movingsumby/)                                           | Create a moving sum of `BigDecimal` objects over the previous `window` values, as mapped via `fn`.                                 |
| [`runningPopulationStandardDeviation()`](https://tginsberg.github.io/gatherers4j/gatherers/mathematical/runningpopulationstandarddeviation/)       | Create a stream of `BigDecimal` objects representing the running population standard deviation.                                    |
| [`runningPopulationStandardDeviationBy(fn)`](https://tginsberg.github.io/gatherers4j/gatherers/mathematical/runningpopulationstandarddeviationby/) | Create a stream of `BigDecimal` objects as mapped from the input via `fn`, representing the running population standard deviation. |
| [`runningProduct()`](https://tginsberg.github.io/gatherers4j/gatherers/mathematical/runningproduct/)                                               | Create a stream of `BigDecimal` objects representing the running product.                                                          |                                                          |
| [`runningProductBy(fn)`](https://tginsberg.github.io/gatherers4j/gatherers/mathematical/runningproductby/)                                         | Create a stream of `BigDecimal` objects as mapped from the input via `fn`, representing the running product.                       |
| [`runningSampleStandardDeviation()`](https://tginsberg.github.io/gatherers4j/gatherers/mathematical/runningsamplestandarddeviation/)               | Create a stream of `BigDecimal` objects representing the running sample standard deviation.                                        |
| [`runningSampleStandardDeviationBy(fn)`](https://tginsberg.github.io/gatherers4j/gatherers/mathematical/runningsamplestandarddeviationby/)         | Create a stream of `BigDecimal` objects as mapped from the input via `fn`, representing the running sample standard deviation.     |
| [`runningSum()`](https://tginsberg.github.io/gatherers4j/gatherers/mathematical/runningsum/)                                                       | Create a stream of `BigDecimal` objects representing the running sum.                                                              |
| [`runningSumBy(fn)`](https://tginsberg.github.io/gatherers4j/gatherers/mathematical/runningsumby/)                                                 | Create a stream of `BigDecimal` objects as mapped from the input via `fn`, representing the running sum.                           |
| [`simpleMovingAverage(window)`](https://tginsberg.github.io/gatherers4j/gatherers/mathematical/simplemovingaverage/)                               | Create a moving average of `BigDecimal` values over the previous `window` values. See below for options.                           |
| [`simpleMovingAverageBy(window, fn)`](https://tginsberg.github.io/gatherers4j/gatherers/mathematical/simplemovingaverageby/)                       | Create a moving average of `BigDecimal` values over the previous `window` values, as mapped via `fn`.                              |
| [`simpleRunningAverage()`](https://tginsberg.github.io/gatherers4j/gatherers/mathematical/simplerunningaverage/)                                   | Create a running average of `BigDecimal` values. See below for options.                                                            |
| [`simpleRunningAverageBy(fn)`](https://tginsberg.github.io/gatherers4j/gatherers/mathematical/simplerunningaverageby/)                             | Create a running average of `BigDecimal` values as mapped via `fn`.                                                                |

# Contributing

Please feel free to file issues for change requests or bugs. If you would like to contribute new functionality, please
contact me before starting work!

Copyright © 2024-2025 by Todd Ginsberg