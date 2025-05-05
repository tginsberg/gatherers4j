---
title: "peekIndexed()"
linkTitle: "peekIndexed()"
show_in_table: true
category: Sequence Operations
description: Peek at each element of the stream along with its zero-based index

---

### Implementation Notes

**Signature**

`peekIndexed(BiConsumer<Integer, INPUT> peekingFunction)`

* `peekingFunction` - A non-null consumer to receive each element and its zero-based index.

### Examples

#### Map with index

```java
Stream
    .of("A", "B", "C", "D")
    .gather(
        Gatherers4j.peekIndexed(
            (index, element) -> System.out.println("Element " + element + " at index " + index)
        )
     )
     .toList();

// Returns: [A B C D]
// Prints:
//   Element A at index 0
//   Element B at index 1
//   Element C at index 2
//   Element D at index 3
```

