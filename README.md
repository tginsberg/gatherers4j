# Gatherers4j

A library of useful [Stream Gatherers](https://openjdk.org/jeps/473) (custom intermediate operations) for Java 22+.

# Installing

To use this library, add it as a dependency to your build. This library has no additional dependencies.

**Maven**

Add the following dependency to `pom.xml`.

```xml

<dependency>
    <groupId>com.ginsberg</groupId>
    <artifactId>gatherers4j</artifactId>
    <version>0.0.1</version>
</dependency>
```

**Gradle**

Add the following dependency to `build.gradle` or `build.gradle.kts`

```groovy
implementation("com.ginsberg:gatherers4j:0.0.1")
```

# Gatherers In This Library

### Streams
| Function                  | Purpose                                                                                                                            |
|---------------------------|------------------------------------------------------------------------------------------------------------------------------------|
| `concat(stream)`          | Creates a stream which is the concatenation of the source stream and the given stream, which must be of the same type |
| `dedupeConsecutive()`     | Remove consecutive duplicates from a stream                                                                                        |
| `dedupeConsecutiveBy(fn)` | Remove consecutive duplicates from a stream as returned by `fn`                                                                    |
| `distinctBy(fn)`          | Emit only distinct elements from the stream, as measured by `fn`                                                                   |
| `interleave(stream)`      | Creates a stream of alternating objects from the input stream and the argument stream                                              |
| `last(n)`                 | Constrain the stream to the last `n` values                                                                                        |
| `withIndex()`             | Maps all elements of the stream as-is, along with their 0-based index.                                                             |
| `zipWith(stream)`         | Creates a stream of `Pair` objects whose values come from the input stream and argument stream                                     |
| `zipWithNext()`           | Creates a stream of `List` objects via a sliding window of width 2 and stepping 1                                                  |                          |

### Mathematics/Statistics
| Function                                   | Purpose                                                                                                                            |
|--------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------|
| `runningPopulationStandardDeviation()`     | Create a stream of `BigDecimal` objects representing the running population standard deviation.                                    |
| `runningPopulationStandardDeviationBy(fn)` | Create a stream of `BigDecimal` objects as mapped from the input via `fn`, representing the running population standard deviation. |
| `runningSampleStandardDeviation()`         | Create a stream of `BigDecimal` objects representing the running sample standard deviation.                                        |
| `runningSampleStandardDeviationBy(fn)`     | Create a stream of `BigDecimal` objects as mapped from the input via `fn`, representing the running sample standard deviation.     |
| `simpleMovingAverage(window)`              | Create a moving average of `BigDecimal` values over the previous `window` values. See below for options.                           |
| `simpleMovingAverageBy(fn, window)`        | Create a moving average of `BigDecimal` values over the previous `window` values, as mapped via `fn`.                              |
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

#### Concatenate two streams

```java
Stream
    .of("A", "B", "C")
    .gather(Gatherers4j.concat(Stream.of("D", "E", "F")))
    .toList();

// ["A", "B", "C", "D", "E", "F"]
```

#### Concatenate multiple streams

```java
Stream
    .of("A", "B", "C")
    .gather(Gatherers4j
        .concat(Stream.of("D", "E", "F"))
        .concat(Stream.of("G", "H", "I")) // concat can be called again for more streams
    )
    .toList();

// ["A", "B", "C", "D", "E", "F", "G", "H", "I"]
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

#### Include index with original stream values

```java
Stream
    .of("A", "B", "C")
    .gather(Gatherers4j.withIndex())
    .toList();

// [IndexedValue(0, "A"), IndexedValue(1, "B"), IndexedValue(2, "C")]
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

## Streams of `BigDecimal`

Functions which modify output and are available on all `BigDecimal` gatherers (simple average, moving average, and standard deviation).

| Function                       | Purpose                                                                                                                                   |
|--------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------|
| `treatNullAsZero()`            | When an element in the `Stream<BigDecimal>` is `null` treat it as `BigDecimal.ZERO` instead of skipping it in the calculation.            |
| `treatNullAs(BigDecimal)`      | When an element in the `Stream<BigDecimal>` is `null` treat it as the `BigDecimal` value given instead of skipping it in the calculation. |
| `withMathContext(MathContext)` | Switch the `MathContext` for all calculations to the non-null `MathContext` given. The default is `MathContext.DECIMAL64`.                |
| `withOriginal()`               | Include the original stream value in addition to the calculated value.                                                                    |

Note that rounding mode, precision, and scale are derived from the `MathContext`.

### Example of `simpleRunningAverage()`

This example creates a stream of `double`, converts each value to a `BigDecmial`, and takes a `simpleMovingAverage` over 10 trailing values.
It will `includePartialValues` and sets the `MathContext` to the values given. Additionally, nulls
are treated as zeros, and the calculated average is returned along with the original value.

```java
someStreamOfBigDecimal()
    .gather(Gatherers4j
        .averageBigDecimals()
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

1. Consider adding a gatherer if it cannot be implemented with `map`, `filter`, or a collector without enclosing outside state.
2. Resist the temptation to add functions that only exist to provide an alias. They seem fun/handy but add surface area to the API and must be maintained forever.
3. All features should be documented and tested.

# Contributing

Please feel free to file issues for change requests or bugs. If you would like to contribute new functionality, please contact me before starting work!

Copyright © 2024 by Todd Ginsberg