---
title: "foldIndexed()"
linkTitle: "foldIndexed()"
show_in_table: true
category: Sequence Operations
description: Perform a fold over every element in the input stream along with its index.

---

### Implementation Notes

Performs an ordered reduction over the input stream, along with the index of the element being reduced/folded. Because
this implementation produces a single result, it is probably not suitable to apply to infinite input streams. This 
implementation attempts to behave like the non-indexing {{< jdklink linkName="Gatherers::fold()" package="java.base/java/util/stream/Gatherers.html#fold(java.util.function.Supplier,java.util.function.BiFunction)" >}}in
the JDK. For a scanning version of this Gatherer, see [`scanIndexed()`](/gatherers4j/gatherers/sequence-operations/scanindexed/).

**Signature**

`foldIndexed(Supplier<OUTPUT> initialValue, IndexedAccumulatorFunction<OUTPUT, INPUT, OUTPUT> foldFunction)`

* `initialValue` - A non-null `Supplier` to provide the seed value for the fold (this implementation does not assume the first element is the seed)
* `foldFunction` - [`IndexedAccumulatorFunction`](https://github.com/tginsberg/gatherers4j/blob/main/src/main/java/com/ginsberg/gatherers4j/IndexedAccumulatorFunction.java) is a variation on a `BiFunction` which injects the `int` index of each element. This function does the actual fold/reduction work.



### Examples

#### Perform an indexed fold

This example joins the index and the input strings to show how this works.

```java
Stream
    .of("A", "B", "C", "D")
    .gather(
        Gatherers4j.foldIndexed(
            () -> "", // initialValue
            (index, carry, next) -> carry + String.format("%s%d", next, index)
        )
     )
     .forEach(System.out::println);

// A0B1C2D3
```

