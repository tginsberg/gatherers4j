---
title: "repeat()"
linkTitle: "repeat()"
show_in_table: true
category: Sequence Operations
description: Repeatedly emit the input stream to the output stream a given number of times.

---

### Implementation Notes

This implementation reads the entire stream before emitting any results making it inappropriate for infinite streams.
For a version of this Gatherer that repeats infinitely, see [`repeatInfinitely()`](/gatherers4j/gatherers/sequence-operations/repeatinfinitely/).

**Signature**

`repeat(count)`
* `count` - The number of times the input stream is emitted to the output stream


### Examples

#### Repeat the input stream 3 times to the output stream

```java
Stream
    .of("A", "B", "C")
    .gather(Gatherers4j.repeat(3))
    .toList();
    
// [ "A", "B", "C", "A", "B", "C", "A", "B", "C" ]
```

