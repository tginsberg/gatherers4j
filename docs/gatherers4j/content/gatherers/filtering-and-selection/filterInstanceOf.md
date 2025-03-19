---
title: "filterInstanceOf()"
linkTitle: "filterInstanceOf()"
show_in_table: true
category: Filtering and Selection
description: Filter the elements in the stream to only include elements of the given types.

---

### Implementation Notes

Due to how generics work you may end up with some... interesting stream types as a result (which ultimately shouldn't
matter but will look funny if you have type hinting on in your IDE)

**Signature**

`filterInstanceOf(Class<? extends OUTPUT>... validTypes)`

* `validTypes` - A non-empty number of `Class` types to limit the output stream to

### Examples

#### Filter a `Stream<Number>` to only contain `Short` and `Integer`

```java
Stream
    .of((byte)1, (short)2, 3, (long)4, 1.0, 1.0d)
    .gather(Gatherers4j.filterInstanceOf(Integer.class, Short.class))
    .toList();

// [2, 3]
```
