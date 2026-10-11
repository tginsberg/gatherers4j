---
title: "runningPercentile()"
linkTitle: "runningPercentile()"
show_in_table: true
category: "Mathematical Operations"
description: Calculate the running percentile of a `Stream<BigDecimal>`.

---

### Implementation Notes
This implementation is suitable for `Stream<BigDecimal>`, for a version that takes a user-specified mapping function see [`runningPercentileBy()`](/gatherers4j/gatherers/mathematical/runningpercentileby/).
By default, nulls are ignored and play no part in calculations, see `treatNullAs()` and `treatNullAsZero()` below for ways to change this behavior. The default `MathContext`
for all calculations is {{< jdklink linkName="MathContext.DECIMAL64" package="java.base/java/math/MathContext.html#DECIMAL64" >}}, but this can be overridden (see `withMathContext()`, below).

Each emitted value is the percentile of every element seen so far, so the last value emitted is the percentile of the whole stream. Values that fall between two ranks are linearly interpolated (the R-7 method, as used by Excel's `PERCENTILE.INC` and NumPy's default). The `percentile` is converted to `BigDecimal` from its decimal representation, so `99.9` is exactly `99.9`. The rank and interpolation are calculated exactly, and only the result is rounded to the `MathContext`.

Every value seen is retained in sorted order, so memory use grows with the length of the stream. For long or unbounded streams, consider [`movingPercentile()`](/gatherers4j/gatherers/mathematical/movingpercentile/) instead.

**Signatures**

`runningPercentile(double percentile)`
* `percentile` - The percentile to calculate, between 0 and 100, inclusive

**Additional Methods**

| Method                                     | Purpose                                                                                                                                                                                                                                                                                                              |
|--------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `treatNullAsZero()`                        | When encountering a `null` value in a stream, treat it as `BigDecimal.ZERO` instead. [See example.](#treating-null-as-zero)                                                                                                                                                                                          |
| `treatNullAs(BigDecimal replacement)`      | When encountering a `null` value in a stream, treat it as the given `replacement` value instead. [See example.](#replacing-null-with-another-bigdecimal)                                                                                                                                                             |
| `treatNullAsOne()`                         | When encountering a `null` value in a stream, treat it as `BigDecimal.ONE` instead.                                                                                                                                                                                                                                  |
| `withMathContext(MathContext mathContext)` | Replace the `MathContext` used for all mathematical operations performed by this gatherer. [See example.](#specifying-a-new-mathcontext)                                                                                                                                                                             |
| `withOriginal()`                           | Include the original input value from the stream in addition to the calculated value in a [`WithOriginal`](https://github.com/tginsberg/gatherers4j/blob/main/src/main/java/com/ginsberg/gatherers4j/dto/WithOriginal.java) record. [See example.](#emitting-a-record-containing-the-original-and-calculated-values) |

### Examples

#### Running 90th percentile

```java
Stream
    .of("1.0", "2.0", "10.0", "2.0", "5.0")
    .map(BigDecimal::new)
    .gather(Gatherers4j.runningPercentile(90))
    .toList();

// [
//   BigDecimal("1.0")
//   BigDecimal("1.9")
//   BigDecimal("8.4")
//   BigDecimal("7.6")
//   BigDecimal("8.0")
// ]
```

#### Showing nulls are ignored by default

```java
Stream
    .of(null, null, new BigDecimal("10.0"), new BigDecimal("2.0"), new BigDecimal("1.0"))
    .gather(Gatherers4j.runningPercentile(90))
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
    .gather(Gatherers4j.runningPercentile(90).treatNullAsZero())
    .toList();

// [
//   BigDecimal("0")
//   BigDecimal("0")
//   BigDecimal("8.0")
//   BigDecimal("7.6")
// ]
```

#### Replacing null with another `BigDecimal`

```java
Stream
    .of(null, null, new BigDecimal("10.0"), new BigDecimal("2.0"))
    .gather(Gatherers4j.runningPercentile(90).treatNullAs(new BigDecimal("3.5")))
    .toList();

// [
//   BigDecimal("3.5")
//   BigDecimal("3.5")
//   BigDecimal("8.7")
//   BigDecimal("8.05")
// ]
```

#### Specifying a new `MathContext`

```java
Stream
    .of("1.111", "2.222", "10.333", "2.444")
    .map(BigDecimal::new)
    .gather(Gatherers4j
        .runningPercentile(90)
        .withMathContext(new MathContext(3, RoundingMode.DOWN))
    )
    .toList();

// [
//   BigDecimal("1.11")
//   BigDecimal("2.10")
//   BigDecimal("8.68")
//   BigDecimal("7.94")
// ]
```

#### Emitting a record containing the original and calculated values

```java
Stream
    .of("1.0", "2.0", "10.0", "2.0")
    .map(BigDecimal::new)
    .gather(Gatherers4j.runningPercentile(90).withOriginal())
    .toList();

// [
//   WithOriginal[original=1.0, calculated=1.0]
//   WithOriginal[original=2.0, calculated=1.9]
//   WithOriginal[original=10.0, calculated=8.4]
//   WithOriginal[original=2.0, calculated=7.6]
// ]
```
