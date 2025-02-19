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

| Function                       | Purpose                                                                                                                                             |
|--------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------|
| `crossWith()`                  | Emit each element of the source stream with each element of the given `iterable`, `iterator`, `stream`, or varargs as a `Pair` to the output stream |
| `foldIndexed(fn)`              | Perform a fold over the input stream where each element is included along with its index                                                            |
| `interleaveWith()`             | Creates a stream of alternating objects from the input stream and the argument `iterable`, `iterator`, `stream`, or varargs                         |
| `orderByFrequencyAscending()`  | Returns a stream where elements are ordered from least to most frequent as `WithCount<T>` wrapper objects.                                          |
| `orderByFrequencyDescending()` | Returns a stream where elements are ordered from most to least frequent as `WithCount<T>` wrapper objects.                                          |
| `repeat(n)`                    | Repeat the input stream `n` times to the output stream                                                                                              |
| `repeatInfinitely()`           | Repeat the input stream to the output stream forever (or until some downstream operation stops it)                                                  |
| `reverse()`                    | Reverse the order of the stream                                                                                                                     |
| `rotateLeft(n)`                | Rotate the stream `n` elements to the left. Stores entire stream into memory.                                                                       |
| `rotateRight(n)`               | Rotate the stream `n` elements to the right. Stores entire stream into memory.                                                                      |
| `scanIndexed(fn)`              | Performs a scan on the input stream using the given function, and includes the index of the elements                                                |
| `shuffle()`                    | Shuffle the stream into a random order using the platform default `RandomGenerator`                                                                 |
| `shuffle(rg)`                  | Shuffle the stream into a random order using the specified `RandomGenerator`                                                                        |
| `throttle(amount, duration)`   | Limit stream elements to `amount` elements over `duration`, pausing until a new `duration` period starts                                            |
| `withIndex()`                  | Maps all elements of the stream as-is along with their 0-based index                                                                                |
| `zipWith( )`                   | Creates a stream of `Pair` objects whose values come from the input stream and argument `iterable`, `iterator`, `stream`, or varargs                |
| `zipWithNext()`                | Creates a stream of `List` objects via a sliding window of width 2 and stepping 1                                                                   |      

## Filtering Functions

Functions that remove elements (or retain them, depending on how you look at it) from a stream

| Function                     | Purpose                                                                                                                        |
|------------------------------|--------------------------------------------------------------------------------------------------------------------------------|
| `debounce(amount, duration)` | Limit stream elements to `amount` elements over `duration`, dropping any elements over the limit until a new `duration` starts |
| `dedupeConsecutive()`        | Remove consecutive duplicates from a stream                                                                                    |
| `dedupeConsecutiveBy(fn)`    | Remove consecutive duplicates from a stream as returned by `fn`                                                                |
| `distinctBy(fn)`             | Emit only distinct elements from the stream, as measured by `fn`                                                               |
| `dropEveryNth(n)`            | Drop every`n`<sup>th</sup> element from the input stream                                                                       |
| `dropLast(n)`                | Keep all but the last `n` elements of the stream                                                                               |
| `filterDecreasing()`         | Filter the input stream of `Comparable` objects so that it contains only strictly decreasing objects                           |                                                                                           |
| `filterDecreasingBy()`       | Filter the input stream of objects so that it contains only strictly decreasing objects, as measured by a given `Comparator`   |
| `filterIncreasing()`         | Filter the input stream of `Comparable` objects so that it contains only strictly increasing objects                           |
| `filterIncreasingBy() `      | Filter the input stream of objects so that it contains only strictly increasing objects, as measured by a given `Comparator`   |
| `filterIndexed(predicate)`   | Filter the stream with the given `predicate`, which takes an `element` and its `index`                                         |
| `filterNonDecreasing()`      | Filter the input stream of `Comparable` objects so that it contains only non-decreasing objects                                |
| `filterNonDecreasingBy()`    | Filter the input stream of objects so that it contains only non-decreasing objects, as measured by a given `Comparator`        |
| `filterNonIncreasing()`      | Filter the input stream of `Comparable` objects so that it contains non-increasing objects                                     |
| `filterNonIncreasingBy()`    | Filter the input stream of objects so that it contains only non-increasing objects, as measured by a given `Comparator`        |
| `filterInstanceOf(types)`    | Filter the stream to only include elements of the given type(s)                                                                |
| `last(n)`                    | Constrain the stream to the last `n` values                                                                                    |
| `takeEveryNth(n)`            | Keep every`n`<sup>th</sup> element from the input stream                                                                       |
| `takeUntil(predicate)`       | Take elements from the input stream until the `predicate` is met, including the first element that matches the `preciate`      |
| `uniquelyOccurring()`        | Emit elements that occur a single time, dropping all others                                                                    |

