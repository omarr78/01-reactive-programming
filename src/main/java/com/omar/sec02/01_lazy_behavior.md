## Java Streams — Lazy Behavior

### Overview

Java 8 introduced the Stream API, which allows us to process data in a functional style.

One of the most important characteristics of Java Streams is lazy evaluation.

Lazy evaluation means that intermediate operations are not executed immediately. They execute only when a terminal operation is added to the stream pipeline.

This concept is important when learning reactive programming because Reactor's `Mono` and `Flux` also use lazy execution in many common scenarios.

### Understanding the Code

The example creates two streams and uses the `peek()` operation to log the elements being processed.

#### First Stream — Without a Terminal Operation

```java
Stream.of(1)
    .peek(i -> log.info("received: {}", i));
```

What happens here?

1. `Stream.of(1)` creates a stream containing the number `1`.
2. `peek()` defines an action that logs the element when it passes through the stream.
3. However, `peek()` is an intermediate operation, so it does not trigger stream processing.
4. Because no terminal operation is connected, the logging statement inside `peek()` is never executed.

**Result:** Nothing is printed by the first `peek()` operation.

#### Second Stream — With a Terminal Operation

```java
Stream.of(1)
    .peek(i -> log.info("received: {}", i))
    .toList();
```

What happens here?

1. `Stream.of(1)` creates a stream containing the number `1`.
2. `peek()` defines the logging action.
3. `toList()` is a terminal operation that triggers stream processing.
4. The element `1` passes through `peek()`, so the logging statement is executed.
5. The stream result is collected into a list. In this example, the returned list is not stored in a variable.

**Result:** The log displays `received: 1`.

### How This Relates to Reactive Programming

The same general idea appears in Project Reactor, where we work with `Mono` and `Flux`.

For example:

```java
Mono.just(1)
    .doOnNext(i -> log.info("received: {}", i));
```

Creating this `Mono` and defining `doOnNext()` does not, by itself, trigger the normal subscription-driven execution. A subscriber is needed.

```java
Mono.just(1)
    .doOnNext(i -> log.info("received: {}", i))
    .subscribe();
```

Now the subscription triggers the sequence, and `received: 1` is logged.

The concepts are similar, but there is an important distinction:

- **Java Streams:** A terminal operation such as `toList()` or `forEach()` triggers processing.
- **Project Reactor:** A subscription usually triggers processing for a cold `Mono` or `Flux`.

Reactive execution can also involve asynchronous work, cancellation, backpressure, and other behaviors that Java Streams do not provide in the same way.
