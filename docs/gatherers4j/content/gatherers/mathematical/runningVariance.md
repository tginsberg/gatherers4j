---
title: "runningVariance()"
linkTitle: "runningVariance()"
show_in_table: true
category: "Mathematical Operations"
description: Calculate the running variance of a `Stream<BigDecimal>`, as either a population or a sample.

---

### Implementation Notes
This implementation is suitable for `Stream<BigDecimal>`, for a version that takes a user-specified mapping function see [`runningVarianceBy()`](/gatherers4j/gatherers/mathematical/runningvarianceby/).
By default, nulls are ignored and play no part in calculations, see `treatNullAs()` and `treatNullAsZero()` below for ways to change this behavior. The default `MathContext`
for all calculations is {{< jdklink linkName="MathContext.DECIMAL64" package="java.base/java/math/MathContext.html#DECIMAL64" >}}, but this can be overridden (see `withMathContext()`, below).

Use `Dataset.Population` when the elements are the entire dataset (the variance is divided by *n*), and `Dataset.Sample` when they are a sample of a larger population (the variance is divided by *n - 1*). The sample variance of a single value is undefined, so when using `Dataset.Sample` nothing is emitted until at least two values have been seen.

See also [`runningStandardDeviation()`](/gatherers4j/gatherers/mathematical/runningstandarddeviation/), which is the square root of this value.

**Signatures**

`runningVariance(Dataset dataset)`
* `dataset` - Either `Population` or `Sample`

**Additional Methods**

| Method                                     | Purpose                                                                                                                                                                                                                                                                                                              |
|--------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `treatNullAsZero()`                        | When encountering a `null` value in a stream, treat it as `BigDecimal.ZERO` instead. [See example.](#treating-null-as-zero)                                                                                                                                                                                          |
| `treatNullAs(BigDecimal replacement)`      | When encountering a `null` value in a stream, treat it as the given `replacement` value instead. [See example.](#replacing-null-with-another-bigdecimal)                                                                                                                                                             |
| `treatNullAsOne()`                         | When encountering a `null` value in a stream, treat it as `BigDecimal.ONE` instead.                                                                                                                                                                                                                                  |
| `withMathContext(MathContext mathContext)` | Replace the `MathContext` used for all mathematical operations performed by this gatherer. [See example.](#specifying-a-new-mathcontext)                                                                                                                                                                             |
| `withOriginal()`                           | Include the original input value from the stream in addition to the calculated value in a [`WithOriginal`](https://github.com/tginsberg/gatherers4j/blob/main/src/main/java/com/ginsberg/gatherers4j/dto/WithOriginal.java) record. [See example.](#emitting-a-record-containing-the-original-and-calculated-values) |

### Examples

#### Running Variance (Population)

```java
Stream
    .of("1.0", "2.0", "10.0")
    .map(BigDecimal::new)
    .gather(Gatherers4j.runningVariance(Dataset.Population))
    .toList();

// [
//   BigDecimal("0.00"),
//   BigDecimal("0.25"),
//   BigDecimal("16.22222222222222")
// ]
```

#### Running Variance (Sample)

```java
Stream
    .of("1.0", "2.0", "10.0")
    .map(BigDecimal::new)
    .gather(Gatherers4j.runningVariance(Dataset.Sample))
    .toList();

// [
//   BigDecimal("0.50"),
//   BigDecimal("24.33333333333333")
// ]
```

#### Showing nulls are ignored by default

```java
Stream
    .of(null, null, new BigDecimal("10.0"), new BigDecimal("2.0"), new BigDecimal("1.0"))
    .gather(Gatherers4j.runningVariance(Dataset.Population))
    .toList();

// [
//   BigDecimal("0.00"),
//   BigDecimal("25.00"),
//   BigDecimal("66.66666666666667")
// ]
```

#### Treating null as zero

```java
Stream
    .of(null, null, new BigDecimal("10.0"), new BigDecimal("2.0"))
    .gather(Gatherers4j.runningVariance(Dataset.Population).treatNullAsZero())
    .toList();

// [
//   BigDecimal("0"),
//   BigDecimal("0"),
//   BigDecimal("22.22222222222222"),
//   BigDecimal("17.00")
// ]
```

#### Replacing null with another `BigDecimal`

```java
Stream
    .of(null, null, new BigDecimal("10.0"), new BigDecimal("2.0"))
    .gather(Gatherers4j.runningVariance(Dataset.Population).treatNullAs(new BigDecimal("3.5")))
    .toList();

// [
//   BigDecimal("0.00"),
//   BigDecimal("0.00"),
//   BigDecimal("9.388888888888889"),
//   BigDecimal("9.5625")
// ]
```

#### Specifying a new `MathContext`

```java
Stream
    .of("1.0", "2.0", "10.0", "2.0")
    .map(BigDecimal::new)
    .gather(Gatherers4j
        .runningVariance(Dataset.Population)
        .withMathContext(new MathContext(3, RoundingMode.DOWN))
    )
    .toList();

// [
//   BigDecimal("0.00"),
//   BigDecimal("0.25"),
//   BigDecimal("16.2"),
//   BigDecimal("13.1")
// ]
```

#### Emitting a record containing the original and calculated values

```java
Stream
    .of("1.0", "2.0", "10.0", "2.0")
    .map(BigDecimal::new)
    .gather(Gatherers4j.runningVariance(Dataset.Population).withOriginal())
    .toList();

// [
//   WithOriginal[original=1.0, calculated=0.00]
//   WithOriginal[original=2.0, calculated=0.25]
//   WithOriginal[original=10.0, calculated=16.22222222222222]
//   WithOriginal[original=2.0, calculated=13.1875]
// ]
```
