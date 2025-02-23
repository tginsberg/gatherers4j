---
title: "mapIndexed()"
linkTitle: "mapIndexed()"
show_in_table: true
category: Sequence Operations
description: Perform a mapping operation given the element being mapped and its zero-based index

---

### Implementation Notes

**Signature**

`mapIndexed(BiFunction<Long, INPUT, OUTPUT> mappingFunction)`

* `mappingFunction` - A non-null function to convert input elements and their indexes to output elements.

### Examples

#### Map with index

```java
Stream
    .of("A", "B", "C", "D")
    .gather(
        Gatherers4j.mapIndexed(
            (index, element) -> element + index
        )
     )
     .toList();

// [A0 B1 C2 D3]
```

