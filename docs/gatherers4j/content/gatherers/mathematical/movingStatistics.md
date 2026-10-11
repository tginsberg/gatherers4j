---
title: "movingStatistics()"
linkTitle: "movingStatistics()"
show_in_table: true
category: "Mathematical Operations"
description: Calculate summary statistics (count, sum, mean, min, max, variance, and standard deviation) of a `Stream<BigDecimal>` looking back `windowSize` elements.

---

### Implementation Notes
This implementation is suitable for `Stream<BigDecimal>`, for a version that takes a user-specified mapping function see [`movingStatisticsBy()`](/gatherers4j/gatherers/mathematical/movingstatisticsby/).
By default, nulls are ignored and play no part in calculations, see `treatNullAs()` and `treatNullAsZero()` below for ways to change this behavior. The default `MathContext`
for all calculations is {{< jdklink linkName="MathContext.DECIMAL64" package="java.base/java/math/MathContext.html#DECIMAL64" >}}, but this can be overridden (see `withMathContext()`, below).

Each emitted [`StatisticalSummary`](https://github.com/tginsberg/gatherers4j/blob/main/src/main/java/com/ginsberg/gatherers4j/dto/StatisticalSummary.java) record describes the values in the trailing window, which holds up to `windowSize` elements:

| Field                         | Meaning                                                                                                   |
|-------------------------------|-----------------------------------------------------------------------------------------------------------|
| `count`                       | How many values are summarized. Ignored `null` values are not counted.                                    |
| `sum`                         | The sum of the values                                                                                     |
| `mean`                        | The arithmetic mean of the values                                                                         |
| `min`                         | The smallest value                                                                                        |
| `max`                         | The largest value                                                                                         |
| `populationVariance`          | The variance, treating the values as the entire dataset (divided by *n*)                                  |
| `populationStandardDeviation` | The standard deviation, treating the values as the entire dataset                                         |
| `sampleVariance`              | The variance, treating the values as a sample of a larger population (divided by *n - 1*). May be `null`. |
| `sampleStandardDeviation`     | The standard deviation, treating the values as a sample of a larger population. May be `null`.            |

The sample variance and sample standard deviation of a single value are undefined, so `sampleVariance` and `sampleStandardDeviation` are `null` until at least two values have been summarized. Every other field always has a value.

Every value is the same as the corresponding single-statistic gatherer would produce for the same input. Each summary calculates every field, including two square roots, so if you only need one statistic, the dedicated gatherer is cheaper.

**Signatures**

`movingStatistics(int windowSize)`
* `windowSize` - How many trailing elements to summarize, must be greater than 1

**Additional Methods**

| Method                                     | Purpose                                                                                                                                                                                                                                                                                                              |
|--------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `excludePartialValues()`                   | When the full size of the window has not yet been reached, suppress emitting summaries until the lookback window is full. [See example.](#excluding-partial-values)                                                                                                                                                  |
| `treatNullAsZero()`                        | When encountering a `null` value in a stream, treat it as `BigDecimal.ZERO` instead. [See example.](#treating-null-as-zero)                                                                                                                                                                                          |
| `treatNullAs(BigDecimal replacement)`      | When encountering a `null` value in a stream, treat it as the given `replacement` value instead. [See example.](#replacing-null-with-another-bigdecimal)                                                                                                                                                             |
| `treatNullAsOne()`                         | When encountering a `null` value in a stream, treat it as `BigDecimal.ONE` instead.                                                                                                                                                                                                                                  |
| `withMathContext(MathContext mathContext)` | Replace the `MathContext` used for all mathematical operations performed by this gatherer. [See example.](#specifying-a-new-mathcontext)                                                                                                                                                                             |
| `withOriginal()`                           | Include the original input value from the stream in addition to the calculated value in a [`WithOriginal`](https://github.com/tginsberg/gatherers4j/blob/main/src/main/java/com/ginsberg/gatherers4j/dto/WithOriginal.java) record. [See example.](#emitting-a-record-containing-the-original-and-calculated-values) |

### Examples

#### Moving statistics of window size 3

```java
Stream
    .of("1.0", "2.0", "10.0", "2.0")
    .map(BigDecimal::new)
    .gather(Gatherers4j.movingStatistics(3))
    .toList();

// [
//   StatisticalSummary[count=1, sum=1.0, mean=1.0, min=1.0, max=1.0, populationVariance=0.00, populationStandardDeviation=0.0, sampleVariance=null, sampleStandardDeviation=null]
//   StatisticalSummary[count=2, sum=3.0, mean=1.5, min=1.0, max=2.0, populationVariance=0.25, populationStandardDeviation=0.5, sampleVariance=0.50, sampleStandardDeviation=0.7071067811865475]
//   StatisticalSummary[count=3, sum=13.0, mean=4.333333333333333, min=1.0, max=10.0, populationVariance=16.22222222222222, populationStandardDeviation=4.02768199119819, sampleVariance=24.33333333333333, sampleStandardDeviation=4.932882862316247]
//   StatisticalSummary[count=3, sum=14.0, mean=4.666666666666667, min=2.0, max=10.0, populationVariance=14.22222222222222, populationStandardDeviation=3.771236166328253, sampleVariance=21.33333333333333, sampleStandardDeviation=4.618802153517006]
// ]
```

#### Combining statistics from the same window

Because every statistic for a window arrives together, combining them takes a single `map`. This example calculates a band two standard deviations either side of the mean (as in Bollinger Bands).

```java
Stream
    .of("1.0", "2.0", "10.0", "2.0")
    .map(BigDecimal::new)
    .gather(Gatherers4j.movingStatistics(3))
    .map(it -> {
        final BigDecimal width = it.populationStandardDeviation().multiply(BigDecimal.TWO);
        return List.of(it.mean().subtract(width), it.mean(), it.mean().add(width));
    })
    .toList();

// [
//   [1.0, 1.0, 1.0]
//   [0.5, 1.5, 2.5]
//   [-3.722030649063047, 4.333333333333333, 12.388697315729713]
//   [-2.875805665989839, 4.666666666666667, 12.209138999323173]
// ]
```

#### Excluding partial values

Showing that summaries are not emitted until the lookback window has been filled.

```java
Stream
    .of("1.0", "2.0", "10.0", "2.0")
    .map(BigDecimal::new)
    .gather(Gatherers4j.movingStatistics(3).excludePartialValues())
    .toList();

// [
//   StatisticalSummary[count=3, sum=13.0, mean=4.333333333333333, min=1.0, max=10.0, populationVariance=16.22222222222222, populationStandardDeviation=4.02768199119819, sampleVariance=24.33333333333333, sampleStandardDeviation=4.932882862316247]
//   StatisticalSummary[count=3, sum=14.0, mean=4.666666666666667, min=2.0, max=10.0, populationVariance=14.22222222222222, populationStandardDeviation=3.771236166328253, sampleVariance=21.33333333333333, sampleStandardDeviation=4.618802153517006]
// ]
```

#### Showing nulls are ignored by default

```java
Stream
    .of(null, null, new BigDecimal("10.0"), new BigDecimal("2.0"))
    .gather(Gatherers4j.movingStatistics(3))
    .toList();

// [
//   StatisticalSummary[count=1, sum=10.0, mean=10.0, min=10.0, max=10.0, populationVariance=0.00, populationStandardDeviation=0.0, sampleVariance=null, sampleStandardDeviation=null]
//   StatisticalSummary[count=2, sum=12.0, mean=6.0, min=2.0, max=10.0, populationVariance=16.00, populationStandardDeviation=4.0, sampleVariance=32.00, sampleStandardDeviation=5.65685424949238]
// ]
```

#### Treating null as zero

```java
Stream
    .of(null, null, new BigDecimal("10.0"), new BigDecimal("2.0"))
    .gather(Gatherers4j.movingStatistics(3).treatNullAsZero())
    .toList();

// [
//   StatisticalSummary[count=1, sum=0, mean=0, min=0, max=0, populationVariance=0, populationStandardDeviation=0, sampleVariance=null, sampleStandardDeviation=null]
//   StatisticalSummary[count=2, sum=0, mean=0, min=0, max=0, populationVariance=0, populationStandardDeviation=0, sampleVariance=0, sampleStandardDeviation=0]
//   StatisticalSummary[count=3, sum=10.0, mean=3.333333333333333, min=0, max=10.0, populationVariance=22.22222222222222, populationStandardDeviation=4.714045207910317, sampleVariance=33.33333333333333, sampleStandardDeviation=5.773502691896257]
//   StatisticalSummary[count=3, sum=12.0, mean=4.0, min=0, max=10.0, populationVariance=18.66666666666667, populationStandardDeviation=4.320493798938574, sampleVariance=28.00, sampleStandardDeviation=5.291502622129181]
// ]
```

#### Replacing null with another `BigDecimal`

```java
Stream
    .of(null, null, new BigDecimal("10.0"), new BigDecimal("2.0"))
    .gather(Gatherers4j.movingStatistics(3).treatNullAs(new BigDecimal("3.5")))
    .toList();

// [
//   StatisticalSummary[count=1, sum=3.5, mean=3.5, min=3.5, max=3.5, populationVariance=0.00, populationStandardDeviation=0.0, sampleVariance=null, sampleStandardDeviation=null]
//   StatisticalSummary[count=2, sum=7.0, mean=3.5, min=3.5, max=3.5, populationVariance=0.00, populationStandardDeviation=0.0, sampleVariance=0.00, sampleStandardDeviation=0.0]
//   StatisticalSummary[count=3, sum=17.0, mean=5.666666666666667, min=3.5, max=10.0, populationVariance=9.388888888888889, populationStandardDeviation=3.064129385141706, sampleVariance=14.08333333333333, sampleStandardDeviation=3.752776749732567]
//   StatisticalSummary[count=3, sum=15.5, mean=5.166666666666667, min=2.0, max=10.0, populationVariance=12.05555555555556, populationStandardDeviation=3.472111109333277, sampleVariance=18.08333333333333, sampleStandardDeviation=4.252450274057691]
// ]
```

#### Specifying a new `MathContext`

```java
Stream
    .of("1.0", "2.0", "10.0", "2.0")
    .map(BigDecimal::new)
    .gather(Gatherers4j
        .movingStatistics(3)
        .withMathContext(new MathContext(3, RoundingMode.DOWN))
    )
    .toList();

// [
//   StatisticalSummary[count=1, sum=1.0, mean=1.0, min=1.0, max=1.0, populationVariance=0.00, populationStandardDeviation=0.0, sampleVariance=null, sampleStandardDeviation=null]
//   StatisticalSummary[count=2, sum=3.0, mean=1.5, min=1.0, max=2.0, populationVariance=0.25, populationStandardDeviation=0.5, sampleVariance=0.50, sampleStandardDeviation=0.707]
//   StatisticalSummary[count=3, sum=13.0, mean=4.33, min=1.0, max=10.0, populationVariance=16.2, populationStandardDeviation=4.02, sampleVariance=24.3, sampleStandardDeviation=4.92]
//   StatisticalSummary[count=3, sum=14.0, mean=4.66, min=2.0, max=10.0, populationVariance=14.2, populationStandardDeviation=3.76, sampleVariance=21.3, sampleStandardDeviation=4.61]
// ]
```

#### Emitting a record containing the original and calculated values

```java
Stream
    .of("1.0", "2.0", "10.0", "2.0")
    .map(BigDecimal::new)
    .gather(Gatherers4j.movingStatistics(3).withOriginal())
    .toList();

// [
//   WithOriginal[original=1.0, calculated=StatisticalSummary[count=1, sum=1.0, mean=1.0, min=1.0, max=1.0, populationVariance=0.00, populationStandardDeviation=0.0, sampleVariance=null, sampleStandardDeviation=null]]
//   WithOriginal[original=2.0, calculated=StatisticalSummary[count=2, sum=3.0, mean=1.5, min=1.0, max=2.0, populationVariance=0.25, populationStandardDeviation=0.5, sampleVariance=0.50, sampleStandardDeviation=0.7071067811865475]]
//   WithOriginal[original=10.0, calculated=StatisticalSummary[count=3, sum=13.0, mean=4.333333333333333, min=1.0, max=10.0, populationVariance=16.22222222222222, populationStandardDeviation=4.02768199119819, sampleVariance=24.33333333333333, sampleStandardDeviation=4.932882862316247]]
//   WithOriginal[original=2.0, calculated=StatisticalSummary[count=3, sum=14.0, mean=4.666666666666667, min=2.0, max=10.0, populationVariance=14.22222222222222, populationStandardDeviation=3.771236166328253, sampleVariance=21.33333333333333, sampleStandardDeviation=4.618802153517006]]
// ]
```
