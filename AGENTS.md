# AGENTS.md

Instructions for AI agents working on Gatherers4j. When this file and the code disagree, the code wins. Fix this file in
the same change.

## Project

A Java library of custom [Stream Gatherers](https://openjdk.org/jeps/485), published as `com.ginsberg:gatherers4j`.
JSpecify is its only runtime dependency. Java 25, no preview features (CI also builds on 27). Gradle Kotlin DSL, JUnit
Jupiter, AssertJ, ErrorProne and NullAway (errors in main code, disabled in tests), Pitest, and a Hugo/Docsy site in
`docs/gatherers4j/`.

| Task | Command |
|---|---|
| Build and run all tests | `./gradlew build` |
| One test class | `./gradlew test --tests "com.ginsberg.gatherers4j.TakeUntilGathererTest"` |
| Build on another JDK | `./gradlew build -PjavaVersion=27` |
| Mutation testing | `./gradlew pitest` (fails below 75% mutation score or 80% line coverage) |
| Benchmarks | `./gradlew jmh` |
| Preview the docs site | `docs/gatherers4j/start.sh` (needs extended Hugo 0.160.1 or newer, plus npm) |

## Layout

```
src/main/java/com/ginsberg/gatherers4j/
    Gatherers4j.java                 the facade: every public factory method lives here
    *Gatherer.java, *Gatherers.java  implementations (a *Gatherers file holds several small ones)
    dto/, enums/                     exported, so public API
    util/                            not exported: GathererUtils, CircularBuffer, MathUtils
src/test/java/com/ginsberg/gatherers4j/
    test/, util/                     shared test helpers
```

## Public API

New public API is the maintainer's decision. Propose it first unless you were asked to build it. It must pass the
[guiding principles](docs/gatherers4j/content/guiding-principles/_index.md): not doable with `map`, `filter` or a
collector without enclosing outside state, not an alias, and named by convention (imperative verbs, `...By()` for
mapped or comparator variants, `ensure...()` for validation, `...Indexed()` for index-aware variants).

The library is pre-1.0 and has shipped breaking changes in minor releases. Treat a new public method as permanent
anyway.

## Library code

### The facade

- Users create every gatherer through a `public static` method on `Gatherers4j`. Gatherer constructors are
  package-private.
- Return `Gatherer<INPUT, ?, OUTPUT>`, or the concrete `public` type when the gatherer has fluent options (for example
  `ZipWithGatherer`).
- A gatherer that takes a second source gets four overloads: `Iterable`, `Iterator`, `Stream` and varargs, as in
  `crossWith`, `interleaveWith` and `zipWith`.
- A mapping function or `Comparator` is the last parameter: `movingStandardDeviationBy(dataset, windowSize, fn)`.
- Wildcards follow the JDK: `? super` for what a parameter consumes, `? extends` for what it produces
  (`Predicate<? super INPUT>`, `Comparator<? super INPUT>`, `Function<? super INPUT, ? extends BigDecimal>`,
  `Iterable<? extends SECOND>`, `Supplier<? extends Stream<? extends INPUT>>`), and `Comparable` bounds are
  `INPUT extends Comparable<? super INPUT>`. Without them a caller passing a broader type gets its element type widened.
- Keep facade methods in alphabetical order.
- Every factory method gets a Markdown doc comment (`///`) with an `@param` for each parameter and type parameter and
  an `@return` ("A non-null `Gatherer`"). Doclint runs with `all,-missing`, so nothing flags a missing comment or tag.

### Implementing a gatherer

- **State:** per-stream mutable state goes in a nested `static class State` created by `initializer()`, because a
  gatherer instance can be reused across streams. The only exception is a single-use second source (`Iterator` or
  `Stream`), which `InterleavingGatherer` and `ZipWithGatherer` keep in a field.
- **Integrator:** use `Integrator.ofGreedy(...)` unless the gatherer can stop early on its own, as `takeUntil` does.
  Return the result of `downstream.push(...)` or `!downstream.isRejecting()`.
- **Pushing many elements:** use `GathererUtils.pushAll`, which stops as soon as the downstream rejects.
- **Combiner:** implement `combiner()` only when the gatherer emits nothing until its finisher and its state merges
  exactly, as in `FrequencyGatherer` and `UniquelyOccurringGatherer`. A gatherer that emits per element can't have a
  correct combiner, so leave the default.
- **Argument validation:** in the constructor or factory method, never in the integrator. Null arguments use
  `GathererUtils.mustNotBeNull(arg, "Predicate must not be null")`, not `Objects.requireNonNull`. Other invalid values
  throw `IllegalArgumentException` naming the parameter and the rule: "Window size must be greater than 1".
- **Windows:** use `util.CircularBuffer` for fixed-size trailing windows.
- **New types:** reuse the existing `dto` records and `enums` first. Prefer an enum to a boolean flag in a public
  signature.

### Nullness and style

- Every package is `@NullMarked`. Gatherers accept null stream elements, so declare element type parameters as
  `INPUT extends @Nullable Object`.
- A function parameter's nullness matches what the gatherer does at runtime: its return is `@Nullable` when the
  gatherer handles a null result (the BigDecimal engine skips it, key functions compare with `Objects.equals`), and
  its input is `? super INPUT`, never `? super @Nullable INPUT`. Annotate only what fails on null as non-null.
- Generic records in `dto` take their nullness from the type argument: `FIRST first`, not `@Nullable FIRST first`.
  A gatherer that can put `null` into a component the caller declared non-null must say so in its return type, as
  `zipWith(...).nullArgumentWhenSourceLonger()` does.
- NullAway runs in JSpecify mode on main code.
- `src/nullnessUsage` holds compile-only call sites checked by NullAway in JSpecify mode as part of `check`.
  NullAway checks a method reference against its functional interface but not generic type arguments, so write the
  call sites as method references.
- Every source file starts with the Apache 2.0 license header from the existing files, with the current year.
- `final` on parameters and on locals that aren't reassigned.
- Type parameters are full words (`INPUT`, `OUTPUT`, `MAPPED`, `SECOND`), not `T` or `R`.
- Methods and fields in alphabetical order unless another grouping clearly reads better.

## BigDecimal statistics engine

Every running and moving BigDecimal statistic (sum, product, the means, RMS, median, percentile, variance, standard
deviation, EMA, statistical summary) runs on one engine:

- `BigDecimalRunningStatGatherer` and `BigDecimalMovingStatGatherer` are the public types the facade returns. With
  their package-private base `AbstractBigDecimalStatGatherer`, they own the mapping function, `treatNullAs*`,
  `withMathContext` (default `DECIMAL64`), `withOriginal()`, and for the moving type the window and
  `excludePartialValues()`.
- Each statistic is a `StatisticAccumulator` in `StatisticAccumulators` (`add`, `evict` for the moving form, `isReady`,
  `value`), built on the private blocks `CountSum`, `Extremes`, `Moments`, `Product` and `SortedWindow`.

To add a statistic, add an accumulator (implement `evict` if it should have a moving form), then facade methods: an
identity form over `@Nullable BigDecimal` passing `Function.identity()` and a `...By` form, each running, moving or
both. A population/sample choice is a `Dataset` parameter, not a separate method. Don't write a new `Gatherer` class.

Numeric rules. `BigDecimalVarianceGathererTest`, `BigDecimalStandardDeviationGathererTest`,
`BigDecimalExponentialMovingAverageGathererTest` and `BigDecimalMovingStatOracleTest` guard these. Don't "simplify"
them:

- **Additive statistics** (sum, mean, harmonic mean, RMS, variance, weighted mean) round each input to the
  `MathContext` once, on entry, then add and subtract exactly so eviction reverses an addition exactly. Harmonic mean
  rounds `1/x` and RMS rounds `x²` instead of the input, and eviction rounds the departing term the same way.
- **`Moments` squares exactly.** Variance is `(n*sumSq - sum*sum) / (n*n)`, or `/ (n*(n-1))` for a sample, rounded
  only in that divide. Rounding earlier gives negative variance for `1.000000000000001, 1.000000000000002,
  1.000000000000003`.
- **Product, geometric mean and EMA** round at every step, because exact accumulation grows digits without bound.
- **Moving product never divides.** Dividing out a departing value rounds, and the error builds over the stream.
  `Product` evicts with two stacks (amortized O(1)) so each result is built only from values in the window.
- **Output:** `value()` never returns more digits than the `MathContext` allows. Sum rounds its exact total only on
  the way out.
- **Nulls** still null after mapping and replacement are skipped: no output, no window movement.
- **Undefined results emit nothing; invalid input throws.** `Dataset.Sample` before two values, weights summing to
  zero in a partial window, and harmonic-mean reciprocals cancelling to zero emit nothing and continue. Unusable input,
  such as `0` for harmonic mean, throws. Never emit a placeholder.
- **`withMathContext`** rejects precision 0 (`MathContext.UNLIMITED`); divides and square roots need a finite one.
- **Interpolation scale:** median and percentile drop trailing zeros that interpolation adds, but keep at least as
  many decimal places as the two values they interpolate between.

## Tests

- One class per gatherer, `[Name]GathererTest` (or `[Name]GatherersTest` for a multi-gatherer source file). Mark
  Arrange, Act and Assert with comments. Group modes or overloads with `@Nested`.
- Cover: an empty stream, a single element, null elements, each fluent option, every null argument with its exact
  message (`assertThatIllegalArgumentException().isThrownBy(...).withMessage("...")`), and invalid numeric arguments
  with `@ParameterizedTest` and `@ValueSource`.
- A gatherer with a `combiner()` is tested with `@ParallelAndSequentialTest(values = {...})` from the `test` package.
- Compare `BigDecimal` with `.usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)`, and records holding
  them with `.usingRecursiveFieldByFieldElementComparator(TestUtils.BIG_DECIMAL_RECURSIVE_COMPARISON)`.
- Run `./gradlew pitest` after adding a gatherer. A surviving mutant usually means an untested edge case.

## Documentation

Every public factory method needs, in the same change:

1. **A docs page** at `docs/gatherers4j/content/gatherers/<category-directory>/<methodName>.md`, one per `By` variant.
   Copy the front matter and section layout from a page in the same directory: `### Implementation Notes`,
   `**Signature(s)**`, an `**Additional Methods**` table for fluent options, then `### Examples` with one `####` per
   example and the output in a trailing `// [...]` comment. Link JDK types with the `jdklink` shortcode.
2. **A README row** in the matching category table, alphabetical, linking to
   `https://tginsberg.github.io/gatherers4j/gatherers/<category-directory>/<methodname, lowercased>/`.
3. **A changelog bullet** under the unreleased version in `docs/gatherers4j/content/changelog/_index.md`. Call out
   every rename, removal and behavior change. The root `CHANGELOG.md` only points there.

Add a JMH benchmark only when a performance decision depends on it.

## Versions, branches and files

- `VERSION.txt` holds `X.Y.Z-SNAPSHOT` during development. `main` and `release/*` publish it verbatim. Other branches
  publish `<last branch path segment>-SNAPSHOT`.
- Release work goes on `release/X.Y.Z`. Feature branches merge into it, and it merges to `main` when the release ships.
- `.gitattributes` forces LF on every text file except `*.bat` (CRLF). Write new files with LF, and don't rewrite files
  whose content hasn't changed.
