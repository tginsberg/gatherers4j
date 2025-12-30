
---
title: "Gatherers4J"
linkTitle: "Gatherers4J"
type: "docs"
weight: 20
description: A library of useful custom intermediate stream operations for Java.
cascade:
- _target:
    path: "/blog/**"
  type: "blog"
  # set to false to include a blog section in the section nav along with docs
  toc_root: true
- _target:
    path: "/**"
    kind: "page"
  type: "docs"
- _target:
    path: "/**"
    kind: "section"
  type: "docs"
- _target:
    path: "/**"
    kind: "section"
  type: "home"
---

## Why Are We Here?

Since Streams were first introduced in Java 8, there has been a way for us to write our own terminal operations 
with Collectors. However, there has not been a convenient way to write our own intermediate operations. Also, there
are a lot of intermediate operations that seem like they would be good additions to the JDK, but
adding all of them to the Stream API would make it more difficult to learn and maintain.

Enter Gatherers! Finally, we have the ability to plug our own intermediate operations into Java Streams, so we
can make life easier for ourselves, or use that sequence operation from another language that we miss in Java. 

This library aims to provide a comprehensive and useful set of Gatherers (intermediate operations).

## Shortcuts

* [Getting Started](/gatherers4j/start) - The quickstart guide will show you how to add Gatherers4j to your project.

* Gatherers are organized into five categories for easier searching:
  * [Sequence Operations](/gatherers4j/gatherers/sequence-operations/) - Reorder, combine, or manipulate the sequence of elements.

  * [Filtering and Selection](/gatherers4j/gatherers/filtering-and-selection/) - Select or remove elements based on some criteria.

  * [Grouping and Windowing](/gatherers4j/gatherers/grouping-and-windowing/) - Collect elements into groups or windows.

  * [Mathematical Operations](/gatherers4j/gatherers/mathematical/) - Perform calculations over the stream.

  * [Validation and Constraints](/gatherers4j/gatherers/validation-and-constraints/) - Enforce conditions on the stream.

## Features


**A Single Tiny Dependency**

Don't you hate it when you find a library that meets your needs only to discover that it comes with a 
massive tree of mandatory dependencies that conflict with your project? 

Gatherers4j has a single dependency. [JSpecify](https://jspecify.dev/) annotations are used on all public methods and types 
so static type checkers can more easily check for nullability issues. It's there to help you write better code.
JSpecify is _tiny_ (it is made up of only four annotations) and _stable_. 

**A Single Entrypoint**

All the Gatherers in this library are exposed through a single class, 
[`Gatherers4j`](https://javadoc.io/doc/com.ginsberg/gatherers4j/latest/com/ginsberg/gatherers4j/Gatherers4j.html)for 
simplicity. The groups outlined above are only represented explicitly in the documentation.

**Apache 2.0 License**

This project is licensed under the business-friendly [Apache 2.0 license](https://github.com/tginsberg/gatherers4j/?tab=Apache-2.0-1-ov-file#readme), which 
means your legal team can probably stop sweating because Apache 2.0 is generally well-received in enterprise settings. 
You should definitely run this by your Actual Lawyers&trade; though!
