---
title: "rotateRight()"
linkTitle: "rotateRight()"
show_in_table: true
category: Sequence Operations
description: Consume the entire stream and emit its elements rotated to the right `distance` number of spaces

---

### Implementation Notes

This implementation reads the entire stream before emitting any results making it inappropriate for infinite streams.
For a version of this Gatherer that rotates in the opposite direction, see [`rotateLeft()`](/gatherers/sequence-operations/rotateleft/).

**Signature**

`rotateRight(distance)`
* `distance` - Distance to rotate elements, may be positive or negative, may exceed input stream size


### Examples

#### Rotate right positive distance

```java
Stream
    .of("A", "B", "C", "D", "E")
    .gather(Gatherers4j.rotateRight(2))
    .toList();

// ["D", "E", "A", "B", "C"]
```

#### Rotate right negative distance

```java
Stream
    .of("A", "B", "C", "D", "E")
    .gather(Gatherers4j.rotateRight(-2))
    .toList();

// ["C", "D", "E", "A", "B"]
```
