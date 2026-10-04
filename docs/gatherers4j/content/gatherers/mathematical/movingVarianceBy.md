---
title: "movingVarianceBy()"
linkTitle: "movingVarianceBy()"
show_in_table: true
category: "Mathematical Operations"
description: Calculate the moving variance of `BigDecimal` objects mapped from a `Stream<INPUT>` via a `mappingFunction` and looking back `windowSize` elements, as either a population or a sample.

---

### Implementation Notes
This implementation is suitable for mapping an arbitrary `Stream<INPUT>` to `BigDecimal` via a `mappingFunction`; for a version that operates directly on a `Stream<BigDecimal>`, see [`movingVariance()`](/gatherers4j/gatherers/mathematical/movingvariance/).
By default, nulls are ignored and play no part in calculations, see `treatNullAs()` and `treatNullAsZero()` below for ways to change this behavior. The default `MathContext`
for all calculations is {{< jdklink linkName="MathContext.DECIMAL64" package="java.base/java/math/MathContext.html#DECIMAL64" >}}, but this can be overridden (see `withMathContext()`, below).

Use `Dataset.Population` when the elements are the entire dataset (the variance is divided by *n*), and `Dataset.Sample` when they are a sample of a larger population (the variance is divided by *n - 1*). The sample variance of a single value is undefined, so when using `Dataset.Sample` nothing is emitted until at least two values have been seen.

See also [`movingStandardDeviationBy()`](/gatherers4j/gatherers/mathematical/movingstandarddeviationby/), which is the square root of this value.

**Signatures**

`movingVarianceBy(Dataset dataset, int windowSize, Function<INPUT, BigDecimal> mappingFunction)`
* `dataset` - Either `Population` or `Sample`
* `windowSize` - How many trailing elements to calculate the variance over at any given point in the stream, must be greater than 1
* `mappingFunction` - A non-null function to map stream `INPUT` elements into `BigDecimal` for calculation

**Additional Methods**

