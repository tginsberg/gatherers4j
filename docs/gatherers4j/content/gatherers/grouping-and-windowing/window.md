---
title: "window()"
linkTitle: "window()"
show_in_table: true
category: Grouping and Windowing
description: Create windows over the elements of the input stream that are `windowSize` in length, sliding over `stepping` elements and optionally including partial windows at the end of the stream.
---


### Implementation Notes

The lists returned from this Gatherer are unmodifiable. The `stepping` may be larger than the `windowSize`, in which case elements are skipped.

Fun fact: calling `window(2, 1, true)` or `window(2, 1, false)` is the same as [`zipWithNext()`](/gatherers4j/gatherers/sequence-operations/zipwithnext/)!

**Signature**

`window(int windowSize, int stepping, boolean includePartials)`
* `windowSize` - How many elements to include in each `List`, must be positive.
* `stepping` - How many elements to slide over each iteration, must be positive.
* `includePartials` - Emit any partially constructed windows at the end of the stream.

### Examples

#### Window size 2, stepping 2, not including partials

```java
Stream
    .of("A", "B", "C", "D", "E", "F", "G")
    .gather(Gatherers4j.window(2, 2, false))
    .toList();

// [ ["A", "B"], ["C", "D"], ["E", "F"] ]
```

#### Window size 2, stepping 3, including partials

```java
Stream
    .of("A", "B", "C", "D", "E", "F", "G")
    .gather(Gatherers4j.window(2, 3, true))
    .toList();

// [ ["A", "B"], ["D", "E"], ["G"] ]
```

#### Window size 5, stepping 2, including partials

This demonstrates the behavior of multiple partials.

```java
Stream
     .of("A", "B", "C", "D", "E", "F", "G")
     .gather(Gatherers4j.window(5, 2, true))
     .toList();

// [ 
//   ["A", "B", "C", "D", "E"]
//   ["C", "D", "E", "F", "G"]
//   ["E", "F", "G"]
//   ["G"]
// ]
```