---
title: "samplePercentage()"
linkTitle: "samplePercentage()"
show_in_table: true
category: Filtering and Selection
description: Perform a percentage-based sampling over the input stream.

---

### Implementation Notes

This uses Bernoulli sampling internally, meaning each element is kept independently with a probability of `percentage`.
This means the `percentage` given is an approximation and will be more accurate over longer streams. Elements will be
emitted in the order in which they are encountered.

There are two versions of this Gatherer, one that uses the platform default `RandomGenerator` and one that allows the caller
to specify a `RandomGenerator`. Specifying a `RandomGenerator` with a fixed seed makes the sample repeatable, which is useful
for testing. Most `RandomGenerator` implementations are not thread-safe, so a single instance of this Gatherer should not be
used from multiple threads at the same time.

**Signatures**

1. `samplePercentage(double percentage)`
2. `samplePercentage(double percentage, RandomGenerator randomGenerator)`

* `percentage` - Percentage of elements to sample, as a fraction (`0.4` samples 40% of elements). Must be greater than 0.0 and no more than 1.0.
* `randomGenerator` - _(Optional)_ A {{< jdklink linkName="RandomGenerator" package="java.base/java/util/random/RandomGenerator.html" >}} to use as the source of randomness.

### Examples

#### Get a percentage of elements from the stream, specifying the source of randomness

```java
Stream
    .of("A", "B", "C", "D", "E")
    .gather(Gatherers4j.samplePercentage(0.4, new Random(42)))
    .toList();

// ["C", "D"] 
```

#### Get a percentage of elements from the stream, randomly

```java
Stream
    .of("A", "B", "C", "D", "E")
    .gather(Gatherers4j.samplePercentage(0.4))
    .toList();

// Possibly: ["A", "D"]
// Possibly: ["A"]
// Possibly: ["A", "D", "E"]
// Answer will be different every time, and is an approximation
```
