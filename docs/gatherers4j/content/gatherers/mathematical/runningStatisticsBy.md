---
title: "runningStatisticsBy()"
linkTitle: "runningStatisticsBy()"
show_in_table: true
category: "Mathematical Operations"
description: Calculate summary statistics (count, sum, mean, min, max, variance, and standard deviation) of `BigDecimal` objects mapped from a `Stream<INPUT>` via a `mappingFunction` so far.

---

### Implementation Notes
This implementation is suitable for mapping an arbitrary `Stream<INPUT>` to `BigDecimal` via a `mappingFunction`; for a version that operates directly on a `Stream<BigDecimal>`, see [`runningStatistics()`](/gatherers4j/gatherers/mathematical/runningstatistics/).
By default, nulls are ignored and play no part in calculations, see `treatNullAs()` and `treatNullAsZero()` below for ways to change this behavior. The default `MathContext`
for all calculations is {{< jdklink linkName="MathContext.DECIMAL64" package="java.base/java/math/MathContext.html#DECIMAL64" >}}, but this can be overridden (see `withMathContext()`, below).

Each emitted [`StatisticalSummary`](https://github.com/tginsberg/gatherers4j/blob/main/src/main/java/com/ginsberg/gatherers4j/dto/StatisticalSummary.java) record describes every mapped value seen so far:

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

`runningStatisticsBy(Function<? super INPUT, ? extends BigDecimal> mappingFunction)`
* `mappingFunction` - A non-null function to map stream `INPUT` elements into `BigDecimal` for calculation

**Additional Methods**

| Method                                     | Purpose                                                                                                                                                                                                                                                                                                              |
|--------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `treatNullAsZero()`                        | When encountering a `null` value in a stream, treat it as `BigDecimal.ZERO` instead. [See example.](#treating-null-as-zero)                                                                                                                                                                                          |
| `treatNullAs(BigDecimal replacement)`      | When encountering a `null` value in a stream, treat it as the given `replacement` value instead. [See example.](#replacing-null-with-another-bigdecimal)                                                                                                                                                             |
| `treatNullAsOne()`                         | When encountering a `null` value in a stream, treat it as `BigDecimal.ONE` instead.                                                                                                                                                                                                                                  |
| `withMathContext(MathContext mathContext)` | Replace the `MathContext` used for all mathematical operations performed by this gatherer. [See example.](#specifying-a-new-mathcontext)                                                                                                                                                                             |
| `withOriginal()`                           | Include the original input value from the stream in addition to the calculated value in a [`WithOriginal`](https://github.com/tginsberg/gatherers4j/blob/main/src/main/java/com/ginsberg/gatherers4j/dto/WithOriginal.java) record. [See example.](#emitting-a-record-containing-the-original-and-calculated-values) |

### Examples

#### Running statistics, mapped from an object

```java
record NamedValue(String name, BigDecimal value) {}

Stream
    .of(
        new NamedValue("first",  new BigDecimal("1.0")),
        new NamedValue("second", new BigDecimal("2.0")),
        new NamedValue("third",  new BigDecimal("10.0")),
        new NamedValue("fourth", new BigDecimal("2.0"))
    )
    .gather(Gatherers4j.runningStatisticsBy(NamedValue::value))
    .toList();

// [
//   StatisticalSummary[count=1, sum=1.0, mean=1.0, min=1.0, max=1.0, populationVariance=0.00, populationStandardDeviation=0.0, sampleVariance=null, sampleStandardDeviation=null]
//   StatisticalSummary[count=2, sum=3.0, mean=1.5, min=1.0, max=2.0, populationVariance=0.25, populationStandardDeviation=0.5, sampleVariance=0.50, sampleStandardDeviation=0.7071067811865475]
//   StatisticalSummary[count=3, sum=13.0, mean=4.333333333333333, min=1.0, max=10.0, populationVariance=16.22222222222222, populationStandardDeviation=4.02768199119819, sampleVariance=24.33333333333333, sampleStandardDeviation=4.932882862316247]
//   StatisticalSummary[count=4, sum=15.0, mean=3.75, min=1.0, max=10.0, populationVariance=13.1875, populationStandardDeviation=3.631459761583488, sampleVariance=17.58333333333333, sampleStandardDeviation=4.193248541803041]
// ]
```

#### Showing nulls are ignored by default

```java
record NamedValue(String name, BigDecimal value) {}

Stream
    .of(
        new NamedValue("first",  null),
        new NamedValue("second", null),
        new NamedValue("third",  new BigDecimal("10.0")),
        new NamedValue("fourth", new BigDecimal("2.0"))
    )
    .gather(Gatherers4j.runningStatisticsBy(NamedValue::value))
    .toList();

// [
//   StatisticalSummary[count=1, sum=10.0, mean=10.0, min=10.0, max=10.0, populationVariance=0.00, populationStandardDeviation=0.0, sampleVariance=null, sampleStandardDeviation=null]
//   StatisticalSummary[count=2, sum=12.0, mean=6.0, min=2.0, max=10.0, populationVariance=16.00, populationStandardDeviation=4.0, sampleVariance=32.00, sampleStandardDeviation=5.65685424949238]
// ]
```

#### Treating null as zero

```java
record NamedValue(String name, BigDecimal value) {}

Stream
    .of(
        new NamedValue("first",  null),
        new NamedValue("second", null),
        new NamedValue("third",  new BigDecimal("10.0")),
        new NamedValue("fourth", new BigDecimal("2.0"))
    )
    .gather(Gatherers4j.runningStatisticsBy(NamedValue::value).treatNullAsZero())
    .toList();

// [
//   StatisticalSummary[count=1, sum=0, mean=0, min=0, max=0, populationVariance=0, populationStandardDeviation=0, sampleVariance=null, sampleStandardDeviation=null]
//   StatisticalSummary[count=2, sum=0, mean=0, min=0, max=0, populationVariance=0, populationStandardDeviation=0, sampleVariance=0, sampleStandardDeviation=0]
//   StatisticalSummary[count=3, sum=10.0, mean=3.333333333333333, min=0, max=10.0, populationVariance=22.22222222222222, populationStandardDeviation=4.714045207910317, sampleVariance=33.33333333333333, sampleStandardDeviation=5.773502691896257]
//   StatisticalSummary[count=4, sum=12.0, mean=3.0, min=0, max=10.0, populationVariance=17.00, populationStandardDeviation=4.123105625617661, sampleVariance=22.66666666666667, sampleStandardDeviation=4.760952285695234]
// ]
```

#### Replacing null with another `BigDecimal`

```java
record NamedValue(String name, BigDecimal value) {}

Stream
    .of(
        new NamedValue("first",  null),
        new NamedValue("second", null),
        new NamedValue("third",  new BigDecimal("10.0")),
        new NamedValue("fourth", new BigDecimal("2.0"))
    )
    .gather(Gatherers4j.runningStatisticsBy(NamedValue::value).treatNullAs(new BigDecimal("3.5")))
    .toList();

// [
//   StatisticalSummary[count=1, sum=3.5, mean=3.5, min=3.5, max=3.5, populationVariance=0.00, populationStandardDeviation=0.0, sampleVariance=null, sampleStandardDeviation=null]
//   StatisticalSummary[count=2, sum=7.0, mean=3.5, min=3.5, max=3.5, populationVariance=0.00, populationStandardDeviation=0.0, sampleVariance=0.00, sampleStandardDeviation=0.0]
//   StatisticalSummary[count=3, sum=17.0, mean=5.666666666666667, min=3.5, max=10.0, populationVariance=9.388888888888889, populationStandardDeviation=3.064129385141706, sampleVariance=14.08333333333333, sampleStandardDeviation=3.752776749732567]
//   StatisticalSummary[count=4, sum=19.0, mean=4.75, min=2.0, max=10.0, populationVariance=9.5625, populationStandardDeviation=3.092329219213245, sampleVariance=12.75, sampleStandardDeviation=3.570714214271425]
// ]
```

#### Specifying a new `MathContext`

```java
record NamedValue(String name, BigDecimal value) {}

Stream
    .of(
        new NamedValue("first",  new BigDecimal("1.0")),
        new NamedValue("second", new BigDecimal("2.0")),
        new NamedValue("third",  new BigDecimal("10.0")),
        new NamedValue("fourth", new BigDecimal("2.0"))
    )
    .gather(Gatherers4j
        .runningStatisticsBy(NamedValue::value)
        .withMathContext(new MathContext(3, RoundingMode.DOWN))
    )
    .toList();

// [
//   StatisticalSummary[count=1, sum=1.0, mean=1.0, min=1.0, max=1.0, populationVariance=0.00, populationStandardDeviation=0.0, sampleVariance=null, sampleStandardDeviation=null]
//   StatisticalSummary[count=2, sum=3.0, mean=1.5, min=1.0, max=2.0, populationVariance=0.25, populationStandardDeviation=0.5, sampleVariance=0.50, sampleStandardDeviation=0.707]
//   StatisticalSummary[count=3, sum=13.0, mean=4.33, min=1.0, max=10.0, populationVariance=16.2, populationStandardDeviation=4.02, sampleVariance=24.3, sampleStandardDeviation=4.92]
//   StatisticalSummary[count=4, sum=15.0, mean=3.75, min=1.0, max=10.0, populationVariance=13.1, populationStandardDeviation=3.61, sampleVariance=17.5, sampleStandardDeviation=4.18]
// ]
```

#### Emitting a record containing the original and calculated values

```java
record NamedValue(String name, BigDecimal value) {}

Stream
    .of(
        new NamedValue("first",  new BigDecimal("1.0")),
        new NamedValue("second", new BigDecimal("2.0")),
        new NamedValue("third",  new BigDecimal("10.0")),
        new NamedValue("fourth", new BigDecimal("2.0"))
    )
    .gather(Gatherers4j.runningStatisticsBy(NamedValue::value).withOriginal())
    .toList();

// [
//   WithOriginal[original=NamedValue[name=first, value=1.0], calculated=StatisticalSummary[count=1, sum=1.0, mean=1.0, min=1.0, max=1.0, populationVariance=0.00, populationStandardDeviation=0.0, sampleVariance=null, sampleStandardDeviation=null]]
//   WithOriginal[original=NamedValue[name=second, value=2.0], calculated=StatisticalSummary[count=2, sum=3.0, mean=1.5, min=1.0, max=2.0, populationVariance=0.25, populationStandardDeviation=0.5, sampleVariance=0.50, sampleStandardDeviation=0.7071067811865475]]
//   WithOriginal[original=NamedValue[name=third, value=10.0], calculated=StatisticalSummary[count=3, sum=13.0, mean=4.333333333333333, min=1.0, max=10.0, populationVariance=16.22222222222222, populationStandardDeviation=4.02768199119819, sampleVariance=24.33333333333333, sampleStandardDeviation=4.932882862316247]]
//   WithOriginal[original=NamedValue[name=fourth, value=2.0], calculated=StatisticalSummary[count=4, sum=15.0, mean=3.75, min=1.0, max=10.0, populationVariance=13.1875, populationStandardDeviation=3.631459761583488, sampleVariance=17.58333333333333, sampleStandardDeviation=4.193248541803041]]
// ]
```
