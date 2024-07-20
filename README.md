# Gatherers4j

A library of useful [Stream Gatherers](https://openjdk.org/jeps/473) (custom intermediate operations) for Java 22+.

# Installing

TBD, once I start publishing snapshots to Maven Central.

# Gatherers In This Library


| Function                   | Purpose                                                                                                             |
|----------------------------|---------------------------------------------------------------------------------------------------------------------|
| `averageBigDecimals()`     | Create a running or trailing average of `BigDecimal` values. See below for options.                                 |
| `averageBigDecimalsBy(fn)` | Create a running avergage of `BigDecimal` values mapped out of some different object via `fn`                       |
| `dedupeConsecutive()`      | Remove conescutive duplicates from a stream |
| `dedupeConsecutiveBy(fn)`  | Remove consecutive duplicates from a stream as returned by `fn`                                                     |
| `distinctBy(fn)`           | Emit only distinct elements from the stream, as measured by `fn`                                                    |
| `interleave(stream)`       | Creates a stream of alternating objects from the input stream and the argument stream                               |
| `last(n)`                  | Constrain the stream to the last `n` values                                                                         |
| `withIndex()`              | Maps all elements of the stream as-is, along with their 0-based index.                                              |
| `zipWith(stream)`          | Creates a stream of `Pair` objects whose values come from the input stream and argument stream                      |
| `zipWithNext()`            | Creates a stream of `List` objects via a sliding window of width 2 and stepping 1                                   |                          |


# Use Cases

#### Running average

```java
Stream
    .of("1.0", "2.0", "10.0")
    .map(BigDecimal::new)        
    .gather(Gatherers4j.averageBigDecimals())
    .toList();

// [1, 1.5, 4.3333333333333333]
```

#### Moving average

```java
Stream
    .of("1.0", "2.0", "10.0", "20.0", "30.0")
    .map(BigDecimal::new)
    .gather(Gatherers4j.averageBigDecimals().simpleMovingAverage(2))
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
    .gather(Gatherers4j.dedupeConsecutiveBy(Person::firstName))
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


# Project Philosophy

1. Consider adding a gatherer if it cannot be implemented with `map`, `filter`, or a collector without enclosing outside state.
2. Resist the temptation to add functions that only exist to provide an alias. They seem fun/handy but add surface area to the API and must be maintained forever.
3. All features should be documented and tested.

# Contributing

Please feel free to file issues for change requests or bugs. If you would like to contribute new functionality, please contact me before starting work!

Copyright © 2024 by Todd Ginsberg