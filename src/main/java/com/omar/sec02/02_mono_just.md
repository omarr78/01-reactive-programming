## Understanding `Mono.just()` in Project Reactor

The main topic of this lecture is how to create a `Mono` publisher using `Mono.just()`, how a subscriber receives its value, and why wrapping an existing value in a publisher can be useful when working with reactive libraries.

### 1. What is a `Mono`?

A `Mono<T>` is a Project Reactor publisher that can emit zero or one item, followed by a completion signal or an error signal.

For example:

```java
Mono<String> mono = Mono.just("Omar");
```

This creates a publisher that represents the value `"Omar"`.

- `Mono<String>` means the publisher emits a string, if it emits a value.
- `Mono.just("Omar")` creates a publisher containing the value `"Omar"`.
- `Mono.just(10)` creates a `Mono<Integer>`.
- `Mono.just(10.5)` creates a `Mono<Double>`.

The `just()` factory method is useful when you already have a value in your application and want to represent it as a reactive publisher.

### 2. Creating a `Mono` Does Not Mean Printing Its Value

Consider this line:

```java
var mono = Mono.just("Omar");
log.info("Mono created: {}", mono);
```

You might see output similar to:

```text
Mono created: MonoJust
```

**Important:** Printing the `Mono` object is not the same as receiving its data. The log typically shows a description of the publisher, not the string `"Omar"`.

To receive the value, a subscriber must subscribe to the publisher.

### 3. Subscribing and Requesting Data

```java
var subscriber = new SubscriberImpl();
mono.subscribe(subscriber);
```

This connects the subscriber to the publisher.

With the Reactive Streams API, the subscriber receives a subscription and can request data through it. A subscriber that has not requested any items has not given the publisher permission to deliver those items.

Next:

```java
subscriber.getSubscription().request(10);
```

This requests up to 10 items. However, because this is a `Mono`, it can emit at most one item.

The expected sequence is:

1. The subscriber subscribes to the `Mono`.
2. The subscriber requests 10 items.
3. The publisher emits `"Omar"`.
4. The subscriber receives the value.
5. The publisher sends the completion signal.

The publisher does not need to emit 10 items just because the subscriber requested 10. The request specifies the maximum number of items the subscriber is prepared to receive, not the number the publisher must produce.

### 4. Requesting Twice and Cancelling

The code contains these three operations:

```java
subscriber.getSubscription().request(10);
subscriber.getSubscription().request(10);
subscriber.getSubscription().cancel();
```

Here's what happens with a normal `Mono.just("Omar")` and a subscriber that doesn't automatically request items:

| Operation | What happens |
| :--- | :--- |
| First `request(10)` | The `Mono` emits `"Omar"` and completes. |
| Second `request(10)` | No additional item is emitted because the publisher has already completed. |
| `cancel()` | No effect on the already completed publisher. |

The important distinction is that a `Mono` completes after emitting its single value; it does not keep producing values while the subscriber continues requesting them.

Cancellation is useful when a subscription is still active and you want to stop receiving further signals. In this example, the publisher has already completed before cancellation occurs.

### 5. Why Use `Mono.just()`?

Imagine that your application already has a string in memory:

```java
String name = "Omar";
```

But an existing library expects a `Publisher<String>` rather than a regular `String`.

You cannot pass the string directly:

```java
save(name); // Does not compile if save() expects a Publisher<String>
```

Instead, you can wrap it in a `Mono`:

```java
save(Mono.just(name));
```

This works because `Mono` implements the Reactive Streams `Publisher` interface, so a `Mono<String>` can be passed wherever a `Publisher<String>` is expected.