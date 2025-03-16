---
title: "sampleFixed()"
linkTitle: "sampleFixed()"
show_in_table: true
category: Filtering and Selection
description: Perform a fixed size sampling over the input stream.

---

### Implementation Notes

This uses the reservoir method internally and guarantees to have exactly `sampleSize` number of elements
for streams at least `sampleSize` in length. Elements will be emitted in the order in which they are encountered.

**Signature**

`sampleFixed(int sampleSize)`

* `sampleSize` - A positive number of elements to sample from the stream, randomly

### Examples

#### Get a fixed number of elements from the stream, randomly

```java
Stream
    .of("A", "B", "C", "D", "E")
    .gather(Gatherers4j.sampleFixed(2))
    .toList();

// Possibly: ["A", "D"]
// Answer will be different every time
```
