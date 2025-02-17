---
title: "last()"
linkTitle: "last()"
show_in_table: true
category: Filtering and Selection
description: Remove all but the last `count` elements from the stream
---


### Implementation Notes

Holds at most `count` number of elements in memory before emitting them once the input stream is fully exhausted. This 
Gatherer is not suitable for infinite streams.

**Signature**

`last(int count)`

* `count` - The positive number of elements to keep at the end of the input stream

### Examples

#### Limit the stream to the last 3 elements

```java
Stream
    .of("A", "B", "C", "D", "E", "F", "G")
    .gather(Gatherers4j.last(3))
    .toList();

// ["E", "F", "G"]
```

