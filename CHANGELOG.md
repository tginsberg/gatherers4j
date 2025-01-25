### 0.8.0 
+ Add support for `orElse()` and `orElseEmpty()` on size-based gatherers to provide a non-exceptional output stream
+ Implement `everyNth()` to get every `n`<sup>th</sup> element from the stream
+ Implement `uniquelyOccurring()` to emit stream elements that occur a single time
+ Implement `takeUntil()` to take from a stream until a predicate is met, including the first element that matches the predicate
+ Implement `foldIndexed()` to perform a traditional fold along with the index of each element
+ Implement `reduceIndexed()` to perform a reduce along with the index of each element

### 0.7.0
+ Use greedy integrators where possible (Fixes #57)
+ Add [JSpecify](https://jspecify.dev/) annotations for static analysis
+ Implement `orderByFrequencyAscending()` and `orderByFrequencyDescending()`
+ Implement `movingProduct()` and `movingProductBy()`
+ Implement `movingSum()` and `movingSumBy()`
+ Remove `maxBy(fn)` and `minBy(fn)`, can be done with JDK methods trivially
+ Rename `exactSize()` to `sizeExactly()`
+ Implement `sizeLessThan`, `sizeLessThanOrEqualTo`, `sizeGreaterThan`, and `sizeGreaterThanOrEqualTo`
+ API Style - Functions, when used as arguments, should come last for consistency and to play nice with Kotlin (Fixes #64)

### 0.6.0
+ Implement `dropLast(n)`
+ Implement `grouping()` and `groupingBy(fn)`
+ Add support for `zipWith(iterable)` and `zipWith(iterator)`
+ Add support for `interleave(iterable)` and `interleave(iterator)`
+ Add support for `appendLonger()`, `appendArgumentIfLonger()`, and `appendSourceIfLonger()` on `interleave()`
+ Add support for `argumentWhenSourceLonger()`, `sourceWhenArgumentLonger()`, `nullArgumentWhenSourceLonger()`, and `nullSourceWhenArgumentLonger` on `zipWith()`

### 0.5.0
+ Implement `reverse()`
+ Implement `maxBy(fn)` and `minBy(fn)`
+ Implement `exactSize(n)`
+ 
### 0.4.0
+ Implement `suffle()` and `shuffle(RandomGenerator)`
+ Implement `filterWithIndex()`
+ Implement `runningProduct()` and `runningProductBy()`
+ Implement `runningSum()` and `runningSumBy()`
+ Remove `concat()` implementation (the JDK has this)
+ Remove `withIndexStartingAt()`, not sure if this is a real use case.

### 0.3.0
+ Move minimum Java version from 22 to 23

### 0.2.0
+ Added starting point for indexing gatherer
+ Added throttling over a time period
+ Added debounce over a time period

### 0.1.0
+ Added Standard Deviation (population and sample)
+ Refactored all BigDecimal-based Gatherers
+ Added concat

### Initial Release 0.0.1