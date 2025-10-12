---
title: "runningMaxBy()"
linkTitle: "runningMaxBy()"
show_in_table: true
category: "Mathematical Operations"
description: Calculate the running maximum value of a `Stream<T>`, as calculated by the given `Comparator`.
---

### Implementation Notes
This implementation is suitable calculating the running maximum value of a `Stream<INPUT>` via a given `Comparator`; for a version that operates directly on a `Stream<Comparable>`, see [`runningMax()`](/gatherers4j/gatherers/mathematical/runningmax/).
Nulls are ignored and play no part in calculations.

**Signatures**

`runningMaxBy(Comparator<INPUT> comparator)`
* `comparator` - A non-null `Comparator<INPUT>` to use for comparing elements
* 
### Examples

#### Running maximum value

```java
Stream
    .of("1", "2", "3", "2", "3")
    .gather(Gatherers4j.runningMax(Integer::valueOf))
    .toList();

// [ "1", "2", "3", "3", "3" ]
```


#### Showing nulls are ignored by default

```java
Stream
    .of(null, "1", null, "2", "3", "2", "3")
    .gather(Gatherers4j.runningMax(Integer::valueOf))
    .toList();

// [ "1", "2", "3", "3", "3" ]
```
