---
title: "rotate()"
linkTitle: "rotate()"
show_in_table: true
category: Sequence Operations
description: Consume the entire stream and emit its elements rotated either left or right `distance` number of spaces

---

### Implementation Notes

This implementation reads the entire stream before emitting any results making it inappropriate for infinite streams.

**Signature**

`rotate(Rotate direction, long distance)`
* `direction` - Either `Left` or `Right`
* `distance` - Distance to rotate elements, may be positive or negative, may exceed input stream size


### Examples

#### Rotate left positive distance

```java
Stream
    .of("A", "B", "C", "D", "E")
    .gather(Gatherers4j.rotate(Rotate.Left, 2))
    .toList();

// ["C", "D", "E", "A", "B"]
```

#### Rotate left negative distance

```java
Stream
    .of("A", "B", "C", "D", "E")
    .gather(Gatherers4j.rotate(Rotate.Left, -2))
    .toList();

// ["D", "E", "A", "B", "C"]
```

#### Rotate right positive distance

```java
Stream
    .of("A", "B", "C", "D", "E")
    .gather(Gatherers4j.rotate(Rotate.Right, 2))
    .toList();

// ["D", "E", "A", "B", "C"]
```

#### Rotate right negative distance

```java
Stream
    .of("A", "B", "C", "D", "E")
    .gather(Gatherers4j.rotate(Rotate.Right, -2))
    .toList();

// ["C", "D", "E", "A", "B"]
```
