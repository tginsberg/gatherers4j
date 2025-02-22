# Gatherers4j

A library of useful [Stream Gatherers](https://openjdk.org/jeps/485) (custom intermediate operations) for Java 23+.

# Installing

To use this library, add it as a dependency to your build. This library has one transitive
dependency - the [JSpecify](https://jspecify.dev/) set of annotations for static analysis tools.

**Maven**

Add the following dependency to `pom.xml`.

```xml

<dependency>
    <groupId>com.ginsberg</groupId>
    <artifactId>gatherers4j</artifactId>
    <version>0.9.0</version>
</dependency>
```

**Gradle**

Add the following dependency to `build.gradle` or `build.gradle.kts`

```groovy
implementation("com.ginsberg:gatherers4j:0.9.0")
```


# Gatherers In This Library

For convenience, the full list of gatherers in this library are broken into four categories:

1. [General Functions](#general-functions)
2. [Filtering Functions](#filtering-functions)
3. [Grouping Functions](#grouping-functions)
4. [Stream Content Checks/Validation](#stream-content-checksvalidation)
5. [Mathematics/Statistics](#mathematicsstatistics)

## General Functions

Functions that don't (yet!) fall into one of the other categories.

| Function                     | Purpose                                                                                                                                             |
|------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------|
| `crossWith()`                | Emit each element of the source stream with each element of the given `iterable`, `iterator`, `stream`, or varargs as a `Pair` to the output stream |
| `foldIndexed(fn)`            | Perform a fold over the input stream where each element is included along with its index                                                            |
| `interleaveWith()`           | Creates a stream of alternating objects from the input stream and the argument `iterable`, `iterator`, `stream`, or varargs                         |
| `orderByFrequency()`         | Returns a stream where elements are ordered in either ascending or descending frequency contained in `WithCount<T>` wrapper objects.                |
| `repeat(n)`                  | Repeat the input stream `n` times to the output stream                                                                                              |
| `repeatInfinitely()`         | Repeat the input stream to the output stream forever (or until some downstream operation stops it)                                                  |
| `reverse()`                  | Reverse the order of the stream                                                                                                                     |
| `rotate(direction, n)`       | Rotate the stream `n` elements to direction specified. Stores entire stream into memory.                                                            |
| `scanIndexed(fn)`            | Performs a scan on the input stream using the given function, and includes the index of the elements                                                |
| `shuffle()`                  | Shuffle the stream into a random order using the platform default `RandomGenerator`                                                                 |
| `shuffle(rg)`                | Shuffle the stream into a random order using the specified `RandomGenerator`                                                                        |
| `throttle(amount, duration)` | Limit stream elements to `amount` elements over `duration`, pausing until a new `duration` period starts                                            |
| `withIndex()`                | Maps all elements of the stream as-is along with their 0-based index                                                                                |
| `zipWith( )`                 | Creates a stream of `Pair` objects whose values come from the input stream and argument `iterable`, `iterator`, `stream`, or varargs                |
| `zipWithNext()`              | Creates a stream of `List` objects via a sliding window of width 2 and stepping 1                                                                   |      

## Filtering Functions

Functions that remove elements (or retain them, depending on how you look at it) from a stream

| Function                             | Purpose                                                                                                                       |
|--------------------------------------|-------------------------------------------------------------------------------------------------------------------------------|
| `debounce(amount, duration)`         | Limit stream elements to `amount` elements over `duration`, dropping any elements over the limit until a new `duration` starts |
| `dedupeConsecutive()`                | Remove consecutive duplicates from a stream                                                                                   |
| `dedupeConsecutiveBy(fn)`            | Remove consecutive duplicates from a stream as returned by `fn`                                                               |
| `distinctBy(fn)`                     | Emit only distinct elements from the stream, as measured by `fn`                                                              |
| `dropEveryNth(n)`                    | Drop every`n`<sup>th</sup> element from the input stream                                                                      |
| `dropLast(n)`                        | Keep all but the last `n` elements of the stream                                                                              |
| `filterOrdered(order)`               | Filter the input stream of `Comparable` objects so that is strictly in the given `order`                                      |                                                                                           |
| `filterOrderedBy(order, comparator)` | Filter the input stream of objects so that it contains only elements in the given `order`, as measured by a given `Comparator` |
| `takeEveryNth(n)`                    | Keep every`n`<sup>th</sup> element from the input stream                                                                      |
| `takeLast(n)`                        | Emit the last `n` values                                                                                    |
| `takeUntil(predicate)`               | Take elements from the input stream until the `predicate` is met, including the first element that matches the `preciate`     |
| `uniquelyOccurring()`                | Emit elements that occur a single time, dropping all others                                                                   |

## Grouping Functions

Functions that group input elements by varying criteria.

| Function                            | Purpose                                                                     |
|-------------------------------------|-----------------------------------------------------------------------------|
| `group()`                           | Group adjacent equal elements into lists                                    |
| `groupBy(fn)`                       | Group adjacent elements that are equal according to `fn` into lists         | 
| `groupOrdered(order)`               | Group `Comparable` elements in the input stream to lists in the given order |
| `groupOrderedBy(order, comparator)` | Group elements in the given `order` as measured by a `Comparator` to lists  |

## Stream Content Checks/Validation

These gatherers check invariants about streams and fail if they are not met.

| Function                     | Purpose                                                                                                                                                                                            |
|------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `ensureOrdered(orderType)`   | Ensure that the input stream of `Comparable` objects is ordered according to the `orderType` (increasing, decreasing, non-increasing, non-decreasing), and fail otherwise.                         |                                                                                           |
| `ensureOrderedBy(orderType)` | Ensure that the input stream of objects is ordered according to the `orderType` (increasing, decreasing, non-increasing, non-decreasing), as measured by a given `Comparator`, and fail otherwise. |
| `ensureSize(sizeType, n)`    | Ensure the stream is `n` elements long with reference to `sizeType` (equals, less/greater than, less/greater than or equal to), and fail otherwise.                                                |

### Mathematics/Statistics

| Function                                   | Purpose                                                                                                                            |
|--------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------|
| `movingProduct(window)`                    | Create a moving product of `BigDecimal` objects over the previous `window` values.                                                  |
| `movingProductBy(window, fn)`              | Create a moving product of `BigDecimal` objects over the previous `window` values, as mapped via `fn`.                              |
| `movingSum(window)`                        | Create a moving sum of `BigDecimal` objects over the previous `window` values.                                                      |
| `movingSumBy(window, fn)`                  | Create a moving sum of `BigDecimal` objects over the previous `window` values, as mapped via `fn`.                                  |
| `runningPopulationStandardDeviation()`     | Create a stream of `BigDecimal` objects representing the running population standard deviation.                                    |
| `runningPopulationStandardDeviationBy(fn)` | Create a stream of `BigDecimal` objects as mapped from the input via `fn`, representing the running population standard deviation. |
| `runningProduct()`                         | Create a stream of `BigDecimal` objects representing the running product.                                                          |                                                          |
| `runningProductBy(fn)`                     | Create a stream of `BigDecimal` objects as mapped from the input via `fn`, representing the running product.                       |
| `runningSampleStandardDeviation()`         | Create a stream of `BigDecimal` objects representing the running sample standard deviation.                                        |
| `runningSampleStandardDeviationBy(fn)`     | Create a stream of `BigDecimal` objects as mapped from the input via `fn`, representing the running sample standard deviation.     |
| `runningSum()`                             | Create a stream of `BigDecimal` objects representing the running sum.                                                              |
| `runningSumBy(fn)`                         | Create a stream of `BigDecimal` objects as mapped from the input via `fn`, representing the running sum.                           |
| `simpleMovingAverage(window)`              | Create a moving average of `BigDecimal` values over the previous `window` values. See below for options.                           |
| `simpleMovingAverageBy(window, fn)`        | Create a moving average of `BigDecimal` values over the previous `window` values, as mapped via `fn`.                              |
| `simpleRunningAverage()`                   | Create a running average of `BigDecimal` values. See below for options.                                                            |
| `simpleRunningAverageBy(fn)`               | Create a running average of `BigDecimal` values as mapped via `fn`.                                                                |


# Project Philosophy

1. Consider adding a gatherer if it cannot be implemented with `map`, `filter`, or a collector without enclosing outside
   state.
2. Resist the temptation to add functions that only exist to provide an alias. They seem fun/handy but add surface area
   to the API and must be maintained forever.
3. All features should be documented and tested.

# Contributing

Please feel free to file issues for change requests or bugs. If you would like to contribute new functionality, please
contact me before starting work!

Copyright © 2024-2025 by Todd Ginsberg