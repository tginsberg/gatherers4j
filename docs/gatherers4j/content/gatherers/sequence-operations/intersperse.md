---
title: "intersperse()"
linkTitle: "intersperse()"
show_in_table: true
category: Sequence Operations
description: Put the given element between each pair of adjacent elements in the input stream.

---

### Implementation Notes

The given element is only placed *between* elements of the input stream, never before the first or after the last,
so a stream of *n* elements gains *n - 1* copies of it. Empty and single-element streams are emitted unchanged. The
element to intersperse may be `null`.

To alternate with elements from another source rather than repeating a single element, see
[`interleaveWith()`](/gatherers4j/gatherers/sequence-operations/interleavewith/).

**Signature**

`intersperse(INPUT intersperseElement)`
* `intersperseElement` - The element to put between each pair of adjacent elements, which may be `null`

### Examples

#### Put a separator between each element

```java
Stream
    .of("A", "B", "C")
    .gather(Gatherers4j.intersperse("-"))
    .toList();

// [ "A", "-", "B", "-", "C" ]
```

#### A single-element stream is unchanged

```java
Stream
    .of("A")
    .gather(Gatherers4j.intersperse("-"))
    .toList();

// [ "A" ]
```

#### Intersperse `null`

```java
Stream
    .of("A", "B", "C")
    .gather(Gatherers4j.intersperse(null))
    .toList();

// [ "A", null, "B", null, "C" ]
```
