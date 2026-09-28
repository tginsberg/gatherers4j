---
title: "runningVarianceBy()"
linkTitle: "runningVarianceBy()"
show_in_table: true
category: "Mathematical Operations"
description: Calculate the running variance of `BigDecimal` objects mapped from a `Stream<INPUT>` via a `mappingFunction`, as either a population or a sample.

---

### Implementation Notes
This implementation is suitable for mapping an arbitrary `Stream<INPUT>` to `BigDecimal` via a `mappingFunction`; for a version that operates directly on a `Stream<BigDecimal>`, see [`runningVariance()`](/gatherers4j/gatherers/mathematical/runningvariance/).
By default, nulls are ignored and play no part in calculations, see `treatNullAs()` and `treatNullAsZero()` below for ways to change this behavior. The default `MathContext`
for all calculations is {{< jdklink linkName="MathContext.DECIMAL64" package="java.base/java/math/MathContext.html#DECIMAL64" >}}, but this can be overridden (see `withMathContext()`, below).

Use `Dataset.Population` when the elements are the entire dataset (the variance is divided by *n*), and `Dataset.Sample` when they are a sample of a larger population (the variance is divided by *n - 1*). The sample variance of a single value is `0`.

See also [`runningStandardDeviationBy()`](/gatherers4j/gatherers/mathematical/runningstandarddeviationby/), which is the square root of this value.

**Signatures**

`runningVarianceBy(Dataset dataset, Function<INPUT, BigDecimal> mappingFunction)`
* `dataset` - Either `Population` or `Sample`
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

#### Running Variance (Population), mapped from an object

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
    .gather(Gatherers4j.runningVarianceBy(Dataset.Population, NamedValue::value))
    .toList();

// [
//   BigDecimal("0.00"),
//   BigDecimal("0.25"),
//   BigDecimal("16.22222222222222"),
//   BigDecimal("58.1875"),
//   BigDecimal("122.24")
// ]
```

#### Running Variance (Sample), mapped from an object

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
    .gather(Gatherers4j.runningVarianceBy(Dataset.Sample, NamedValue::value))
    .toList();

// [
//   BigDecimal("0.00"),
//   BigDecimal("0.50"),
//   BigDecimal("24.33333333333333"),
//   BigDecimal("77.58333333333333"),
//   BigDecimal("152.80")
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
    .gather(Gatherers4j.runningVarianceBy(Dataset.Population, NamedValue::value))
    .toList();

// [
//   BigDecimal("0.00"),
//   BigDecimal("25.00"),
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
    .gather(Gatherers4j.runningVarianceBy(Dataset.Population, NamedValue::value).treatNullAsZero())
    .toList();

// [
//   BigDecimal("0"),
//   BigDecimal("0"),
//   BigDecimal("22.22222222222222"),
//   BigDecimal("68.75"),
//   BigDecimal("136.00")
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
    .gather(Gatherers4j.runningVarianceBy(Dataset.Population, NamedValue::value).treatNullAs(BigDecimal.TWO))
    .toList();

// [
//   BigDecimal("0"),
//   BigDecimal("0"),
//   BigDecimal("14.22222222222222"),
//   BigDecimal("54.75"),
//   BigDecimal("117.76")
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
        .runningVarianceBy(Dataset.Population, NamedValue::value)
        .withMathContext(new MathContext(3, RoundingMode.DOWN))
    )
    .toList();

// [
//   BigDecimal("0.00"),
//   BigDecimal("0.25"),
//   BigDecimal("16.2"),
//   BigDecimal("58.1"),
//   BigDecimal("122")
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
    .gather(Gatherers4j.runningVarianceBy(Dataset.Population, NamedValue::value).withOriginal())
    .toList();

// [
//   WithOriginal[original=NamedValue[name=first, value=1.0], calculated=0.00]
//   WithOriginal[original=NamedValue[name=second, value=2.0], calculated=0.25]
//   WithOriginal[original=NamedValue[name=third, value=10.0], calculated=16.22222222222222]
//   WithOriginal[original=NamedValue[name=fourth, value=20.0], calculated=58.1875]
//   WithOriginal[original=NamedValue[name=fifth, value=30.0], calculated=122.24]
// ]
```