## Grouping Functions

Functions that group input elements by varying criteria.

| Function                   | Purpose                                                                                                                                            |
|----------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------|
| `group()`                  | Group consecutive identical elements into lists                                                                                                    |
| `groupBy(fn)`              | Group consecutive elements that are identical according to `fn` into lists                                                                         | 
| `groupDecreasing()`        | Group decreasing `Comparable` elements in the input stream to lists in the output stream                                                           |
| `groupDecreasingBy()`      | Group decreasing elements as measured by a `Comparator` in the input stream to lists in the output stream                                          |
| `groupIncreasing()`        | Group increasing `Comparable` elements in the input stream to lists in the output stream                                                           |
| `groupIncreasingBy()`      | Group increasing elements as measured by a `Comparator` in the input stream to lists in the output stream                                          |
| `groupNonIncreasing()`     | Group non-increasing `Comparable` elements in the input stream to lists in the output stream                                                       |
| `groupNonIncreasingBy()`   | Group non-increasing elements as measured by a `Comparator` in the input stream to lists in the output stream                                      |
| `groupNonDecreasing()`     | Group non-decreasing `Comparable` elements in the input stream to lists in the output stream                                                       |
| `groupNonDecreasingBy()`   | Group non-decreasing elements as measured by a `Comparator` in the input stream to lists in the output stream                                      |
| `window(size,step,partial)` | Create windows over the input stream that are `size` elements long, sliding over `step` elements each time, optionally including `partial` windows |

## Stream Content Checks/Validation

These gatherers check invariants about streams and fail if they are not met.

| Function                      | Purpose                                                                                                                                     |
|-------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------|
| `ensureDecreasing()`          | Ensure that the input stream of `Comparable` objects contains only strictly decreasing objects, and fail otherwise.                         |                                                                                           |
| `ensureDecreasingBy()`        | Ensure that the input stream of objects contains only strictly decreasing objects, as measured by a given `Comparator`, and fail otherwise. |
| `ensureIncreasing()`          | Ensure that the input stream of `Comparable` objects contains only strictly increasing objects, and fail otherwise.                         |
| `ensureIncreasingBy()`        | Ensure that the input stream of objects contains only strictly increasing objects, as measured by a given `Comparator`, and fail otherwise. |
| `ensureNonDecreasing()`       | Ensure that the input stream of `Comparable` objects contains only non-decreasing objects, and fail otherwise.                              |
| `ensureNonDecreasingBy()`     | Ensure that the input stream of objects contains only non-decreasing objects, as measured by a given `Comparator`, and fail otherwise.      |
| `ensureNonIncreasing()`       | Ensure that the input stream of `Comparable` objects contains non-increasing objects, and fail otherwise.                                   |
| `ensureNonIncreasingBy() `    | Ensure that the input stream of objects contains only non-increasing objects, as measured by a given `Comparator`, and fail otherwise.      |
| `sizeExactly(n)`              | Ensure the stream is exactly `n` elements long, or throw an `IllegalStateException`                                                         |
| `sizeGreaterThan(n)`          | Ensure the stream is greater than `n` elements long, or throw an `IllegalStateException`                                                    |
| `sizeGreaterThanOrEqualTo(n)` | Ensure the stream is greater than or equal to `n` elements long, or throw an `IllegalStateException`                                        |
| `sizeLessThan(n)`             | Ensure the stream is less than `n` elements long, or throw an `IllegalStateException`                                                       |
| `sizeLessThanOrEqualTo(n)`    | Ensure the stream is less than or equal to `n` elements long, or throw an `IllegalStateException`                                           |

