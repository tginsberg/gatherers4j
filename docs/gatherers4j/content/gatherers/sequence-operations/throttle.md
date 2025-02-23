---
title: "throttle()"
linkTitle: "throttle()"
show_in_table: true
category: Sequence Operations
description: Limit the number of elements in the stream to some number per period. When the limit is reached consumption is paused until a new period starts and the count resets.
---


### Implementation Notes

Time is measured on a best-effort basis and may not be suitable for cases where a high precision clock is required.
For a version of this that drops elements from the stream instead of pausing, see the [`debounce()`](/gatherers4j/gatherers/filtering-and-selection/debounce/)Gatherer. 

**Signature**

`throttle(int amount, Duration duration)`

* `amount` - A positive number of elements to allow over the `duration`
* `duration` - A non-null `Duration` over which to limit element production

### Examples

#### Limit to 2 per 100 milliseconds

This shows a throttler that allows 2 elements every 100ms. First we record the `start` time so we can crudely measure elapsed time in milliseconds.
Next, we start a range of `Integer` objects and `debounce` them. We see the original int and the elapsed time in the `Pair` we `map` to. Finally,
we print the results.

```java
long start = System.currentTimeMillis();

IntStream
    .range(1, 7)
    .boxed()
    .gather(Gatherers4j.throttle(2, Duration.ofMillis(100)))
    .map(it -> new Pair<>(it, System.currentTimeMillis()-start))
    .forEach(System.out::println);

// Prints
Pair[first=1, second=0]
Pair[first=2, second=10]
Pair[first=3, second=107]
Pair[first=4, second=107]
Pair[first=5, second=211]
Pair[first=6, second=211]
```

