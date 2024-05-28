# Gatherers4j

A library of useful [Stream Gatherers](https://openjdk.org/jeps/473) (custom intermediate operations) for Java 22+.

# Installing

TBD, once I start publishing snapshots to Maven Central.

# Use Cases

(Example, TODO clean this up)


**Dropping elements from the start of a Stream:**

```java
Stream
    .of("A", "B", "C", "D", "E", "F", "G")
    .gather(Gatherers4j.drop(3))
    .toList();

// ["D", "E", "F", "G"]
```

**Limiting the size of the Stream:**

```java
Stream
    .of("A", "B", "C", "D", "E", "F", "G")
    .gather(Gatherers4j.take(3))
    .toList();

// ["A", "B", "C"]
```

**Removing consecutive duplicate elements:**

```java
Stream
    .of("A", "A", "A", "B", "B", "C", "C", "D", "A", "B", "C")
    .gather(Gatherers4j.dedupeConsecutive())
    .toList();

// ["A", "B", "C", "D", "A", "B", "C"]
```

**Limit the stream to the `last` _n_ elements:**

```java
Stream
    .of("A", "B", "C", "D", "E", "F", "G")
    .gather(Gatherers4j.last(3))
    .toList();

// ["E", "F", "G"]
```

# Contributing

Guidance forthcoming.

Copyright © 2024 by Todd Ginsberg