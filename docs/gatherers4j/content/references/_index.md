---
title: References
linkTitle: References
weight: 100
---

### JEP-485
[JEP-485](https://openjdk.org/jeps/485) is the JDK Enhancement Proposal (JEP) that was used to deliver Stream Gatherers. 
It contains quite a good explanation as to why we need Gatherers and details of how we got where we are today. If you
are interested in the history of this proposal, there are previous versions available (see the "History" sections 
for an explanation of changes): [JEP-473 (Second Preview)](https://openjdk.org/jeps/473) and  [JEP-461 (First Preview)](https://openjdk.org/jeps/461).


### JSpecify
[JSpecify](https://jspecify.dev/) is an open-source Java tool that adds nullness annotations to improve static analysis and prevent null-related runtime errors. It helps developers identify potential null pointer issues at compile-time, ensuring safer and more reliable code.

Gatherers4j annotates all methods and types with JSpecify to help you write better code.

### Gatherers Overview and Examples

[Stream Gatherers: The Missing Piece in Java Stream Processing](https://todd.ginsberg.com/post/java/gatherers/) - A blog post I wrote on Gatherers explaining what they are, why we need them, and how to write your own.

[Some guidance](https://docs.oracle.com/en/java/javase/24/core/stream-gatherers.html) from Oracle on what Stream Gatherers are and how to create them.