| Method                                     | Purpose                                                                                                                                                                                                                                                                                                              |
|--------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `excludePartialValues()`                   | When calculating the moving variance, and the full size of the window has not yet been reached, the gatherer should suppress emitting values until the lookback window is full. [See example.](#excluding-partial-values)                                                                                                 |
| `treatNullAsZero()`                        | When encountering a `null` value in a stream, treat it as `BigDecimal.ZERO` instead. [See example.](#treating-null-as-zero)                                                                                                                                                                                          |
| `treatNullAs(BigDecimal replacement)`      | When encountering a `null` value in a stream, treat it as the given `replacement` value instead. [See example.](#replacing-null-with-another-bigdecimal)                                                                                                                                                             |
| `treatNullAsOne()`                         | When encountering a `null` value in a stream, treat it as `BigDecimal.ONE` instead.                                                                                                                                                                                                                                  |
| `withMathContext(MathContext mathContext)` | Replace the `MathContext` used for all mathematical operations performed by this gatherer. [See example.](#specifying-a-new-mathcontext)                                                                                                                                                                             |
| `withOriginal()`                           | Include the original input value from the stream in addition to the calculated value in a [`WithOriginal`](https://github.com/tginsberg/gatherers4j/blob/main/src/main/java/com/ginsberg/gatherers4j/dto/WithOriginal.java) record. [See example.](#emitting-a-record-containing-the-original-and-calculated-values) |

### Examples

#### Moving variance (population) of window size 3, mapped from an object

```java
record NamedValue(String name, BigDecimal value) {}

Stream
    .of(
        new NamedValue("first",  new BigDecimal("1.0")),
        new NamedValue("second", new BigDecimal("2.0")),
        new NamedValue("third",  new BigDecimal("10.0")),
        new NamedValue("fourth", new BigDecimal("20.0")),
        new NamedValue("fifth",  new BigDecimal("30.0"))
    )
    .gather(Gatherers4j.movingVarianceBy(Dataset.Population, 3, NamedValue::value))
    .toList();

// [
//   BigDecimal("0.00")
//   BigDecimal("0.25")
//   BigDecimal("16.22222222222222")
//   BigDecimal("54.22222222222222")
//   BigDecimal("66.66666666666667")
// ]
```

#### Moving variance (sample) of window size 3, mapped from an object

```java
record NamedValue(String name, BigDecimal value) {}

Stream
    .of(
        new NamedValue("first",  new BigDecimal("1.0")),
        new NamedValue("second", new BigDecimal("2.0")),
        new NamedValue("third",  new BigDecimal("10.0")),
        new NamedValue("fourth", new BigDecimal("20.0")),
        new NamedValue("fifth",  new BigDecimal("30.0"))
    )
    .gather(Gatherers4j.movingVarianceBy(Dataset.Sample, 3, NamedValue::value))
    .toList();

// [
//   BigDecimal("0.50")
//   BigDecimal("24.33333333333333")
//   BigDecimal("81.33333333333333")
//   BigDecimal("100.00")
// ]
```

#### Excluding partial values

```java
record NamedValue(String name, BigDecimal value) {}

Stream
    .of(
        new NamedValue("first",  new BigDecimal("1.0")),
        new NamedValue("second", new BigDecimal("2.0")),
        new NamedValue("third",  new BigDecimal("10.0")),
        new NamedValue("fourth", new BigDecimal("20.0")),
        new NamedValue("fifth",  new BigDecimal("30.0"))
    )
    .gather(Gatherers4j.movingVarianceBy(Dataset.Population, 3, NamedValue::value).excludePartialValues())
    .toList();

// [
//   BigDecimal("16.22222222222222")
//   BigDecimal("54.22222222222222")
//   BigDecimal("66.66666666666667")
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
        new NamedValue("fourth", new BigDecimal("20.0")),
        new NamedValue("fifth",  new BigDecimal("30.0"))
    )
    .gather(Gatherers4j.movingVarianceBy(Dataset.Population, 3, NamedValue::value))
    .toList();

// [
//   BigDecimal("0.00")
//   BigDecimal("25.00")
//   BigDecimal("66.66666666666667")
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
        new NamedValue("fourth", new BigDecimal("20.0")),
        new NamedValue("fifth",  new BigDecimal("30.0"))
    )
    .gather(Gatherers4j.movingVarianceBy(Dataset.Population, 3, NamedValue::value).treatNullAsZero())
    .toList();

// [
//   BigDecimal("0")
//   BigDecimal("0")
//   BigDecimal("22.22222222222222")
//   BigDecimal("66.66666666666667")
//   BigDecimal("66.66666666666667")
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
        new NamedValue("fourth", new BigDecimal("20.0")),
        new NamedValue("fifth",  new BigDecimal("30.0"))
    )
    .gather(Gatherers4j.movingVarianceBy(Dataset.Population, 3, NamedValue::value).treatNullAs(BigDecimal.TWO))
    .toList();

// [
//   BigDecimal("0")
//   BigDecimal("0")
//   BigDecimal("14.22222222222222")
//   BigDecimal("54.22222222222222")
//   BigDecimal("66.66666666666667")
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
        new NamedValue("fourth", new BigDecimal("20.0")),
        new NamedValue("fifth",  new BigDecimal("30.0"))
    )
    .gather(Gatherers4j
        .movingVarianceBy(Dataset.Population, 3, NamedValue::value)
        .withMathContext(new MathContext(3, RoundingMode.DOWN))
    )
    .toList();

// [
//   BigDecimal("0.00")
//   BigDecimal("0.25")
//   BigDecimal("16.2")
//   BigDecimal("54.2")
//   BigDecimal("66.6")
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
        new NamedValue("fourth", new BigDecimal("20.0")),
        new NamedValue("fifth",  new BigDecimal("30.0"))
    )
    .gather(Gatherers4j.movingVarianceBy(Dataset.Population, 3, NamedValue::value).withOriginal())
    .toList();

// [
//   WithOriginal[original=NamedValue[name=first, value=1.0], calculated=0.00]
//   WithOriginal[original=NamedValue[name=second, value=2.0], calculated=0.25]
//   WithOriginal[original=NamedValue[name=third, value=10.0], calculated=16.22222222222222]
//   WithOriginal[original=NamedValue[name=fourth, value=20.0], calculated=54.22222222222222]
//   WithOriginal[original=NamedValue[name=fifth, value=30.0], calculated=66.66666666666667]
// ]
```
