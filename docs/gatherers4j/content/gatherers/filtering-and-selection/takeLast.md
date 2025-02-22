---
title: "takeLast()"
linkTitle: "takeLast()"
show_in_table: true
category: Filtering and Selection
description: Remove all but the last `count` elements from the stream
---


### Implementation Notes

Holds at most `count` number of elements in memory before emitting them once the input stream is fully exhausted. This 
Gatherer is not suitable for infinite streams. For a version that drops the last `count` elements, see [`dropLast()`](/gatherers4j/gatherers/filtering-and-selection/droplast/).

**Signature**

`takeLast(int count)`

* `count` - The positive number of elements to keep at the end of the input stream

### Examples

#### Limit the stream to the last 3 elements

```java
Stream
    .of("A", "B", "C", "D", "E", "F", "G")
    .gather(Gatherers4j.takeLast(3))
    .toList();

// ["E", "F", "G"]
```

