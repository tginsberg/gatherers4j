---
title: "movingRootMeanSquareBy()"
linkTitle: "movingRootMeanSquareBy()"
show_in_table: true
category: "Mathematical Operations"
description: Calculate the moving root mean square (quadratic mean) of `BigDecimal` objects mapped from a `Stream<INPUT>` via a `mappingFunction`.

---

### Implementation Notes
This implementation is suitable for mapping an arbitrary `Stream<INPUT>` to `BigDecimal` via a `mappingFunction`; for a version that operates directly on a `Stream<BigDecimal>`, see [`movingRootMeanSquare()`](/gatherers4j/gatherers/mathematical/movingrootmeansquare/).
By default, nulls are ignored and play no part in calculations, see `treatNullAs()` and `treatNullAsOne()` below for ways to change this behavior. The default `MathContext`
for all calculations is {{< jdklink linkName="MathContext.DECIMAL64" package="java.base/java/math/MathContext.html#DECIMAL64" >}}, but this can be overridden (see `withMathContext()`, below).

**Signatures**

`movingRootMeanSquareBy(int windowSize, Function<INPUT, BigDecimal> mappingFunction)`
* `windowSize` - How many trailing elements to calculate the root mean square over
* `mappingFunction` - A non-null function to map stream `INPUT` elements into `BigDecimal` for calculation

**Additional Methods**

| Method                                     | Purpose                                                                                                                                                                                                                                                                                                              |
|--------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `excludePartialValues()`                   | When calculating the moving root mean square, and the full size of the window has not yet been reached, the gatherer should suppress emitting values until the lookback window is full. [See example.](#excluding-partial-values)                                                                                    |
| `treatNullAsZero()`                        | When encountering a `null` value in a stream, treat it as `BigDecimal.ZERO` instead. [See example.](#treating-null-as-zero)                                                                                                                                                                                          |
| `treatNullAs(BigDecimal replacement)`      | When encountering a `null` value in a stream, treat it as the given `replacement` value instead. [See example.](#replacing-null-with-another-bigdecimal)                                                                                                                                                             |
| `treatNullAsOne()`                         | When encountering a `null` value in a stream, treat it as `BigDecimal.ONE` instead. [See example.](#treating-null-as-one)                                                                                                                                                                                            |
| `withMathContext(MathContext mathContext)` | Replace the `MathContext` used for all mathematical operations performed by this gatherer. [See example.](#specifying-a-new-mathcontext)                                                                                                                                                                             |
| `withOriginal()`                           | Include the original input value from the stream in addition to the calculated value in a [`WithOriginal`](https://github.com/tginsberg/gatherers4j/blob/main/src/main/java/com/ginsberg/gatherers4j/dto/WithOriginal.java) record. [See example.](#emitting-a-record-containing-the-original-and-calculated-values) |

### Examples

#### Moving root mean square, mapped from an object

```java
record NamedValue(String name, BigDecimal value) {}

Stream
    .of(
        new NamedValue("first",  BigDecimal.valueOf(1)),
        new NamedValue("second", BigDecimal.valueOf(2)),
        new NamedValue("third",  BigDecimal.valueOf(4))
    )
    .gather(Gatherers4j.movingRootMeanSquareBy(2, NamedValue::value))
    .toList();

// [ 
//   BigDecimal("1"), 
//   BigDecimal("1.58113883008419"),
//   BigDecimal("3.162277660168379") 
// ]
```

#### Excluding partial values

Showing that in-process moving root mean square values are not emitted for each element until the lookback window has been filled.

```java
record NamedValue(String name, BigDecimal value) {}

Stream
    .of(
        new NamedValue("first",  BigDecimal.valueOf(1)),
        new NamedValue("second", BigDecimal.valueOf(2)),
        new NamedValue("third",  BigDecimal.valueOf(4))
    )
    .gather(Gatherers4j.movingRootMeanSquareBy(2, NamedValue::value).excludePartialValues())
    .toList();

// [ 
//   BigDecimal("1.58113883008419"),
//   BigDecimal("3.162277660168379") 
// ]
```

#### Showing nulls are ignored by default

```java
record NamedValue(String name, BigDecimal value) {}

Stream
    .of(
        new NamedValue("first",  null),
        new NamedValue("second", BigDecimal.valueOf(1)),
        new NamedValue("third",  BigDecimal.valueOf(2)),
        new NamedValue("fourth", BigDecimal.valueOf(4))
    )
    .gather(Gatherers4j.movingRootMeanSquareBy(2, NamedValue::value))
    .toList();

// [
//   BigDecimal("1"), 
//   BigDecimal("1.58113883008419"), 
//   BigDecimal("3.162277660168379") 
// ]
```

#### Treating null as zero

```java
record NamedValue(String name, BigDecimal value) {}

Stream
    .of(
        new NamedValue("first",  null),
        new NamedValue("second", BigDecimal.valueOf(1)),
        new NamedValue("third",  BigDecimal.valueOf(2))
    )
    .gather(Gatherers4j.movingRootMeanSquareBy(2, NamedValue::value).treatNullAsZero())
    .toList();

// [
//   BigDecimal("0"),
//   BigDecimal("0.7071067811865475"),
//   BigDecimal("1.58113883008419")
// ]
```

#### Treating null as one

```java
record NamedValue(String name, BigDecimal value) {}

Stream
    .of(
        new NamedValue("first",  null),
        new NamedValue("second", BigDecimal.valueOf(1)),
        new NamedValue("third",  BigDecimal.valueOf(2))
    )
    .gather(Gatherers4j.movingRootMeanSquareBy(2, NamedValue::value).treatNullAsOne())
    .toList();

// [
//   BigDecimal("1"),
//   BigDecimal("1"),
//   BigDecimal("1.58113883008419")
// ]
```

#### Replacing null with another `BigDecimal`


```java
record NamedValue(String name, BigDecimal value) {}

Stream
    .of(
        new NamedValue("first",  null),
        new NamedValue("second", BigDecimal.valueOf(1)),
        new NamedValue("third",  BigDecimal.valueOf(2))
    )
    .gather(Gatherers4j.movingRootMeanSquareBy(2, NamedValue::value).treatNullAs(BigDecimal.valueOf(27)))
    .toList();

// [
//   BigDecimal("27"),
//   BigDecimal("19.1049731745428"),
//   BigDecimal("1.58113883008419") 
// ]
```


#### Specifying a new `MathContext`


```java
record NamedValue(String name, BigDecimal value) {}

Stream
    .of(
        new NamedValue("first",  BigDecimal.valueOf(1)),
        new NamedValue("second", BigDecimal.valueOf(2)),
        new NamedValue("third",  BigDecimal.valueOf(4))
    )
    .gather(Gatherers4j
        .movingRootMeanSquareBy(2, NamedValue::value)
        .withMathContext(new MathContext(3))
    )
    .toList();

// [ 
//   BigDecimal("1"), 
//   BigDecimal("1.58"),
//   BigDecimal("3.16")
// ]
```


#### Emitting a record containing the original and calculated values


```java
record NamedValue(String name, BigDecimal value) {}

Stream
    .of(
        new NamedValue("first",  BigDecimal.valueOf(1)),
        new NamedValue("second", BigDecimal.valueOf(2))
    )
    .gather(Gatherers4j.movingRootMeanSquareBy(2, NamedValue::value).withOriginal())
    .toList();

// [ 
//   WithOriginal[original=NamedValue[name=first, value=1], calculated=1],
//   WithOriginal[original=NamedValue[name=second, value=2], calculated=1.58113883008419]
// ]
```
