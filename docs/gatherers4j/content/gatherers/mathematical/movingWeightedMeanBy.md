---
title: "movingWeightedMeanBy()"
linkTitle: "movingWeightedMeanBy()"
show_in_table: true
category: "Mathematical Operations"
description: Calculate the weighted moving mean of `BigDecimal` objects mapped from a `Stream<INPUT>` via a `mappingFunction` and looking back `windowSize` elements, giving newer values more weight.
---

### Implementation Notes
This implementation is suitable for mapping an arbitrary `Stream<INPUT>` to `BigDecimal` via a `mappingFunction`; for a version that operates directly on a `Stream<BigDecimal>`, see [`movingWeightedMean()`](/gatherers4j/gatherers/mathematical/movingweightedmean/).

A weighted moving mean works like [`movingMeanBy()`](/gatherers4j/gatherers/mathematical/movingmeanby/), except that the values in the window do not count equally. With a `windowSize`, the oldest value in the window has a weight of 1, the next oldest has a weight of 2, and so on, up to the newest value, which has a weight of `windowSize`. The result is the sum of each value multiplied by its weight, divided by the sum of the weights. This is also known as the weighted moving average (WMA).

For example, if the window holds `10`, `20`, and `30` (oldest to newest), the weights are 1, 2, and 3, and the result is `(10 * 1 + 20 * 2 + 30 * 3) / 6 = 140 / 6 = 23.333...`. A plain mean of the same window is `20`.

Because newer values count for more, the weighted mean reacts to changes faster than `movingMeanBy()`. Unlike the exponential moving average (see [`exponentialMovingAverageWithAlphaBy()`](/gatherers4j/gatherers/mathematical/exponentialmovingaveragewithalphaby/)), where old values fade but never fully disappear, a value in a weighted moving mean stops counting completely as soon as it leaves the window.

Until the window is full, the same rule is applied to the values seen so far: with two values in the window, the weights are 1 and 2.

By default, nulls are ignored and play no part in calculations, see `treatNullAs()` and `treatNullAsZero()` below for ways to change this behavior. The default `MathContext`
for all calculations is {{< jdklink linkName="MathContext.DECIMAL64" package="java.base/java/math/MathContext.html#DECIMAL64" >}}, but this can be overridden (see `withMathContext()`, below). Each input is rounded to the `MathContext` once, when it arrives, and the weighted sum is otherwise calculated exactly. Only the final division is rounded.

**Signatures**

`movingWeightedMeanBy(int windowSize, Function<INPUT, BigDecimal> mappingFunction)`
* `windowSize` - How many trailing elements to average over at any given point in the stream. Must be greater than 1. Weights run from 1 (oldest) to `windowSize` (newest).
* `mappingFunction` - A non-null function to map stream `INPUT` elements into `BigDecimal` for calculation

`movingWeightedMeanBy(List<BigDecimal> weights, Function<INPUT, BigDecimal> mappingFunction)`
* `weights` - One weight per element in the window, **ordered from oldest to newest**. The window size is the number of weights, which must be at least 2. Weights may not be null or negative, and must not sum to zero. They do not need to sum to 1, because the result is always divided by the sum of the weights.
* `mappingFunction` - A non-null function to map stream `INPUT` elements into `BigDecimal` for calculation

**Custom weights are ordered oldest to newest**

