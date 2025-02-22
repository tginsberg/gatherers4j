---
title: Getting Started
linkTitle: Getting Started
weight: 2
menu: {main: {weight: 20}}
---

Gatherers4j releases are published to Maven Central, the latest version is {{< env "PROJECT_VERSION" >}}.

### Maven

Add the following dependency to `pom.xml`:

```xml
<dependency>
    <groupId>com.ginsberg</groupId>
    <artifactId>gatherers4j</artifactId>
    <version>{{< env "PROJECT_VERSION" >}}</version>
</dependency>
```
### Gradle

Add the following dependency to `build.gradle` or `build.gradle.kts`:

```groovy
implementation('com.ginsberg:gatherers4j:{{< env "PROJECT_VERSION" >}}')
```

### Example Usage

The `Gatherers4j` class is the main entrypoint for this library. 

Here's a simple example that imports `Gatherers4j`, sets up a `Stream<String>`, 
keeps the last three elements, and puts them in a `List<String>`:

```java
import com.ginsberg.gatherers4j.Gatherers4j;
import java.util.stream.Stream;

Stream.of("A", "B", "C", "D", "E")
    .gather(Gatherers4j.takeLast(3))
    .toList();

// Returns: [ "C", "D", "E" ]
```