---
title: Guiding Principles
linkTitle: Guiding Principles
weight: 90
---

1. Consider adding a gatherer if it cannot be implemented with `map`, `filter`, or a collector without enclosing outside
   state.

2. Resist the temptation to add functions that only exist to provide an alias. They seem fun/handy but add surface area
   to the API and must be maintained forever.

3. All features should be documented and tested.

4. Follow the naming convention, unless it makes things unclear.


### Naming Convention

* Use imperative tense when possible (prefer `group()` over `grouping()` or `grouped()`)

* `...By()` - Perform the action on the output of the given function (usually a `Function` or `Comparator`)

* `ensure...` - Make sure something is true and fail if it isn't

* `...withIndex` - Perform the action, including the zero-based index of the element in the stream