---
title: "runningHarmonicMeanBy()"
linkTitle: "runningHarmonicMeanBy()"
show_in_table: true
category: "Mathematical Operations"
description: Calculate the running harmonic mean of `BigDecimal` objects mapped from a `Stream<INPUT>` via a `mappingFunction`.

---

### Implementation Notes
This implementation is suitable for mapping an arbitrary `Stream<INPUT>` to `BigDecimal` via a `mappingFunction`; for a version that operates directly on a `Stream<BigDecimal>`, see [`runningHarmonicMean()`](/gatherers4j/gatherers/mathematical/runningharmonicmean/).
By default, nulls are ignored and play no part in calculations, see `treatNullAs()` and `treatNullAsOne()` below for ways to change this behavior. The default `MathContext`
for all calculations is {{< jdklink linkName="MathContext.DECIMAL64" package="java.base/java/math/MathContext.html#DECIMAL64" >}}, but this can be overridden (see `withMathContext()`, below).

A `0` anywhere in the stream is undefined for a harmonic mean and will throw an `ArithmeticException`.

**Signatures**

`runningHarmonicMeanBy(Function<INPUT, BigDecimal> mappingFunction)`
* `mappingFunction` - A non-null function to map stream `INPUT` elements into `BigDecimal` for calculation

**Additional Methods**

| Method                                     | Purpose                                                                                                                                                                                                                                                                                                              |
|--------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `treatNullAsOne()`                         | When encountering a `null` value in a stream, treat it as `BigDecimal.ONE` instead. [See example.](#treating-null-as-one)                                                                                                                                                                                            |
| `treatNullAs(BigDecimal replacement)`      | When encountering a `null` value in a stream, treat it as the given `replacement` value instead. [See example.](#replacing-null-with-another-bigdecimal)                                                                                                                                                             |
| `withMathContext(MathContext mathContext)` | Replace the `MathContext` used for all mathematical operations performed by this gatherer. [See example.](#specifying-a-new-mathcontext)                                                                                                                                                                             |
| `withOriginal()`                           | Include the original input value from the stream in addition to the calculated value in a [`WithOriginal`](https://github.com/tginsberg/gatherers4j/blob/main/src/main/java/com/ginsberg/gatherers4j/dto/WithOriginal.java) record. [See example.](#emitting-a-record-containing-the-original-and-calculated-values) |

Note: `treatNullAsZero()` is also a valid method on this gatherer, but since `0` is undefined for a harmonic mean, it will cause an `ArithmeticException` as soon as a `null` value is encountered.

### Examples

#### Running harmonic mean, mapped from an object

```java
record NamedValue(String name, BigDecimal value) {}

Stream
    .of(
        new NamedValue("first",  BigDecimal.valueOf(1)),
        new NamedValue("second", BigDecimal.valueOf(4)),
        new NamedValue("third",  BigDecimal.valueOf(16))
    )
    .gather(Gatherers4j.runningHarmonicMeanBy(NamedValue::value))
    .toList();

// [ 
//   BigDecimal("1"), 
//   BigDecimal("1.6"),
//   BigDecimal("2.285714285714286") 
// ]
```

#### Showing nulls are ignored by default

```java
record NamedValue(String name, BigDecimal value) {}

Stream
    .of(
        new NamedValue("first",  null),
        new NamedValue("second", BigDecimal.valueOf(1)),
        new NamedValue("third",  BigDecimal.valueOf(4)),
        new NamedValue("fourth", BigDecimal.valueOf(16))
    )
    .gather(Gatherers4j.runningHarmonicMeanBy(NamedValue::value))
    .toList();

// [
//   BigDecimal("1"), 
//   BigDecimal("1.6"), 
//   BigDecimal("2.285714285714286") 
// ]
```

#### Treating null as one

```java
record NamedValue(String name, BigDecimal value) {}

Stream
    .of(
        new NamedValue("first",  null),
        new NamedValue("second", BigDecimal.valueOf(1)),
        new NamedValue("third",  BigDecimal.valueOf(4))
    )
    .gather(Gatherers4j.runningHarmonicMeanBy(NamedValue::value).treatNullAsOne())
    .toList();

// [
//   BigDecimal("1"),
//   BigDecimal("1"),
//   BigDecimal("1.333333333333333")
// ]
```

#### Replacing null with another `BigDecimal`


```java
record NamedValue(String name, BigDecimal value) {}

Stream
    .of(
        new NamedValue("first",  null),
        new NamedValue("second", BigDecimal.valueOf(1)),
        new NamedValue("third",  BigDecimal.valueOf(4))
    )
    .gather(Gatherers4j.runningHarmonicMeanBy(NamedValue::value).treatNullAs(BigDecimal.valueOf(16)))
    .toList();

// [
//   BigDecimal("16"),
//   BigDecimal("1.882352941176471"),
//   BigDecimal("2.285714285714286") 
// ]
```


#### Specifying a new `MathContext`


```java
record NamedValue(String name, BigDecimal value) {}

Stream
    .of(
        new NamedValue("first",  BigDecimal.valueOf(1)),
        new NamedValue("second", BigDecimal.valueOf(3))
    )
    .gather(Gatherers4j
        .runningHarmonicMeanBy(NamedValue::value)
        .withMathContext(new MathContext(2))
    )
    .toList();

// [ 
//   BigDecimal("1"), 
//   BigDecimal("1.5")
// ]
```


#### Emitting a record containing the original and calculated values


```java
record NamedValue(String name, BigDecimal value) {}

Stream
    .of(
        new NamedValue("first",  BigDecimal.valueOf(1)),
        new NamedValue("second", BigDecimal.valueOf(4))
    )
    .gather(Gatherers4j.runningHarmonicMeanBy(NamedValue::value).withOriginal())
    .toList();

// [ 
//   WithOriginal[original=NamedValue[name=first, value=1], calculated=1],
//   WithOriginal[original=NamedValue[name=second, value=4], calculated=1.6]
// ]
```
