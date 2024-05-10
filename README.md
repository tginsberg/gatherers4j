# Gatherers4j

A library of useful [Stream Gatherers](https://openjdk.org/jeps/473) (custom intermediate operations) for Java 22+.

# Installing

TBD, once I start publishing snapshots to Maven Central.

# Use Cases

(Example, clean this up)

**Removing consecutive duplicate elements:**

```java
Stream
    .of("A", "A", "A", "B", "B", "C", "C", "D", "A", "B", "C")
    .gather(Gatherers4j.dedupeConsecutive())
    .toList();

// ["A", "B", "C", "D", "A", "B", "C"]

```

# Contributing

Guidance forthcoming.

Copyright © 2024 by Todd Ginsberg