---
title: "dropLast()"
linkTitle: "dropLast()"
show_in_table: true
category: Filtering and Selection
description: Keep all elements except the last `count` elements of the stream.
---


### Implementation Notes

Holds at most `count` number of elements in memory before emitting them as it becomes clear that they are not in 
the trailing `count` elements in the stream. For a version that takes the last `count` elements, see [`takeLast()`](/gatherers4j/gatherers/filtering-and-selection/takelast/).

**Signature**

`dropLast(int count)`

* `count` - The positive number of elements to remove from the end of the input stream

### Examples

#### Drop the last 2 elements from the input stream

```java
Stream
    .of("A", "B", "C", "D", "E")
    .gather(Gatherers4j.dropLast(2))
    .toList();

// ["A", "B", "C"]
```