### Mathematics/Statistics

| Function                                   | Purpose                                                                                                                            |
|--------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------|
| `movingProduct(window)`                    | Create a moving product of `BigDecimal` values over the previous `window` values.                                                  |
| `movingProductBy(window, fn)`              | Create a moving product of `BigDecimal` values over the previous `window` values, as mapped via `fn`.                              |
| `movingSum(window)`                        | Create a moving sum of `BigDecimal` values over the previous `window` values.                                                      |
| `movingSumBy(window, fn)`                  | Create a moving sum of `BigDecimal` values over the previous `window` values, as mapped via `fn`.                                  |
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

# Use Cases

#### Running average of `Stream<BigDecimal>`

```java
Stream
    .of("1.0", "2.0", "10.0")
    .map(BigDecimal::new)        
    .gather(Gatherers4j.simpleRunningAverage())
    .toList();

// [1, 1.5, 4.3333333333333333]
```

#### Moving average of `Stream<BigDecimal>`

```java
Stream
    .of("1.0", "2.0", "10.0", "20.0", "30.0")
    .map(BigDecimal::new)
    .gather(Gatherers4j.simpleMovingAverage(2))
    .toList();

// [1.5, 6, 15, 25]
```

#### Remove consecutive duplicate elements

```java
Stream
    .of("A", "A", "A", "B", "B", "C", "C", "D", "A", "B", "C")
    .gather(Gatherers4j.dedupeConsecutive())
    .toList();

// ["A", "B", "C", "D", "A", "B", "C"]
```

#### Remove consecutive duplicate elements, where duplicate is measured by a function

```java
record Person(String firstName, String lastName) {}

Stream
    .of(
        new Person("Todd", "Ginsberg"),
        new Person("Emma", "Ginsberg"),
        new Person("Todd", "Smith")
    )
    .gather(Gatherers4j.dedupeConsecutiveBy(Person::lastName))
    .toList();

// [Person("Todd", "Ginsberg"), Person("Todd", "Smith")]
```

#### Remove duplicate elements, where duplicate is measured by a function

```java
record Person(String firstName, String lastName) {}

Stream
    .of(
        new Person("Todd", "Ginsberg"),
        new Person("Emma", "Ginsberg"),
        new Person("Todd", "Smith")
    )
    .gather(Gatherers4j.distinctBy(Person::firstName))
    .toList();

// [Person("Todd", "Ginsberg"), Person("Emma", "Ginsberg")]
```

#### Keep all but the last `n` elements

```java
Stream.of("A", "B", "C", "D", "E")
    .gather(Gatherers4j.dropLast(2))
    .toList();

// ["A", "B", "C"]
```

#### Keep every `n`<sup>th</sup> element

```java
Stream.of("A", "B", "C", "D", "E", "F", "G")
    .gather(Gatherers4j.everyNth(3))
    .toList();
    
// ["A", "D", "G"]
```

#### Take from a stream until a predicate is met, inclusive

```java
Stream.of("A", "B", "C", "D", "E", "F", "G")
    .gather(Gatherers4j.takeUntil(it -> it.equals("C")))
    .toList()
    
// ["A", "B", "C"]
```

#### Ensure the stream is exactly `n` elements long

```java
// Good

Stream.of("A", "B", "C").gather(Gatherers4j.sizeExactly(3)).toList();
// ["A", "B", "C"]

// Bad
Stream.of("A").gather(Gatherers4j.sizeExactly(3)).toList();
// IllegalStateException
```

#### Replace stream when size check fails

This applies to `sizeExactly()`, `sizeLessThan()`, `sizeLessThanOrEqualTo()`, `sizeGreaterThan()`, and `sizeGreaterThanOrEqualTo()`.

Note, this needs a type witness due to how Java generics work.

