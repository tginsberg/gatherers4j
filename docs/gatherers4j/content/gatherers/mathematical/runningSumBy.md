---
title: "runningSumBy()"
linkTitle: "runningSumBy()"
show_in_table: true
category: "Mathematical Operations"
description: Calculate the running sum of `BigDecimal` objects mapped from a `Stream<INPUT>` via a `mappingFunction`.
---

### Implementation Notes
This implementation is suitable mapping an arbitrary `Stream<INPUT>` to `BigDecimal` via a `mappingFunction`; for a version that operates directly on a `Stream<BigDecimal>`, see [`runningSum()`](/gatherers/mathematical/runningsum/).
By default, nulls are ignored and play no part in calculations, see `treatNullAs()` and `treatNullAsZero()` below for ways to change this behavior. The default `MathContext`
for all calculations is {{< jdklink linkName="MathContext.DECIMAL64" package="java.base/java/math/MathContext.html#DECIMAL64" >}}, but this can be overridden (see `withMathContext()`, below).


**Signatures**

`runnningSumBy(Function<INPUT, BigDecimal> mappingFunction)`
* `mappingFunction` - A non-null function to map stream `INPUT` elements into `BigDecimal` for calculation

**Additional Methods**

| Method                                     | Purpose                                                                                                                                                                                                                                                                                                           |
|--------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `treatNullAsZero()`                        | When encountering a `null` value in a stream, treat it as `BigDecimal.ONE` instead. [See example.](#treating-null-as-one)                                                                                                                                                                                         |
| `treatNullAs(BigDecimal replacement)`      | When encountering a `null` value in a stream, treat it as the given `replacement` value instead. [See example.](#replacing-null-with-another-bigdecimal)                                                                                                                                                          |
| `withMathContext(MathContext mathContext)` | Replace the `MathContext` used for all mathematical operations performed by this gatherer. [See example.](#specifying-a-new-mathcontext)                                                                                                                                                                          |
| `withOriginal()`                           | Include the original input value from the stream in addition to the calculated value in a [`WithOriginal`](https://github.com/tginsberg/gatherers4j/blob/main/src/main/java/com/ginsberg/gatherers4j/WithOriginal.java)record. [See example.](#emitting-a-record-containing-the-original-and-calculated-values) |

### Examples

#### Running sum, mapped from an object

```java
record NamedValue(String name, BigDecimal value) {}

Stream
    .of(
        new NamedValue("first",  new BigDecimal("1.1")),
        new NamedValue("second", new BigDecimal("2.2")),
        new NamedValue("third",  new BigDecimal("10.3"))
    )
    .gather(Gatherers4j.runningSumBy(NamedValue::value))
    .toList();

// [ 
//   BigDecimal("1.1"), 
//   BigDecimal("3.3"),
//   BigDecimal("13.6") 
// ]
```


#### Showing nulls are ignored by default

```java
record NamedValue(String name, BigDecimal value) {}

Stream
    .of(
        new NamedValue("first",  null),
        new NamedValue("second", null),
        new NamedValue("first",  new BigDecimal("1.1")),
        new NamedValue("second", new BigDecimal("2.2")),
        new NamedValue("third",  new BigDecimal("10.3"))
    )
    .gather(Gatherers4j.runningSumBy(NamedValue::value))
    .toList();

// [
//   BigDecimal("1.1"), 
//   BigDecimal("3.3"),
//   BigDecimal("13.6") 
// ]
```

#### Treating null as zero

```java
record NamedValue(String name, BigDecimal value) {}

Stream
    .of(
        new NamedValue("first",  null),
        new NamedValue("second", null),
        new NamedValue("third",  new BigDecimal("1.1")),
        new NamedValue("fourth", new BigDecimal("2.2")),
        new NamedValue("fifth",  new BigDecimal("10.3"))
    )
    .gather(Gatherers4j.runningSumBy(NamedValue::value).treatNullAsZero())
    .toList();

// [
//   BigDecimal("0"), 
//   BigDecimal("0"), 
//   BigDecimal("1.1"), 
//   BigDecimal("3.3"),
//   BigDecimal("13.6") 
// ]
```

#### Replacing null with another `BigDecimal`


```java
record NamedValue(String name, BigDecimal value) {}

Stream
    .of(
        new NamedValue("first",  null),
        new NamedValue("second", null),
        new NamedValue("third",  new BigDecimal("10.1")),
        new NamedValue("fourth", new BigDecimal("20.2")),
        new NamedValue("fifth",  new BigDecimal("30.3"))
    )
    .gather(Gatherers4j.runningSumBy(NamedValue::value).treatNullAs(BigDecimal.TWO))
    .toList();

// [
//   BigDecimal("2"),
//   BigDecimal("4"),
//   BigDecimal("14.1"), 
//   BigDecimal("34.3"), 
//   BigDecimal("64.6") 
// ]
```


#### Specifying a new `MathContext`


```java
record NamedValue(String name, BigDecimal value) {}

Stream
    .of(
        new NamedValue("first",  new BigDecimal("1.111")),
        new NamedValue("second", new BigDecimal("2.222")),
        new NamedValue("third",  new BigDecimal("10.333"))
    )
    .gather(Gatherers4j
        .runningSumBy(NamedValue::value)
        .withMathContext(new MathContext(3, RoundingMode.DOWN))
    )
    .toList();

// [ 
//   BigDecimal("1.11"), 
//   BigDecimal("3.33"),
//   BigDecimal("13.6")
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
    .gather(Gatherers4j.runningSumBy(NamedValue::value).withOriginal())
    .toList();

// [ 
//   WithOriginal[original=NamedValue[name=first, value=1.0], calculated=1.0]
//   WithOriginal[original=NamedValue[name=second, value=2.0], calculated=3.0]
//   WithOriginal[original=NamedValue[name=third, value=10.0], calculated=13.0]
// ]
```