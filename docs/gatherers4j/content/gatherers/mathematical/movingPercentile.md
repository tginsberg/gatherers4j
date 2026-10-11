---
title: "movingPercentile()"
linkTitle: "movingPercentile()"
show_in_table: true
category: "Mathematical Operations"
description: Create a Stream that represents the moving percentile of a `Stream<BigDecimal>` looking back `windowSize` elements.

---

### Implementation Notes
This implementation is suitable for `Stream<BigDecimal>`, for a version that takes a user-specified mapping function see [`movingPercentileBy()`](/gatherers4j/gatherers/mathematical/movingpercentileby/).
By default, nulls are ignored and play no part in calculations, see `treatNullAs()` and `treatNullAsZero()` below for ways to change this behavior. The default `MathContext`
for all calculations is {{< jdklink linkName="MathContext.DECIMAL64" package="java.base/java/math/MathContext.html#DECIMAL64" >}}, but this can be overridden (see `withMathContext()`, below).

Values that fall between two ranks are linearly interpolated (the R-7 method, as used by Excel's `PERCENTILE.INC` and NumPy's default). The `percentile` is converted to `BigDecimal` from its decimal representation, so `99.9` is exactly `99.9`. The rank and interpolation are calculated exactly, and only the result is rounded to the `MathContext`.

**Signatures**

`movingPercentile(double percentile, int windowSize)`
* `percentile` - The percentile to calculate, between 0 and 100, inclusive
* `windowSize` - How many trailing elements to calculate the percentile over, must be greater than 1

**Additional Methods**

| Method                                     | Purpose                                                                                                                                                                                                                                                                                                              |
|--------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `excludePartialValues()`                   | When calculating the moving percentile, and the full size of the window has not yet been reached, the gatherer should suppress emitting values until the lookback window is full. [See example.](#excluding-partial-values)                                                                                          |
| `treatNullAsZero()`                        | When encountering a `null` value in a stream, treat it as `BigDecimal.ZERO` instead. [See example.](#treating-null-as-zero)                                                                                                                                                                                          |
| `treatNullAs(BigDecimal replacement)`      | When encountering a `null` value in a stream, treat it as the given `replacement` value instead. [See example.](#replacing-null-with-another-bigdecimal)                                                                                                                                                             |
| `treatNullAsOne()`                         | When encountering a `null` value in a stream, treat it as `BigDecimal.ONE` instead.                                                                                                                                                                                                                                  |
| `withMathContext(MathContext mathContext)` | Replace the `MathContext` used for all mathematical operations performed by this gatherer. [See example.](#specifying-a-new-mathcontext)                                                                                                                                                                             |
| `withOriginal()`                           | Include the original input value from the stream in addition to the calculated value in a [`WithOriginal`](https://github.com/tginsberg/gatherers4j/blob/main/src/main/java/com/ginsberg/gatherers4j/dto/WithOriginal.java) record. [See example.](#emitting-a-record-containing-the-original-and-calculated-values) |

### Examples

#### Moving 90th percentile of window size 3

```java
Stream
    .of("1.0", "2.0", "10.0", "2.0", "5.0")
    .map(BigDecimal::new)
    .gather(Gatherers4j.movingPercentile(90, 3))
    .toList();

// [
//   BigDecimal("1.0")
//   BigDecimal("1.9")
//   BigDecimal("8.4")
//   BigDecimal("8.4")
//   BigDecimal("9.0")
// ]
```

#### Excluding partial values

Showing that in-process moving percentile values are not emitted for each element until the lookback window has been filled.

```java
Stream
    .of("1.0", "2.0", "10.0", "2.0", "5.0")
    .map(BigDecimal::new)
    .gather(Gatherers4j.movingPercentile(90, 3).excludePartialValues())
    .toList();

// [
//   BigDecimal("8.4")
//   BigDecimal("8.4")
//   BigDecimal("9.0")
// ]
```

#### Showing nulls are ignored by default

```java
Stream
    .of(null, null, new BigDecimal("10.0"), new BigDecimal("2.0"), new BigDecimal("1.0"))
    .gather(Gatherers4j.movingPercentile(90, 3))
    .toList();

// [
//   BigDecimal("10.0")
//   BigDecimal("9.2")
//   BigDecimal("8.4")
// ]
```

#### Treating null as zero

```java
Stream
    .of(null, null, new BigDecimal("10.0"), new BigDecimal("2.0"))
    .gather(Gatherers4j.movingPercentile(90, 3).treatNullAsZero())
    .toList();

// [
//   BigDecimal("0")
//   BigDecimal("0")
//   BigDecimal("8.0")
//   BigDecimal("8.4")
// ]
```

#### Replacing null with another `BigDecimal`

```java
Stream
    .of(null, null, new BigDecimal("10.0"), new BigDecimal("2.0"))
    .gather(Gatherers4j.movingPercentile(90, 3).treatNullAs(new BigDecimal("3.5")))
    .toList();

// [
//   BigDecimal("3.5")
//   BigDecimal("3.5")
//   BigDecimal("8.7")
//   BigDecimal("8.7")
// ]
```

#### Specifying a new `MathContext`

```java
Stream
    .of("1.111", "2.222", "10.333", "2.444")
    .map(BigDecimal::new)
    .gather(Gatherers4j
        .movingPercentile(90, 3)
        .withMathContext(new MathContext(3, RoundingMode.DOWN))
    )
    .toList();

// [
//   BigDecimal("1.11")
//   BigDecimal("2.10")
//   BigDecimal("8.68")
//   BigDecimal("8.72")
// ]
```

#### Emitting a record containing the original and calculated values

```java
Stream
    .of("1.0", "2.0", "10.0", "2.0")
    .map(BigDecimal::new)
    .gather(Gatherers4j.movingPercentile(90, 3).withOriginal())
    .toList();

// [
//   WithOriginal[original=1.0, calculated=1.0]
//   WithOriginal[original=2.0, calculated=1.9]
//   WithOriginal[original=10.0, calculated=8.4]
//   WithOriginal[original=2.0, calculated=8.4]
// ]
```
