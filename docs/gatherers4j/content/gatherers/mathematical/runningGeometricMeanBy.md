---
title: "runningGeometricMeanBy()"
linkTitle: "runningGeometricMeanBy()"
show_in_table: true
category: "Mathematical Operations"
description: Calculate the running geometric mean of `BigDecimal` objects mapped from a `Stream<INPUT>` via a `mappingFunction`.

---

### Implementation Notes
This implementation is suitable for mapping an arbitrary `Stream<INPUT>` to `BigDecimal` via a `mappingFunction`; for a version that operates directly on a `Stream<BigDecimal>`, see [`runningGeometricMean()`](/gatherers4j/gatherers/mathematical/runninggeometricmean/).
By default, nulls are ignored and play no part in calculations, see `treatNullAs()` and `treatNullAsOne()` below for ways to change this behavior. The default `MathContext`
for all calculations is {{< jdklink linkName="MathContext.DECIMAL64" package="java.base/java/math/MathContext.html#DECIMAL64" >}}, but this can be overridden (see `withMathContext()`, below).


**Signatures**

`runningGeometricMeanBy(Function<INPUT, BigDecimal> mappingFunction)`
* `mappingFunction` - A non-null function to map stream `INPUT` elements into `BigDecimal` for calculation

**Additional Methods**

| Method                                     | Purpose                                                                                                                                                                                                                                                                                                              |
|--------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `treatNullAsOne()`                         | When encountering a `null` value in a stream, treat it as `BigDecimal.ONE` instead. [See example.](#treating-null-as-one)                                                                                                                                                                                            |
| `treatNullAs(BigDecimal replacement)`      | When encountering a `null` value in a stream, treat it as the given `replacement` value instead. [See example.](#replacing-null-with-another-bigdecimal)                                                                                                                                                             |
| `withMathContext(MathContext mathContext)` | Replace the `MathContext` used for all mathematical operations performed by this gatherer. [See example.](#specifying-a-new-mathcontext)                                                                                                                                                                             |
| `withOriginal()`                           | Include the original input value from the stream in addition to the calculated value in a [`WithOriginal`](https://github.com/tginsberg/gatherers4j/blob/main/src/main/java/com/ginsberg/gatherers4j/dto/WithOriginal.java) record. [See example.](#emitting-a-record-containing-the-original-and-calculated-values) |

Note: `treatNullAsZero()` is also a valid method on this gatherer, but it only makes sense in a very narrow set of circumstances.

### Examples

#### Running geometric mean, mapped from an object

```java
record NamedValue(String name, BigDecimal value) {}

Stream
    .of(
        new NamedValue("first",  BigDecimal.valueOf(1)),
        new NamedValue("second", BigDecimal.valueOf(4)),
        new NamedValue("third",  BigDecimal.valueOf(16))
    )
    .gather(Gatherers4j.runningGeometricMeanBy(NamedValue::value))
    .toList();

// [ 
//   BigDecimal("1"), 
//   BigDecimal("2"),
//   BigDecimal("4") 
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
    .gather(Gatherers4j.runningGeometricMeanBy(NamedValue::value))
    .toList();

// [
//   BigDecimal("1"), 
//   BigDecimal("2"), 
//   BigDecimal("4") 
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
    .gather(Gatherers4j.runningGeometricMeanBy(NamedValue::value).treatNullAsOne())
    .toList();

// [
//   BigDecimal("1"),
//   BigDecimal("1"),
//   BigDecimal("1.587401051968199")
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
    .gather(Gatherers4j.runningGeometricMeanBy(NamedValue::value).treatNullAs(BigDecimal.valueOf(16)))
    .toList();

// [
//   BigDecimal("16"),
//   BigDecimal("4.0"),
//   BigDecimal("4.0") 
// ]
```


#### Specifying a new `MathContext`


```java
record NamedValue(String name, BigDecimal value) {}

Stream
    .of(
        new NamedValue("first",  BigDecimal.valueOf(1)),
        new NamedValue("second", BigDecimal.valueOf(4))
    )
    .gather(Gatherers4j
        .runningGeometricMeanBy(NamedValue::value)
        .withMathContext(new MathContext(2))
    )
    .toList();

// [ 
//   BigDecimal("1.0"), 
//   BigDecimal("2.0")
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
    .gather(Gatherers4j.runningGeometricMeanBy(NamedValue::value).withOriginal())
    .toList();

// [ 
//   WithOriginal[original=NamedValue[name=first, value=1], calculated=1],
//   WithOriginal[original=NamedValue[name=second, value=4], calculated=2]
// ]
```
