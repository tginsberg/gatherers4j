---
title: "reverse()"
linkTitle: "reverse()"
show_in_table: true
category: Sequence Operations
description: everse the order of the input stream.

---

### Implementation Notes

This implementation reads the entire stream before emitting any results making it inappropriate for infinite streams.


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

