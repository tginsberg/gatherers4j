---
title: "reverse()"
linkTitle: "reverse()"
show_in_table: true
category: Sequence Operations
description: Reverse the order of the input stream.
infinite_streams: holds-all

---

### Implementation Notes

Emits the elements of the input stream in the opposite of the order in which they were encountered.

**Signature**

`reverse()`


### Examples

#### Reverse the input stream

```java
Stream
    .of("A", "B", "C")
    .gather(Gatherers4j.reverse())
    .toList();
    
// [ "C", "B", "A" ]
```