```java
Stream.of("A")
    .gather(Gatherers4j.<String>sizeExactly(2).orElse(() -> Stream.of("A", "B")))
    .toList();

// ["A", "B"]
```

#### Return an empty stream when size check fails

This applies to `sizeExactly()`, `sizeLessThan()`, `sizeLessThanOrEqualTo()`, `sizeGreaterThan()`, and `sizeGreaterThanOrEqualTo()`.

Note, this needs a type witness due to how Java generics work.

```java
Stream.of("A")
    .gather(Gatherers4j.<String>sizeExactly(2).orElseEmpty())
    .toList();

// []
```

#### Ensure the stream is greater than `n` elements long

```java
// Good

Stream.of("A", "B", "C").gather(Gatherers4j.sizeGreaterThan(2)).toList();
// ["A", "B", "C"]

// Bad
Stream.of("A", "B").gather(Gatherers4j.sizeGreaterThan(2)).toList();
// IllegalStateException
```

#### Ensure the stream is greater than or equal to `n` elements long

```java
// Good

Stream.of("A", "B").gather(Gatherers4j.sizeGreaterThanOrEqualTo(2)).toList();
// ["A", "B"]

// Bad
Stream.of("A").gather(Gatherers4j.sizeGreaterThanOrEqualTo(2)).toList();
// IllegalStateException
```

#### Ensure the stream is less than `n` elements long

```java
// Good

Stream.of("A").gather(Gatherers4j.sizeLessThan(2)).toList();
// ["A"]

// Bad
Stream.of("A", "B").gather(Gatherers4j.sizeLessThan(2)).toList();
// IllegalStateException
```


#### Ensure the stream is less than or equal to `n` elements long

```java
// Good

Stream.of("A", "B").gather(Gatherers4j.sizeLessThanOrEqualTo(2)).toList();
// ["A", "B"]

// Bad
Stream.of("A", "B", "C").gather(Gatherers4j.sizeLessThanOrEqualTo(2)).toList();
// IllegalStateException
```

#### Filter a stream by type
```java
Stream.of((byte)1, (short)2, 3, (long)4, 1.0, 1.0d)
    .gather(Gatherers.4j.filterInstanceOf(Integer.class, Short.class))
    .toList();

// [2, 3]
```


#### Filter a stream, knowing the index of each element

```java
Stream.of("A", "B", "C", "D")
      .gather(Gatherers4j.filterIndexed((index, element) -> index % 2 == 0 || element.equals("D")))
      .toList();

// ["A", "C", "D"]
```


#### Perform a fold with access to the index of the elements

```java
// Add even elements only
Stream.of(1, 2, 3, 4, 5, 6)
    .gather(
        Gatherers4j.foldIndexed(
            () -> 0,
            (index, carry, next) -> index % 2 == 0 ? carry + next : carry
        )
    ).toList().getFirst();

// 9
```

#### Perform a scan with access to the index of the elements

```java
Stream.of("A", "B", "C")
    .gather(
        Gatherers4j.scanIndexed(
            () -> "",
            (index, carry, next) -> carry + next + index
        )
    ).toList();

// ["A0", "A0B1", "A0B1C2"]
```

#### Cross the input stream with the given iterable

```java
Stream.of("A", "B", "C")
    .gather(
        Gatherers4j.crossWith(List.of(1, 2))
    ).toList();

// [Pair(A, 1), Pair(A, 2), Pair(B, 1), Pair(B, 2), Pair(C, 1), Pair(C, 2)]
```

#### Cross the input stream with the given iterator

```java
Stream.of("A", "B", "C")
    .gather(
        Gatherers4j.crossWith(List.of(1, 2).iterator())
    ).toList();

// [Pair(A, 1), Pair(A, 2), Pair(B, 1), Pair(B, 2), Pair(C, 1), Pair(C, 2)]
```

#### Cross the input stream with the given stream

```java
Stream.of("A", "B", "C")
    .gather(
        Gatherers4j.crossWith(Stream.of(1, 2))
    ).toList();

// [Pair(A, 1), Pair(A, 2), Pair(B, 1), Pair(B, 2), Pair(C, 1), Pair(C, 2)]
```


