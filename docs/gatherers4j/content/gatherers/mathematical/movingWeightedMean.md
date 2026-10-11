---
title: "movingWeightedMean()"
linkTitle: "movingWeightedMean()"
show_in_table: true
category: "Mathematical Operations"
description: Calculate the weighted moving mean of `BigDecimal` values over the previous `windowSize` number of values, giving newer values more weight.
---

### Implementation Notes
This implementation is suitable for `Stream<BigDecimal>`, for a version that takes a user-specified mapping function see [`movingWeightedMeanBy()`](/gatherers4j/gatherers/mathematical/movingweightedmeanby/).

A weighted moving mean works like [`movingMean()`](/gatherers4j/gatherers/mathematical/movingmean/), except that the values in the window do not count equally. With a `windowSize`, the oldest value in the window has a weight of 1, the next oldest has a weight of 2, and so on, up to the newest value, which has a weight of `windowSize`. The result is the sum of each value multiplied by its weight, divided by the sum of the weights. This is also known as the weighted moving average (WMA).

For example, if the window holds `10`, `20`, and `30` (oldest to newest), the weights are 1, 2, and 3, and the result is `(10 * 1 + 20 * 2 + 30 * 3) / 6 = 140 / 6 = 23.333...`. A plain mean of the same window is `20`.

Because newer values count for more, the weighted mean reacts to changes faster than `movingMean()`. Unlike the exponential moving average (see [`exponentialMovingAverageWithAlpha()`](/gatherers4j/gatherers/mathematical/exponentialmovingaveragewithalpha/)), where old values fade but never fully disappear, a value in a weighted moving mean stops counting completely as soon as it leaves the window.

Until the window is full, the newest weights are used, as with custom weights below: with a `windowSize` of 3 and two values in the window, the weights are 2 and 3 (the newest two of 1, 2, 3), so the window `10`, `20` gives `(10 * 2 + 20 * 3) / 5 = 16`, and the very first value gets a weight of 3 on its own.

By default, nulls are ignored and play no part in calculations, see `treatNullAs()` and `treatNullAsZero()` below for ways to change this behavior. The default `MathContext`
for all calculations is {{< jdklink linkName="MathContext.DECIMAL64" package="java.base/java/math/MathContext.html#DECIMAL64" >}}, but this can be overridden (see `withMathContext()`, below). Each input is rounded to the `MathContext` once, when it arrives, and the weighted sum is otherwise calculated exactly. Only the final division is rounded.

**Signatures**

`movingWeightedMean(int windowSize)`
* `windowSize` - How many trailing elements to average over at any given point in the stream. Must be greater than 1. Weights run from 1 (oldest) to `windowSize` (newest).

`movingWeightedMean(List<BigDecimal> weights)`
* `weights` - One weight per element in the window, **ordered from oldest to newest**. The window size is the number of weights, which must be at least 2. Weights may not be null or negative, and must not sum to zero. They do not need to sum to 1, because the result is always divided by the sum of the weights.

**Custom weights are ordered oldest to newest**

