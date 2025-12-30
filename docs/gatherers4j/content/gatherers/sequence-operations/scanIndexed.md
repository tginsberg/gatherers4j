---
title: "scanIndexed()"
linkTitle: "scanIndexed()"
show_in_table: true
category: Sequence Operations
description: Perform a scan over every element in the input stream along with its index.

---

### Implementation Notes

Performs an ordered scan over the input stream, along with the index of the element being scanned/accumulated. This 
implementation attempts to behave like the non-indexing {{< jdklink linkName="Gatherers::scan()" package="java.base/java/util/stream/Gatherers.html#scan(java.util.function.Supplier,java.util.function.BiFunction)" >}}in
the JDK. For a folding version of this Gatherer, see [`foldIndexed()`](/gatherers4j/gatherers/sequence-operations/foldindexed/).

**Signature**

`scanIndexed(Supplier<OUTPUT> initialValue, IndexedAccumulatorFunction<OUTPUT, INPUT, OUTPUT> scanFunction)`

* `initialValue` - A non-null `Supplier` to provide the seed value for the scan (this implementation does not assume the first element is the seed)
* `scanFunction` - [`IndexedAccumulatorFunction`](https://github.com/tginsberg/gatherers4j/blob/main/src/main/java/com/ginsberg/gatherers4j/IndexedAccumulatorFunction.java) is a variation on a `BiFunction` which injects the `int` index of each element. This function does the actual scan/accumulation work.



### Examples

#### Perform an indexed scan

This example joins the index and the input strings to show how this works.

```java
Stream
    .of("A", "B", "C")
    .gather(
        Gatherers4j.scanIndexed(
            () -> "",
            (index, carry, next) -> carry + next + index
        )
    )
    .toList();

// [ "A0", "A0B1", "A0B1C2" ]

```

