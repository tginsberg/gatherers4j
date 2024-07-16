# Gatherers4j

A library of useful [Stream Gatherers](https://openjdk.org/jeps/473) (custom intermediate operations) for Java 22+.

# Installing

TBD, once I start publishing snapshots to Maven Central.

# Use Cases

(Example, TODO clean this up)

**Running Average**

```java
Stream
    .of(new BigDecimal("1.0"), new BigDecimal("2.0"), new BigDecimal("10.0"))
    .gather(Gatherers4j.averageBigDecimals())
    .toList();

// [1, 1.5, 4.3333333333333333]
```

**Trailing Average**

```java
Stream
    .of(new BigDecimal("1.0"), new BigDecimal("2.0"), new BigDecimal("10.0"), new BigDecimal("20.0"), new BigDecimal("30.0"))
    .gather(Gatherers4j.averageBigDecimals().trailing(2))
    .toList();

// [1.5, 6, 15, 25]
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