The first weight applies to the oldest element in the window, and the last weight applies to the newest. If you have weights in "newest first" order, reverse them before passing them in. A reversed list is accepted without complaint and gives you the opposite weighting, so it is worth double-checking (see [the example below](#reversing-the-weights-reverses-the-weighting)).

```java
// Oldest -> newest: the newest value counts for half of the result
List.of(new BigDecimal("0.2"), new BigDecimal("0.3"), new BigDecimal("0.5"))
```

While the window is not yet full, the **newest** weights are used and the result is divided by their sum. With weights `[1, 1, 2]` and only two values seen so far, the weights `[1, 2]` are used. This is the same rule `movingWeightedMean(int windowSize)` follows, so `movingWeightedMean(3)` gives exactly the same results as `movingWeightedMean(List.of(1, 2, 3))`, including while the window is filling.

Weights of zero are allowed. For example, `[1, 1, 0]` means "the average of the previous two values, excluding the current one". While the window is filling, only the newest weights are used, and if those happen to sum to zero the mean is undefined (0 divided by 0), so nothing is emitted for that element. With `[1, 1, 0]`, the first element emits nothing, because the only weight in use is the `0`. A list whose weights all sum to zero is rejected when the method is called.

```java
Stream
    .of("10", "20", "30", "40")
    .map(BigDecimal::new)
    .gather(Gatherers4j.movingWeightedMean(List.of(
        new BigDecimal("1"), new BigDecimal("1"), new BigDecimal("0")
    )))
    .toList();

// [10, 15, 25]
```

**Additional Methods**

| Method                                     | Purpose                                                                                                                                                                                                                                                                                                              |
|--------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `excludePartialValues()`                   | When calculating the weighted moving mean, and the full size of the window has not yet been reached, the gatherer should suppress emitting values until the lookback window is full. [See example.](#excluding-partial-values)                                                                                    |
| `treatNullAsZero()`                        | When encountering a `null` value in a stream, treat it as `BigDecimal.ZERO` instead. [See example.](#treating-null-as-zero)                                                                                                                                                                                          |
| `treatNullAs(BigDecimal replacement)`      | When encountering a `null` value in a stream, treat it as the given `replacement` value instead. [See example.](#replacing-null-with-another-bigdecimal)                                                                                                                                                             |
| `treatNullAsOne()`                         | When encountering a `null` value in a stream, treat it as `BigDecimal.ONE` instead.                                                                                                                                                                                                                                  |
| `withMathContext(MathContext mathContext)` | Replace the `MathContext` used for all mathematical operations performed by this gatherer. [See example.](#specifying-a-new-mathcontext)                                                                                                                                                                             |
| `withOriginal()`                           | Include the original input value from the stream in addition to the calculated value in a [`WithOriginal`](https://github.com/tginsberg/gatherers4j/blob/main/src/main/java/com/ginsberg/gatherers4j/dto/WithOriginal.java) record. [See example.](#emitting-a-record-containing-the-original-and-calculated-values) |

### Examples

#### Weighted moving mean of window size 3

The weights are 1, 2, and 3. For the third element, `(10 * 1 + 20 * 2 + 30 * 3) / 6 = 23.33...`.

```java
Stream
    .of("10", "20", "30", "40")
    .map(BigDecimal::new)
    .gather(Gatherers4j.movingWeightedMean(3))
    .toList();

// [
//   BigDecimal("10")
//   BigDecimal("16")
//   BigDecimal("23.33333333333333")
//   BigDecimal("33.33333333333333")
// ]
```

#### Reacting faster than `movingMean()`

When the input jumps from 10 to 50, the weighted mean follows sooner than the plain mean.

```java
Stream
    .of("10", "10", "10", "50", "50", "50")
    .map(BigDecimal::new)
    .gather(Gatherers4j.movingMean(3))
    .toList();

// [10, 10, 10, 23.33333333333333, 36.66666666666667, 50]

Stream
    .of("10", "10", "10", "50", "50", "50")
    .map(BigDecimal::new)
    .gather(Gatherers4j.movingWeightedMean(3))
    .toList();

// [10, 10, 10, 30, 43.33333333333333, 50]
```

#### Custom weights

Weights are ordered oldest to newest, so `[1, 1, 2]` counts the newest value twice as much as each of the other two. For the third element, `(10 * 1 + 20 * 1 + 30 * 2) / 4 = 22.5`. While the window is filling, the newest weights are used: the second element uses `[1, 2]`, giving `(10 * 1 + 20 * 2) / 3`.

```java
Stream
    .of("10", "20", "30", "40")
    .map(BigDecimal::new)
    .gather(Gatherers4j.movingWeightedMean(List.of(
        new BigDecimal("1"), new BigDecimal("1"), new BigDecimal("2")
    )))
    .toList();

// [
//   BigDecimal("10")
//   BigDecimal("16.66666666666667")
//   BigDecimal("22.5")
//   BigDecimal("32.5")
// ]
```

#### Custom weights for a forecast

Weights do not need to sum to 1, but when they do, the result reads as a blend. Here the newest value contributes 50%, the one before it 30%, and the oldest 20%: `100 * 0.2 + 120 * 0.3 + 150 * 0.5 = 131`.

```java
Stream
    .of("100", "120", "150", "140")
    .map(BigDecimal::new)
    .gather(Gatherers4j.movingWeightedMean(List.of(
        new BigDecimal("0.2"), new BigDecimal("0.3"), new BigDecimal("0.5")
    )))
    .toList();

// [
//   BigDecimal("100")
//   BigDecimal("112.5")
//   BigDecimal("131")
//   BigDecimal("139")
// ]
```

#### Reversing the weights reverses the weighting

The same weights in the opposite order put the most weight on the **oldest** value in the window, which is rarely what you want. Weights are always oldest to newest.

```java
Stream
    .of("100", "120", "150", "140")
    .map(BigDecimal::new)
    .gather(Gatherers4j.movingWeightedMean(List.of(
        new BigDecimal("0.5"), new BigDecimal("0.3"), new BigDecimal("0.2")
    )))
    .toList();

// [
//   BigDecimal("100")
//   BigDecimal("108")
//   BigDecimal("116")
//   BigDecimal("133")
// ]
```

#### Excluding partial values

Showing that in-process weighted moving mean values are not emitted for each element until the lookback window has been filled.

```java
Stream
    .of("10", "20", "30", "40")
    .map(BigDecimal::new)
    .gather(Gatherers4j.movingWeightedMean(3).excludePartialValues())
    .toList();

// [
//   BigDecimal("23.33333333333333")
//   BigDecimal("33.33333333333333")
// ]
```

#### Showing nulls are ignored by default

```java
Stream
    .of(null, null, new BigDecimal("10"), new BigDecimal("20"), new BigDecimal("30"))
    .gather(Gatherers4j.movingWeightedMean(3))
    .toList();

// [
//   BigDecimal("10")
//   BigDecimal("16")
//   BigDecimal("23.33333333333333")
// ]
```

#### Treating null as zero

```java
Stream
    .of(null, null, new BigDecimal("30"), new BigDecimal("60"))
    .gather(Gatherers4j.movingWeightedMean(3).treatNullAsZero())
    .toList();

// [
//   BigDecimal("0")
//   BigDecimal("0")
//   BigDecimal("15")
//   BigDecimal("40")
// ]
```

#### Replacing null with another `BigDecimal`

```java
Stream
    .of(null, null, new BigDecimal("30"), new BigDecimal("60"))
    .gather(Gatherers4j.movingWeightedMean(3).treatNullAs(new BigDecimal("3.5")))
    .toList();

// [
//   BigDecimal("3.5")
//   BigDecimal("3.5")
//   BigDecimal("16.75")
//   BigDecimal("40.58333333333333")
// ]
```

#### Specifying a new `MathContext`

```java
Stream
    .of("10", "20", "30", "40")
    .map(BigDecimal::new)
    .gather(Gatherers4j
        .movingWeightedMean(3)
        .withMathContext(new MathContext(3, RoundingMode.DOWN))
    )
    .toList();

// [
//   BigDecimal("10")
//   BigDecimal("16")
//   BigDecimal("23.3")
//   BigDecimal("33.3")
// ]
```

#### Emitting a record containing the original and calculated values

```java
Stream
    .of("10", "20", "30", "40")
    .map(BigDecimal::new)
    .gather(Gatherers4j.movingWeightedMean(3).withOriginal())
    .toList();

// [
//   WithOriginal[original=10, calculated=10]
//   WithOriginal[original=20, calculated=16]
//   WithOriginal[original=30, calculated=23.33333333333333]
//   WithOriginal[original=40, calculated=33.33333333333333]
// ]
```
