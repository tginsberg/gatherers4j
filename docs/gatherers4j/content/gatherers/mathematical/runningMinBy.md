---
title: "runningMinBy()"
linkTitle: "runningMinBy()"
show_in_table: true
category: "Mathematical Operations"
description: Calculate the running minimum value of a `Stream<T>`, as calculated by the given `Comparator`.
---

### Implementation Notes
This implementation is suitable calculating the running minimum value of a `Stream<INPUT>` via a given `Comparator`; for a version that operates directly on a `Stream<Comparable>`, see [`runningMin()`](/gatherers4j/gatherers/mathematical/runningmin/).
Nulls are ignored and play no part in calculations.

**Signatures**

`runningMinBy(Comparator<INPUT> comparator)`
* `comparator` - A non-null `Comparator<INPUT>` to use for comparing elements
* 
### Examples

#### Running minimum value

```java
Stream
    .of("3", "2", "1", "2", "3")
    .gather(Gatherers4j.runningMinBy(Integer::valueOf))
    .toList();

// [ "3", "2", "1", "1", "1" ]
```


#### Showing nulls are ignored by default

```java
Stream
    .of(null, "3", null, "2", "1", "2", "3")
    .gather(Gatherers4j.runningMinBy(Integer::valueOf))
    .toList();

// [ "3", "2", "1", "1", "1" ]
```
