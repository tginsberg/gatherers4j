---
title: "movingStandardDeviation()"
linkTitle: "movingStandardDeviation()"
show_in_table: true
category: "Mathematical Operations"
description: Calculate the moving standard deviation of a `Stream<BigDecimal>` looking back `windowSize` elements, as either a population or a sample.

---

### Implementation Notes
This implementation is suitable for `Stream<BigDecimal>`, for a version that takes a user-specified mapping function see [`movingStandardDeviationBy()`](/gatherers4j/gatherers/mathematical/movingstandarddeviationby/).
By default, nulls are ignored and play no part in calculations, see `treatNullAs()` and `treatNullAsZero()` below for ways to change this behavior. The default `MathContext`
for all calculations is {{< jdklink linkName="MathContext.DECIMAL64" package="java.base/java/math/MathContext.html#DECIMAL64" >}}, but this can be overridden (see `withMathContext()`, below).

Use `Dataset.Population` when the elements are the entire dataset (the variance is divided by *n*), and `Dataset.Sample` when they are a sample of a larger population (the variance is divided by *n - 1*). The sample standard deviation of a single value is `0`.


**Signatures**

`movingStandardDeviation(Dataset dataset, int windowSize)`
* `dataset` - Either `Population` or `Sample`
* `windowSize` - How many trailing elements to calculate the standard deviation over, must be greater than 1

**Additional Methods**

| Method                                     | Purpose                                                                                                                                                                                                                                                                                                              |
|--------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `excludePartialValues()`                   | When calculating the moving standard deviation, and the full size of the window has not yet been reached, the gatherer should suppress emitting values until the lookback window is full. [See example.](#excluding-partial-values)                                                                                                 |
| `treatNullAsZero()`                        | When encountering a `null` value in a stream, treat it as `BigDecimal.ZERO` instead. [See example.](#treating-null-as-zero)                                                                                                                                                                                          |
| `treatNullAs(BigDecimal replacement)`      | When encountering a `null` value in a stream, treat it as the given `replacement` value instead. [See example.](#replacing-null-with-another-bigdecimal)                                                                                                                                                             |
| `treatNullAsOne()`                         | When encountering a `null` value in a stream, treat it as `BigDecimal.ONE` instead.                                                                                                                                                                                                                                  |
| `withMathContext(MathContext mathContext)` | Replace the `MathContext` used for all mathematical operations performed by this gatherer. [See example.](#specifying-a-new-mathcontext)                                                                                                                                                                             |
| `withOriginal()`                           | Include the original input value from the stream in addition to the calculated value in a [`WithOriginal`](https://github.com/tginsberg/gatherers4j/blob/main/src/main/java/com/ginsberg/gatherers4j/dto/WithOriginal.java) record. [See example.](#emitting-a-record-containing-the-original-and-calculated-values) |

### Examples

#### Moving standard deviation (population) of window size 3

```java
Stream
    .of("1.0", "2.0", "10.0", "2.0")
    .map(BigDecimal::new)
    .gather(Gatherers4j.movingStandardDeviation(Dataset.Population, 3))
    .toList();

// [
//   BigDecimal("0.0")
//   BigDecimal("0.5")
//   BigDecimal("4.02768199119819")
//   BigDecimal("3.771236166328253")
// ]
```

#### Moving standard deviation (sample) of window size 3

```java
Stream
    .of("1.0", "2.0", "10.0", "2.0")
    .map(BigDecimal::new)
    .gather(Gatherers4j.movingStandardDeviation(Dataset.Sample, 3))
    .toList();

// [
//   BigDecimal("0")
//   BigDecimal("0.7071067811865475")
//   BigDecimal("4.932882862316247")
//   BigDecimal("4.618802153517006")
// ]
```

#### Excluding partial values

Showing that in-process moving standard deviation values are not emitted for each element until the lookback window has been filled.

```java
Stream
    .of("1.0", "2.0", "10.0", "2.0")
    .map(BigDecimal::new)
    .gather(Gatherers4j.movingStandardDeviation(Dataset.Population, 3).excludePartialValues())
    .toList();

// [
//   BigDecimal("4.02768199119819")
//   BigDecimal("3.771236166328253")
// ]
```

#### Showing nulls are ignored by default

```java
Stream
    .of(null, null, new BigDecimal("10.0"), new BigDecimal("2.0"), new BigDecimal("1.0"))
    .gather(Gatherers4j.movingStandardDeviation(Dataset.Population, 3))
    .toList();

// [
//   BigDecimal("0.0")
//   BigDecimal("4.0")
//   BigDecimal("4.02768199119819")
// ]
```

#### Treating null as zero

```java
Stream
    .of(null, null, new BigDecimal("10.0"), new BigDecimal("2.0"))
    .gather(Gatherers4j.movingStandardDeviation(Dataset.Population, 3).treatNullAsZero())
    .toList();

// [
//   BigDecimal("0")
//   BigDecimal("0")
//   BigDecimal("4.714045207910317")
//   BigDecimal("4.320493798938574")
// ]
```

#### Replacing null with another `BigDecimal`

```java
Stream
    .of(null, null, new BigDecimal("10.0"), new BigDecimal("2.0"))
    .gather(Gatherers4j.movingStandardDeviation(Dataset.Population, 3).treatNullAs(new BigDecimal("3.5")))
    .toList();

// [
//   BigDecimal("0.0")
//   BigDecimal("0.0")
//   BigDecimal("3.064129385141706")
//   BigDecimal("3.472111109333277")
// ]
```

#### Specifying a new `MathContext`

```java
Stream
    .of("1.0", "2.0", "10.0", "2.0")
    .map(BigDecimal::new)
    .gather(Gatherers4j
        .movingStandardDeviation(Dataset.Population, 3)
        .withMathContext(new MathContext(3, RoundingMode.DOWN))
    )
    .toList();

// [
//   BigDecimal("0.0")
//   BigDecimal("0.5")
//   BigDecimal("4.02")
//   BigDecimal("3.76")
// ]
```

#### Emitting a record containing the original and calculated values

```java
Stream
    .of("1.0", "2.0", "10.0", "2.0")
    .map(BigDecimal::new)
    .gather(Gatherers4j.movingStandardDeviation(Dataset.Population, 3).withOriginal())
    .toList();

// [
//   WithOriginal[original=1.0, calculated=0.0]
//   WithOriginal[original=2.0, calculated=0.5]
//   WithOriginal[original=10.0, calculated=4.02768199119819]
//   WithOriginal[original=2.0, calculated=3.771236166328253]
// ]
```
