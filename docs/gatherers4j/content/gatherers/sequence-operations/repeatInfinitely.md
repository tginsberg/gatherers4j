---
title: "repeatInfinitely()"
linkTitle: "repeatInfinitely()"
show_in_table: true
category: Sequence Operations
description: Repeatedly emit the input stream to the output stream, infinitely.
infinite_streams: holds-all

---

### Implementation Notes

For a version of this Gatherer that repeats a finite number of times, see [`repeat()`](/gatherers4j/gatherers/sequence-operations/repeat/).

**Signature**

`repeatInfinitely()`


### Examples

#### Repeat the input stream forever

```java
Stream
    .of("A", "B", "C")
    .gather(Gatherers4j.repeatInfinitely())
    .toList();
    
// [ "A", "B", "C", "A", "B", "C", "A", "B", "C" ... <forever> ]
```

