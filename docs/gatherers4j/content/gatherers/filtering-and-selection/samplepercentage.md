---
title: "samplePercentage()"
linkTitle: "samplePercentage()"
show_in_table: true
category: Filtering and Selection
description: Perform a percentage-based sampling over the input stream.

---

### Implementation Notes

This uses the Poisson method internally, which means the `percentage` given is an approximation and will be more
accurate over longer streams.

**Signature**

`samplePercentage(double percentage)`

* `percentage` - Percentage of elements to sample. Must be greater than 0.0 and less than 1.0.

### Examples

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
