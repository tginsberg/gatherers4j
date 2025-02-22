---
title: "Change Log"
linkTitle: "Change Log"
weight: 90 
no_list: true
---

## v0.9.0
+ Implement `filterInstanceOf` to filter a stream by type more easily. Addresses [#86](https://github.com/tginsberg/gatherers4j/issues/86), thanks [@nipafx](https://github.com/nipafx).
+ Implement `groupOrdered()`, and `groupOrderedBy()` to appropriately group elements in the input stream to lists in the output stream. Addresses [#88](https://github.com/tginsberg/gatherers4j/issues/88), thanks [@nipafx](https://github.com/nipafx).
+ Implement `ensureOrdred()` and `ensureOrderedBy()` to ensure the given stream meets an ordering criteria, or fail exceptionally otherwise
+ Implement `filterOrdered()` and `filterOrderedBy()` to remove non-ordered elements from the input stream
+ Implement `window()` to provide more options to windowing functions, namely - ability to specify size, how many to skip each time, and whether to include partial windows
+ Implement `repeat(n)` and `repeatInfinitely()` to repeatedly emit the input stream to the output stream
+ Implement `rotateLeft(n)` and `rotateRight(n)` to rotate the stream (consumes entire stream)
+ Renamed `everyNth(n)` to `takeEveryNth(n)` and added `dropEveryNth(n)` for completeness
+ Renamed `filterWithIndex()` to `filterIndexed()` to match other indexing Gatherers
+ Renamed `cross()` to `crossWith()` for consistency
+ Renamed `interleave()` to `interleaveWith()` for consistency
+ Renamed `grouping()` to `group()` and `groupingBy()` to `groupBy()` for consistency
+ Renamed `last()` to `takeLast()` to match `dropLast()`

## v0.8.0
[Released 2025-02-01](https://github.com/tginsberg/gatherers4j/releases/tag/v0.8.0)

+ Add support for `orElse()` and `orElseEmpty()` on size-based gatherers to provide a non-exceptional output stream
+ Implement `everyNth()` to get every n{{< sup "th" >}} element from the stream
+ Implement `uniquelyOccurring()` to emit stream elements that occur a single time
+ Implement `takeUntil()` to take from a stream until a predicate is met, including the first element that matches the predicate
+ Implement `foldIndexed()` to perform a fold along with the index of each element
+ Implement `scanIndexed()` to perform a scan along with the index of each element
+ Implement `intersperse()` to put the given element between each element of the stream
+ Implement `cross(iterable)`, `cross(iterator)`, and `cross(stream)` to combine elements in the input stream with the given source of elements

## v0.7.0
[Released 2025-01-06](https://github.com/tginsberg/gatherers4j/releases/tag/v0.7.0)

+ Use greedy integrators where possible (Addresses [#57](https://github.com/tginsberg/gatherers4j/issues/57))
+ Add [JSpecify](https://jspecify.dev/) annotations for static analysis
+ Implement `orderByFrequencyAscending()` and `orderByFrequencyDescending()`
+ Implement `movingProduct()` and `movingProductBy()`
+ Implement `movingSum()` and `movingSumBy()`
+ Remove `maxBy(fn)` and `minBy(fn)`, can be done with JDK methods trivially
+ Rename `exactSize()` to `sizeExactly()`
+ Implement `sizeLessThan()`, `sizeLessThanOrEqualTo()`, `sizeGreaterThan()`, and `sizeGreaterThanOrEqualTo()`
+ API Style - Functions, when used as arguments, should come last for consistency and to play nice with Kotlin (Addresses [#64](https://github.com/tginsberg/gatherers4j/issues/64))

## v0.6.0
[Released 2024-11-02](https://github.com/tginsberg/gatherers4j/releases/tag/v0.6.0)

+ Implement `dropLast(n)`
+ Implement `grouping()` and `groupingBy(fn)`
+ Add support for `zipWith(iterable)` and `zipWith(iterator)`
    + With support for:
    + `argumentWhenSourceLonger(fn)`
    + `sourceWhenArgumentLonger(fn)`
    + `nullArgumentWhenSourceLonger()`
    + `nullSourceWhenArgumentLonger`
+ Add support for `interleave(iterable)` and `interleave(iterator)`
    + With support for:
    + `appendLonger()`
    + `appendArgumentIfLonger()`
    + `appendSourceIfLonger()`

## v0.5.0
[Released 2024-10-13](https://github.com/tginsberg/gatherers4j/releases/tag/v0.5.0)
+ Implement `reverse()` - Cause a stream to be emitted in reverse (stores entire stream in memory)
+ Implement `maxBy(fn)` and `minBy(fn)` - Finds the single maximum or minimum value in a stream as defined by the `function` given (stores entire stream in memory)
+ Implement `exactSize(n)` - Ensures a stream is precisely `n` elements long or throws an `IllegalStateException` (stores entire stream in memory)

## v0.4.0
[Released 2024-10-08](https://github.com/tginsberg/gatherers4j/releases/tag/v0.4.0)
+ Implement `shuffle()` and `shuffle(RandomGenerator)`
+ Implement `filterWithIndex()`
+ Implement `runningProduct()` and `runningProductBy()`
+ Implement `runningSum()` and `runningSumBy()`
+ Remove `concat()` implementation (the JDK has this)
+ Remove `withIndexStartingAt()` from `filterWithIndex()`, not sure if this is a real use case.

## v0.3.0
[Released 2024-10-19](https://github.com/tginsberg/gatherers4j/releases/tag/v0.3.0)
+ Upgrade the minimum Java version from 22 to 23

## v0.2.0
[Released 2024-08-22](https://github.com/tginsberg/gatherers4j/releases/tag/v0.2.0)
+ Added `throttle()` over a time period
+ Added `debounce()` over a time period

## v0.1.0
[Released 2024-08-13](https://github.com/tginsberg/gatherers4j/releases/tag/v0.1.0)
+ Added Standard Deviation (population and sample)
+ Refactored all BigDecimal-based Gatherers
+ Added concat

## v0.0.1
[Released 2024-07-29](https://github.com/tginsberg/gatherers4j/releases/tag/v0.0.1)
+ Initial release