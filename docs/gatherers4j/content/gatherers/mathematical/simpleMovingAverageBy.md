---
title: "simpleMovingAverageBy()"
linkTitle: "simpleMovingAverageBy()"
show_in_table: true
category: "Mathematical Operations"
description: Calculate the simple moving average of `BigDecimal` objects mapped from a `Stream<INPUT>` via a `mappingFunction` and looking back `windowSize` number of elements.

---

### Implementation Notes
This implementation is suitable mapping an arbitrary `Stream<INPUT>` to `BigDecimal` via a `mappingFunction`; for a version that operates directly on a `Stream<BigDecimal>`, see [`simpleMovingAverage()`](/gatherers4j/gatherers/mathematical/simplemovingaverage/).
By default, nulls are ignored and play no part in calculations, see `treatNullAs()` and `treatNullAsZero()` below for ways to change this behavior. The default `MathContext`
for all calculations is {{< jdklink linkName="MathContext.DECIMAL64" package="java.base/java/math/MathContext.html#DECIMAL64" >}}, but this can be overridden (see `withMathContext()`, below).


**Signatures**

`simpleMovingAverageBy(int windowSize, Function<INPUT, BigDecimal> mappingFunction)`
* `windowSize` - How many trailing elements to average over at any given point in the stream
* `mappingFunction` - A non-null function to map stream `INPUT` elements into `BigDecimal` for calculation

**Additional Methods**

| Method                                     | Purpose                                                                                                                                                                                                                                                                                                             |
|--------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `excludePartialValues()`                   | When calculating the moving average, and the full size of the window has not yet been reached, the gatherer should suppress emitting values until the lookback window is full. [See example.](#excluding-partial-values)                                                                                            |
| `treatNullAsZero()`                        | When encountering a `null` value in a stream, treat it as `BigDecimal.ZERO` instead. [See example.](#treating-null-as-zero)                                                                                                                                                                                         |
| `treatNullAs(BigDecimal replacement)`      | When encountering a `null` value in a stream, treat it as the given `replacement` value instead. [See example.](#replacing-null-with-another-bigdecimal)                                                                                                                                                            |
| `withMathContext(MathContext mathContext)` | Replace the `MathContext` used for all mathematical operations performed by this gatherer. [See example.](#specifying-a-new-mathcontext)                                                                                                                                                                            |
| `withOriginal()`                           | Include the original input value from the stream in addition to the calculated value in a [`WithOriginal`](https://github.com/tginsberg/gatherers4j/blob/main/src/main/java/com/ginsberg/gatherers4j/dto/WithOriginal.java)record. [See example.](#emitting-a-record-containing-the-original-and-calculated-values) |

### Examples

#### Simple moving average of window size 3, mapped from an object

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
    .gather(Gatherers4j.simpleMovingAverageBy(3, NamedValue::value))
    .toList();

// [
//   BigDecimal("1.0")
//   BigDecimal("1.5")
//   BigDecimal("4.333333333333333")
//   BigDecimal("10.66666666666667")
//   BigDecimal("20.0")
// ]
```

#### Excluding partial values

Showing that in-process moving average values are not emitted for each element until the lookback window has been filled.


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
    .gather(Gatherers4j.simpleMovingAverageBy(3, NamedValue::value).excludePartialValues())
    .toList();

// [
//   BigDecimal("4.333333333333333")
//   BigDecimal("10.66666666666667")
//   BigDecimal("20.0")
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
    .gather(Gatherers4j.simpleMovingAverageBy(3, NamedValue::value))
    .toList();

// [
//   BigDecimal("10.0")
//   BigDecimal("15.0")
//   BigDecimal("20.0")
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
    .gather(Gatherers4j.simpleMovingAverageBy(3, NamedValue::value).treatNullAsZero())
    .toList();

// [
//   BigDecimal("0")
//   BigDecimal("0")
//   BigDecimal("3.333333333333333")
//   BigDecimal("10.0")
//   BigDecimal("20.0")
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
    .gather(Gatherers4j.simpleMovingAverageBy(3, NamedValue::value).treatNullAs(BigDecimal.TWO))
    .toList();

// [
//   BigDecimal("2")
//   BigDecimal("2")
//   BigDecimal("4.666666666666667")
//   BigDecimal("10.66666666666667")
//   BigDecimal("20.0")
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
        .simpleMovingAverageBy(3, NamedValue::value)
        .withMathContext(new MathContext(3, RoundingMode.DOWN))
    )
    .toList();

// [
//   BigDecimal("1.0")
//   BigDecimal("1.5")
//   BigDecimal("4.33")
//   BigDecimal("10.6")
//   BigDecimal("20.0")
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
    .gather(Gatherers4j.simpleMovingAverageBy(3, NamedValue::value).withOriginal())
    .toList();

// [
//   WithOriginal[original=NamedValue[name=first, value=1.0], calculated=1.0]
//   WithOriginal[original=NamedValue[name=second, value=2.0], calculated=1.5]
//   WithOriginal[original=NamedValue[name=third, value=10.0], calculated=4.333333333333333]
//   WithOriginal[original=NamedValue[name=fourth, value=20.0], calculated=10.66666666666667]
//   WithOriginal[original=NamedValue[name=fifth, value=30.0], calculated=20.0]
// ]
```