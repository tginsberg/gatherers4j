---
title: "rotateLeft()"
linkTitle: "rotateLeft()"
show_in_table: true
category: Sequence Operations
description: Consume the entire stream and emit its elements rotated to the left `distance` number of spaces

---

### Implementation Notes

This implementation reads the entire stream before emitting any results making it inappropriate for infinite streams.
For a version of this Gatherer that rotates in the opposite direction, see [`rotateRight()`](/gatherers/sequence-operations/rotateright/).

**Signature**

`rotateLeft(distance)`
* `distance` - Distance to rotate elements, may be positive or negative, may exceed input stream size


### Examples

#### Rotate left positive distance

```java
Stream
    .of("A", "B", "C", "D", "E")
    .gather(Gatherers4j.rotateLeft(2))
    .toList();

// ["C", "D", "E", "A", "B"]
```

#### Rotate left negative distance

```java
Stream
    .of("A", "B", "C", "D", "E")
    .gather(Gatherers4j.rotateLeft(-2))
    .toList();

// ["D", "E", "A", "B", "C"]
```