The first weight applies to the oldest element in the window, and the last weight applies to the newest. If you have weights in "newest first" order, reverse them before passing them in. A reversed list is accepted without complaint and gives you the opposite weighting, so it is worth double-checking (see [`movingWeightedMean()`](/gatherers4j/gatherers/mathematical/movingweightedmean/#reversing-the-weights-reverses-the-weighting) for an example).

```java
// Oldest -> newest: the newest value counts for half of the result
List.of(new BigDecimal("0.2"), new BigDecimal("0.3"), new BigDecimal("0.5"))
```

While the window is not yet full, the **newest** weights are used and the result is divided by their sum. With weights `[1, 1, 2]` and only two values seen so far, the weights `[1, 2]` are used. This means that `movingWeightedMeanBy(3, fn)` and `movingWeightedMeanBy(List.of(1, 2, 3), fn)` give the same results once the window is full, but not while it is filling.

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

#### Weighted moving mean of window size 3, mapped from an object

The weights are 1, 2, and 3. For the third element, `(10 * 1 + 20 * 2 + 30 * 3) / 6 = 23.33...`.

```java
record NamedValue(String name, BigDecimal value) {}

Stream
    .of(
        new NamedValue("first",  new BigDecimal("10")),
        new NamedValue("second", new BigDecimal("20")),
        new NamedValue("third",  new BigDecimal("30")),
        new NamedValue("fourth", new BigDecimal("40"))
    )
    .gather(Gatherers4j.movingWeightedMeanBy(3, NamedValue::value))
    .toList();

// [
//   BigDecimal("10")
//   BigDecimal("16.66666666666667")
//   BigDecimal("23.33333333333333")
//   BigDecimal("33.33333333333333")
// ]
```

#### Custom weights

Weights are ordered oldest to newest, so `[1, 1, 2]` counts the newest value twice as much as each of the other two. For the third element, `(10 * 1 + 20 * 1 + 30 * 2) / 4 = 22.5`.

```java
record NamedValue(String name, BigDecimal value) {}

Stream
    .of(
        new NamedValue("first",  new BigDecimal("10")),
        new NamedValue("second", new BigDecimal("20")),
        new NamedValue("third",  new BigDecimal("30")),
        new NamedValue("fourth", new BigDecimal("40"))
    )
    .gather(Gatherers4j.movingWeightedMeanBy(
        List.of(new BigDecimal("1"), new BigDecimal("1"), new BigDecimal("2")),
        NamedValue::value
    ))
    .toList();

// [
//   BigDecimal("10")
//   BigDecimal("16.66666666666667")
//   BigDecimal("22.5")
//   BigDecimal("32.5")
// ]
```

#### Custom weights for a forecast

Here the newest value contributes 50%, the one before it 30%, and the oldest 20%: `100 * 0.2 + 120 * 0.3 + 150 * 0.5 = 131`.

```java
record NamedValue(String name, BigDecimal value) {}

Stream
    .of(
        new NamedValue("Mon", new BigDecimal("100")),
        new NamedValue("Tue", new BigDecimal("120")),
        new NamedValue("Wed", new BigDecimal("150")),
        new NamedValue("Thu", new BigDecimal("140"))
    )
    .gather(Gatherers4j.movingWeightedMeanBy(
        List.of(new BigDecimal("0.2"), new BigDecimal("0.3"), new BigDecimal("0.5")),
        NamedValue::value
    ))
    .toList();

// [
//   BigDecimal("100")
//   BigDecimal("112.5")
//   BigDecimal("131")
//   BigDecimal("139")
// ]
```

#### Excluding partial values

Showing that in-process weighted moving mean values are not emitted for each element until the lookback window has been filled.

```java
record NamedValue(String name, BigDecimal value) {}

Stream
    .of(
        new NamedValue("first",  new BigDecimal("10")),
        new NamedValue("second", new BigDecimal("20")),
        new NamedValue("third",  new BigDecimal("30")),
        new NamedValue("fourth", new BigDecimal("40"))
    )
    .gather(Gatherers4j.movingWeightedMeanBy(3, NamedValue::value).excludePartialValues())
    .toList();

// [
//   BigDecimal("23.33333333333333")
//   BigDecimal("33.33333333333333")
// ]
```

#### Showing nulls are ignored by default

```java
record NamedValue(String name, BigDecimal value) {}

Stream
    .of(
        new NamedValue("first",  null),
        new NamedValue("second", null),
        new NamedValue("third",  new BigDecimal("30")),
        new NamedValue("fourth", new BigDecimal("60"))
    )
    .gather(Gatherers4j.movingWeightedMeanBy(3, NamedValue::value))
    .toList();

// [
//   BigDecimal("30")
//   BigDecimal("50")
// ]
```

#### Treating null as zero

```java
record NamedValue(String name, BigDecimal value) {}

Stream
    .of(
        new NamedValue("first",  null),
        new NamedValue("second", null),
        new NamedValue("third",  new BigDecimal("30")),
        new NamedValue("fourth", new BigDecimal("60"))
    )
    .gather(Gatherers4j.movingWeightedMeanBy(3, NamedValue::value).treatNullAsZero())
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
record NamedValue(String name, BigDecimal value) {}

Stream
    .of(
        new NamedValue("first",  null),
        new NamedValue("second", null),
        new NamedValue("third",  new BigDecimal("30")),
        new NamedValue("fourth", new BigDecimal("60"))
    )
    .gather(Gatherers4j.movingWeightedMeanBy(3, NamedValue::value).treatNullAs(new BigDecimal("3.5")))
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
record NamedValue(String name, BigDecimal value) {}

Stream
    .of(
        new NamedValue("first",  new BigDecimal("10")),
        new NamedValue("second", new BigDecimal("20")),
        new NamedValue("third",  new BigDecimal("30")),
        new NamedValue("fourth", new BigDecimal("40"))
    )
    .gather(Gatherers4j
        .movingWeightedMeanBy(3, NamedValue::value)
        .withMathContext(new MathContext(3, RoundingMode.DOWN))
    )
    .toList();

// [
//   BigDecimal("10")
//   BigDecimal("16.6")
//   BigDecimal("23.3")
//   BigDecimal("33.3")
// ]
```

#### Emitting a record containing the original and calculated values

```java
record NamedValue(String name, BigDecimal value) {}

Stream
    .of(
        new NamedValue("first",  new BigDecimal("10")),
        new NamedValue("second", new BigDecimal("20")),
        new NamedValue("third",  new BigDecimal("30")),
        new NamedValue("fourth", new BigDecimal("40"))
    )
    .gather(Gatherers4j.movingWeightedMeanBy(3, NamedValue::value).withOriginal())
    .toList();

// [
//   WithOriginal[original=NamedValue[name=first, value=10], calculated=10]
//   WithOriginal[original=NamedValue[name=second, value=20], calculated=16.66666666666667]
//   WithOriginal[original=NamedValue[name=third, value=30], calculated=23.33333333333333]
//   WithOriginal[original=NamedValue[name=fourth, value=40], calculated=33.33333333333333]
// ]
```
