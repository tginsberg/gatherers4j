---
title: "debounce()"
linkTitle: "debounce()"
show_in_table: true
category: Filtering and Selection
description: Limit the number of elements in the stream to some number per period, dropping anything over the limit during the period.
---


### Implementation Notes

Time is measured on a best-effort basis and may not be suitable for cases where a high precision clock is required.
For a version of this that pauses the stream instead of dropping elements, see the [`throttle()`](/gatherers4j/gatherers/sequence-operations/throttle/)Gatherer. 

**Signature**

`debounce(int amount, Duration duration)`

* `amount` - A positive number of elements to allow over the `duration`
* `duration` - A non-null `Duration` over which to limit element production

### Examples

#### Limit to 2 per 50 milliseconds

This shows a debouncer that keeps 2 elements every 50ms. First we record the `start` time so we can crudely measure elapsed time in milliseconds.
Next, we start a range of `Integer` objects and `debounce` them. We see the original int and the elapsed time in the `Pair` we `map` to. Finally,
we print the results.

```java
long start = System.currentTimeMillis();

IntStream
    .range(1, 10_000_000)
    .boxed()
    .gather(Gatherers4j.debounce(2, Duration.ofMillis(50)))
    .map(it -> new Pair<>(it, System.currentTimeMillis()-start))
    .forEach(System.out::println);

// Prints
Pair[first=1, second=12]
Pair[first=2, second=15]
Pair[first=8915384, second=62]
Pair[first=8915385, second=62]
```

