---
title: "sampleFixedSize()"
linkTitle: "sampleFixedSize()"
show_in_table: true
category: Filtering and Selection
description: Perform a fixed-size sampling over the input stream.

---

### Implementation Notes

This uses the reservoir method internally and guarantees that for streams that are at least `sampleSize` in length, there
will be exactly `sampleSize` elements in the output stream. Elements will be emitted in the order in which they are encountered.
This implementation reads the entire stream before emitting any results, making it inappropriate for infinite streams.

There are two versions of this Gatherer, one that uses the platform default `RandomGenerator` and one that allows the caller
to specify a `RandomGenerator`. Specifying a `RandomGenerator` with a fixed seed makes the sample repeatable, which is useful
for testing. Most `RandomGenerator` implementations are not thread-safe, so a single instance of this Gatherer should not be
used from multiple threads at the same time.

**Signatures**

1. `sampleFixedSize(int sampleSize)`
2. `sampleFixedSize(int sampleSize, RandomGenerator randomGenerator)`

* `sampleSize` - A positive number of elements to sample from the stream, randomly
* `randomGenerator` - _(Optional)_ A {{< jdklink linkName="RandomGenerator" package="java.base/java/util/random/RandomGenerator.html" >}} to use as the source of randomness.

### Examples

#### Get a fixed number of elements from the stream, specifying the source of randomness

```java
Stream
    .of("A", "B", "C", "D", "E")
    .gather(Gatherers4j.sampleFixedSize(2, new Random(123)))
    .toList();

// ["A", "E"] 
```

#### Get a fixed number of elements from the stream, randomly

```java
Stream
    .of("A", "B", "C", "D", "E")
    .gather(Gatherers4j.sampleFixedSize(2))
    .toList();

// Possibly: ["A", "D"]
// Answer will be different every time
```