#### Group identical elements

```java
Stream.of("A", "A", "B", "B", "B", "C")
    .gather(Gatherers4j.grouping())
    .toList();

// [["A", "A"], ["B", "B", "B"], ["C"]]
```

#### Group identical elements as measured by a function

```java
Stream.of("A", "B", "AA", "BB", "CC", "DDD")
    .gather(Gatherers4j.groupingBy(String::length))
    .toList();

// [["A", "B"], ["AA", "BB", "CC"], ["DDD"]]
```


#### Interleave streams of the same type into one stream

```java
final Stream<String> left = Stream.of("A", "B", "C");
final Stream<String> right = Stream.of("D", "E", "F");

left.gather(Gatherers4j.interleave(right)).toList();

// ["A", "D", "B", "E", "C", "F"]
```

#### Limit the stream to the `last` _n_ elements

```java
Stream
    .of("A", "B", "C", "D", "E", "F", "G")
    .gather(Gatherers4j.last(3))
    .toList();

// ["E", "F", "G"]
```

#### Order elements by frequency, ascending

```java
Stream.of("A", "A", "A", "B", "B" ,"C")  
    .gather(Gatherers4j.orderByFrequencyAscending())
    .toList()

// [WithCount("C", 1), WithCount("B", 2), WithCount("C", 3) ]
```

#### Order elements by frequency, descending

```java
Stream.of("A", "A", "A", "B", "B" ,"C")  
    .gather(Gatherers4j.orderByFrequencyDescending())
    .toList()

// [WithCount("C", 3), WithCount("B", 2), WithCount("A", 1) ]
```

#### Reverse the order of the stream

```java
Stream
    .of("A", "B", "C")
    .gather(Gatherers4j.reverse())
    .toList();

// ["C", "B", "A"]
```

#### Include index with original stream values

```java
Stream
    .of("A", "B", "C")
    .gather(Gatherers4j.withIndex())
    .toList();

// [IndexedValue(0, "A"), IndexedValue(1, "B"), IndexedValue(2, "C")]
```

#### Shuffle the stream into a random order

```java
Stream
     .of("A", "B", "C", "D" ,"E")
     .gather(Gatherers4j.shuffle())
     .toList();

// ex: ["B", "E", "A", "C", "D"] -- or some other randomly arranged stream
```

#### Shuffle the stream into a random order, with a specific `RandomGenerator`

```java
Stream
     .of("A", "B", "C", "D" ,"E")
     .gather(Gatherers4j.shuffle(RandomGenerator.of("someGenerator")))
     .toList();

// ex: ["B", "E", "A", "C", "D"] -- or some other randomly arranged stream
```

#### Throttle the number of elements consumed in a period

```java
Stream
    .of("A", "B", "C")
    .gather(Gatherers4j.throttle(2, Duration.ofSeconds(1))) // Two per second
    .toList();

// ["A", "B",   "C"]
              ^
              |
              +----------- Pause
```

#### Limit stream to elements that occur a single time

```java
Stream
    .of("A", "B", "C", "A")
    .gather(Gatherers4j.uniquelyOccurring())
    .toList();

// ["B", "C"]
```

#### Get a windowed view of a stream

```java
// Window size 3, stepping 2, without partials
Stream.of("A", "B", "C", "D", "E", "F")
    .gather(Gatherers4j.windowed(3, 2, false))
    .toList();

// [ ["A", "B", "C"], ["C", "D", "E"] ]
```

```java
// Window size 3, stepping 2, with partials
Stream.of("A", "B", "C", "D", "E", "F")
    .gather(Gatherers4j.windowed(3, 2, true))
    .toList();

// [ ["A", "B", "C"], ["C", "D", "E"], ["E, "F"] ]

```


#### Zip two streams of together into a `Stream<Pair>`

The left and right streams can be of different types.

```java
final Stream<String> left = Stream.of("A", "B", "C");
final Stream<Integer> right = Stream.of(1, 2, 3);

left.gather(Gatherers4j.zip(right)).toList();

// [Pair("A", 1), Pair("B", 2), Pair("C", 3)] 

```

