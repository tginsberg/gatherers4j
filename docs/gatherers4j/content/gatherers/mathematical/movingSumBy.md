---
title: "movingSumBy()"
linkTitle: "movingSumBy()"
show_in_table: true
category: "Mathematical Operations"
description: Calculate the moving sum of `BigDecimal` objects mapped from a `Stream<INPUT>` via a `mappingFunction` and looking back `windowSize` number of elements.

---

### Implementation Notes
This implementation is suitable mapping an arbitrary `Stream<INPUT>` to `BigDecimal` via a `mappingFunction`; for a version that operates directly on a `Stream<BigDecimal>`, see [`movingSum()`](/gatherers4j/gatherers/mathematical/movingsum/).
By default, nulls are ignored and play no part in calculations, see `treatNullAs()` and `treatNullAsZero()` below for ways to change this behavior. The default `MathContext`
for all calculations is {{< jdklink linkName="MathContext.DECIMAL64" package="java.base/java/math/MathContext.html#DECIMAL64" >}}, but this can be overridden (see `withMathContext()`, below).


**Signatures**

`movingSumBy(int windowSize, Function<INPUT, BigDecimal> mappingFunction)`
* `windowSize` - How many trailing elements to calculate the sum from at any given point in the stream
* `mappingFunction` - A non-null function to map stream `INPUT` elements into `BigDecimal` for calculation

**Additional Methods**

| Method                                     | Purpose                                                                                                                                                                                                                                                                                                         |
|--------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `includePartialValues()`                   | When calculating the moving sum and the full size of the window has not yet been reached, the gatherer should emit values for what it has. [See example.](#including-partial-values)                                                                                                                            |
| `treatNullAsZero()`                         | When encountering a `null` value in a stream, treat it as `BigDecimal.ZERO` instead. [See example.](#treating-null-as-zero)                                                                                                                                                                                     |
| `treatNullAs(BigDecimal replacement)`      | When encountering a `null` value in a stream, treat it as the given `replacement` value instead. [See example.](#replacing-null-with-another-bigdecimal)                                                                                                                                                        |
| `withMathContext(MathContext mathContext)` | Replace the `MathContext` used for all mathematical operations performed by this gatherer. [See example.](#specifying-a-new-mathcontext)                                                                                                                                                                        |
| `withOriginal()`                           | Include the original input value from the stream in addition to the calculated value in a [`WithOriginal`](https://github.com/tginsberg/gatherers4j/blob/main/src/main/java/com/ginsberg/gatherers4j/WithOriginal.java)record. [See example.](#emitting-a-record-containing-the-original-and-calculated-values) |


### Examples

#### Moving sum of window size 3, mapped from an object

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
    .gather(Gatherers4j.movingSumBy(3, NamedValue::value))
    .toList();

// [ 
//   BigDecimal("13.0"), 
//   BigDecimal("32.0"),
//   BigDecimal("60.0") 
// ]
```

#### Including partial values

Showing that an in-process sum is emitted for each element, even if there aren't 3 elements from which to calculate a sum yet.

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
    .gather(Gatherers4j.movingSumBy(3, NamedValue::value).includePartialValues())
    .toList();

// [ 
//   BigDecimal("1.0"), 
//   BigDecimal("3.0"),
//   BigDecimal("13.0"), 
//   BigDecimal("32.0"),
//   BigDecimal("60.0") 
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
    .gather(Gatherers4j.movingSumBy(3, NamedValue::value))
    .toList();

// [
//   BigDecimal("60.0") 
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
    .gather(Gatherers4j.movingSumBy(3, NamedValue::value).treatNullAsZero())
    .toList();

// [
//   BigDecimal("10.0"), 
//   BigDecimal("30.0"), 
//   BigDecimal("60.0") 
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
    .gather(Gatherers4j.movingSumBy(3, NamedValue::value).treatNullAs(BigDecimal.TWO))
    .toList();

// [
//   BigDecimal("14.0"), 
//   BigDecimal("32.0"), 
//   BigDecimal("60.0") 
// ]
```


#### Specifying a new `MathContext`


```java
record NamedValue(String name, BigDecimal value) {}

Stream
    .of(
        new NamedValue("first",  new BigDecimal("1.111")),
        new NamedValue("second", new BigDecimal("2.222")),
        new NamedValue("third",  new BigDecimal("10.333")),
        new NamedValue("fourth", new BigDecimal("20.444")),
        new NamedValue("fifth",  new BigDecimal("30.555"))
    )
    .gather(Gatherers4j
        .movingSumBy(3, NamedValue::value)
        .withMathContext(new MathContext(3, RoundingMode.DOWN))
    )
    .toList();

// [ 
//   BigDecimal("13.6"), 
//   BigDecimal("32.9"),
//   BigDecimal("61.2") 
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
    .gather(Gatherers4j.movingSumBy(3, NamedValue::value).withOriginal())
    .toList();

// [ 
//   WithOriginal[original=NamedValue[name=third, value=10.0], calculated=13.0]
//   WithOriginal[original=NamedValue[name=fourth, value=20.0], calculated=32.0]
//   WithOriginal[original=NamedValue[name=fifth, value=30.0], calculated=60.0]
// ]
```