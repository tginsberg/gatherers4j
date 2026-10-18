---
title: "runningPercentileBy()"
linkTitle: "runningPercentileBy()"
show_in_table: true
category: "Mathematical Operations"
description: Calculate the running percentile of `BigDecimal` objects mapped from a `Stream<INPUT>` via a `mappingFunction`.
infinite_streams: grows-with-length

---

### Implementation Notes
This implementation is suitable for mapping an arbitrary `Stream<INPUT>` to `BigDecimal` via a `mappingFunction`; for a version that operates directly on a `Stream<BigDecimal>`, see [`runningPercentile()`](/gatherers4j/gatherers/mathematical/runningpercentile/).
By default, nulls are ignored and play no part in calculations, see `treatNullAs()` and `treatNullAsZero()` below for ways to change this behavior. The default `MathContext`
for all calculations is {{< jdklink linkName="MathContext.DECIMAL64" package="java.base/java/math/MathContext.html#DECIMAL64" >}}, but this can be overridden (see `withMathContext()`, below).

Each emitted value is the percentile of every element seen so far, so the last value emitted is the percentile of the whole stream. Values that fall between two ranks are linearly interpolated (the R-7 method, NumPy's default). The `percentile` is converted to `BigDecimal` from its decimal representation, so `99.9` is exactly `99.9`. The rank and interpolation are calculated exactly, and only the result is rounded to the `MathContext`.

Every value seen is retained in sorted order, so memory use grows with the length of the stream. For long or unbounded streams, consider [`movingPercentileBy()`](/gatherers4j/gatherers/mathematical/movingpercentileby/) instead.

**Signatures**

`runningPercentileBy(double percentile, Function<INPUT, BigDecimal> mappingFunction)`
* `percentile` - The percentile to calculate, between 0 and 100, inclusive
* `mappingFunction` - A non-null function to map stream `INPUT` elements into `BigDecimal` for calculation

**Additional Methods**

| Method                                     | Purpose                                                                                                                                                                                                                                                                                                              |
|--------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `treatNullAsZero()`                        | When encountering a `null` value in a stream, treat it as `BigDecimal.ZERO` instead.                                                                                                                                                                                                                                 |
| `treatNullAs(BigDecimal replacement)`      | When encountering a `null` value in a stream, treat it as the given `replacement` value instead.                                                                                                                                                                                                                     |
| `treatNullAsOne()`                         | When encountering a `null` value in a stream, treat it as `BigDecimal.ONE` instead.                                                                                                                                                                                                                                  |
| `withMathContext(MathContext mathContext)` | Replace the `MathContext` used for all mathematical operations performed by this gatherer.                                                                                                                                                                                                                           |
| `withOriginal()`                           | Include the original input value from the stream in addition to the calculated value in a [`WithOriginal`](https://github.com/tginsberg/gatherers4j/blob/main/src/main/java/com/ginsberg/gatherers4j/dto/WithOriginal.java) record. [See example.](#emitting-a-record-containing-the-original-and-calculated-values) |

See [`runningPercentile()`](/gatherers4j/gatherers/mathematical/runningpercentile/) for examples of each of these.

### Examples

#### Running 90th percentile, mapped from an object

```java
record NamedValue(String name, BigDecimal value) {}

Stream
    .of(
        new NamedValue("first",  new BigDecimal("1.0")),
        new NamedValue("second", new BigDecimal("2.0")),
        new NamedValue("third",  new BigDecimal("10.0"))
    )
    .gather(Gatherers4j.runningPercentileBy(90, NamedValue::value))
    .toList();

// [
//   BigDecimal("1.0"),
//   BigDecimal("1.9"),
//   BigDecimal("8.4")
// ]
```

#### Emitting a record containing the original and calculated values

```java
record NamedValue(String name, BigDecimal value) {}

Stream
    .of(
        new NamedValue("first",  new BigDecimal("1.0")),
        new NamedValue("second", new BigDecimal("2.0")),
        new NamedValue("third",  new BigDecimal("10.0"))
    )
    .gather(Gatherers4j.runningPercentileBy(90, NamedValue::value).withOriginal())
    .toList();

// [
//   WithOriginal[original=NamedValue[name=first, value=1.0], calculated=1.0],
//   WithOriginal[original=NamedValue[name=second, value=2.0], calculated=1.9],
//   WithOriginal[original=NamedValue[name=third, value=10.0], calculated=8.4]
// ]
```