#### Zip elements of a stream together

This converts a `Stream<T>` to a `Stream<List<T>>`

```java
Stream
     .of("A", "B", "C", "D", "E")
     .gather(Gatherers4j.zipWitNext())
     .toList();

// [["A", "B"], ["B", "C"], ["C", "D"], ["D", "E"]]
```


#### Ensure Increasing/Decreasing Order of Stream

```java
// Success
Stream.of(1,2,3)
    .gather(Gatherers4j.ensureIncreasing())
    .toList();

// Success
Stream.of(3,2,1)
    .gather(Gatherers4j.ensureDecreasing())
        .toList();

// Success
Stream.of(1,1,2)
    .gather(Gatherers4j.ensureNonDecreasing())
        .toList();

// Success
Stream.of(2,2,1)
    .gather(Gatherers4j.ensureNonIncreasing())
        .toList();

// Fail
Stream.of(3,2,1)
    .gather(Gatherers4j.ensureIncreasing())
    .toList();
// thrown: IllegalStateException

// Fail
Stream.of(1,2,3)
    .gather(Gatherers4j.ensureDecreasing())
        .toList();
// thrown: IllegalStateException

// Fail
Stream.of(2,2,1)
    .gather(Gatherers4j.ensureNonDecreasing())
        .toList();
// thrown: IllegalStateException

// Fail
Stream.of(2,2,3)
    .gather(Gatherers4j.ensureNonIncreasing())
    .toList();
// thrown: IllegalStateException
```


## Streams of `BigDecimal`

Functions which modify output and are available on all `BigDecimal` gatherers (simple average, moving average, and standard deviation).

| Function                       | Purpose                                                                                                                                   |
|--------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------|
| `treatNullAsZero()`            | When an element in the `Stream<BigDecimal>` is `null` treat it as `BigDecimal.ZERO` instead of skipping it in the calculation.            |
| `treatNullAs(BigDecimal)`      | When an element in the `Stream<BigDecimal>` is `null` treat it as the `BigDecimal` value given instead of skipping it in the calculation. |
| `withMathContext(MathContext)` | Switch the `MathContext` for all calculations to the non-null `MathContext` given. The default is `MathContext.DECIMAL64`.                |
| `withOriginal()`               | Include the original stream value in addition to the calculated value.                                                                    |

Note that rounding mode, precision, and scale are derived from the `MathContext`.

#### Example of `simpleRunningAverage()`

This example creates a stream of `double`, converts each value to a `BigDecmial`, and takes a `simpleMovingAverage` over 10 trailing values.
It will `includePartialValues` and sets the `MathContext` to the values given. Additionally, nulls
are treated as zeros, and the calculated average is returned along with the original value.

```java
someStreamOfBigDecimal()
    .gather(Gatherers4j
        .simpleMovingAverage(10)
        .includePartialValues()
        .withMathContext(MathContext.DECIMAL32)
        .treatNullAsZero()
        .withOriginal()
    )
    .toList();

// Example output:
[
  WithOriginal[original=0.8462487, calculated=0.8462487], 
  WithOriginal[original=0.8923297, calculated=0.8692890], 
  WithOriginal[original=0.2556937, calculated=0.6647573], 
  WithOriginal[original=0.2901778, calculated=0.5711125], 
  WithOriginal[original=0.4945578, calculated=0.5558016], 
  WithOriginal[original=0.3173066, calculated=0.5160525], 
  WithOriginal[original=0.6377766, calculated=0.5334417], 
  WithOriginal[original=0.1729199, calculated=0.4883765], 
  WithOriginal[original=0.7408201, calculated=0.5164258], 
  WithOriginal[original=0.7169926, calculated=0.5364825], 
  WithOriginal[original=0.5174489, calculated=0.5036025], 
  WithOriginal[original=0.5895662, calculated=0.4733262], 
  WithOriginal[original=0.4458275, calculated=0.4923396], 
  // etc...
]
```


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