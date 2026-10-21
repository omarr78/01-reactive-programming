### Different Ways to Subscribe to a `Mono`

#### Case 1: Subscribe with Only One Consumer

```java
var mono = Mono.just(1);

mono.subscribe(
        i -> log.info("received: {}", i)
);
```

- `Mono.just(1)` creates a publisher that emits the value `1` and then completes, unless cancelled.
- `subscribe()` registers a subscriber.
- The lambda `i -> log.info(...)` handles the emitted value.

**Expected log:**

```text
received: 1
```

Why don't we see `completed`?

Because we provided only the handler for the emitted value (`onNext`). We did not provide an `onComplete` handler.

The completion signal still occurs, but we haven't told Reactor what to do when it happens.

#### Case 2: Handle All the Signals

```java
mono.subscribe(
        i -> log.info("received: {}", i),
        err -> log.error(err.getMessage(), err),
        () -> log.info("completed"),
        subscription -> subscription.request(10)
);
```

The four arguments handle different responsibilities:

| Argument | Purpose |
| :--- | :--- |
| First | Receives values (`onNext`) |
| Second | Handles errors (`onError`) |
| Third | Handles successful completion (`onComplete`) |
| Fourth | Receives the `Subscription`, allowing you to request or cancel |

**Expected log:**

```text
received: 1
completed
```

There is no error, so the error handler is not invoked.

#### Case 3: Cancel the Subscription

```java
subscription -> subscription.cancel()
```

- `request(1)` means you want one delivery.
- `cancel()` means you want to stop the service.

In this case, the subscription is cancelled immediately, before any value is requested.

**Expected result:** no received value and no completion message.

#### Case 4: Cause an Error Using `map()`

```java
var mono = Mono.just(1)
        .map(i -> i / 0);
```

The `map()` operator transforms the value emitted by a publisher.

Imagine a publisher sends the number `1`, and you want to perform a calculation before passing it to the subscriber.

Here, the calculation is `1 / 0`, which throws an `ArithmeticException`.

The exception is delivered through `onError`, so the error handler logs it.

**Expected result:**
- No `received: 1` message, because the transformation fails.
- An error message containing `ArithmeticException: / by zero`.
- No `completed` message, because the sequence terminates with an error.

#### The Most Important Concept: Automatic Requests

You might wonder: Why did Case 1 work even though we never called `request()`?

When you use the convenience overload:

```java
mono.subscribe(i -> log.info("received: {}", i));
```

Reactor manages the subscription and demand for you. In particular, its convenience subscriber requests an unbounded amount of data (`Long.MAX_VALUE`), rather than making you request each item manually.

When you provide the four-argument overload, you can access the `Subscription` and control demand yourself.

For example:

```java
subscription -> subscription.request(1)
```

This requests one item. Since a `Mono` emits at most one item, that is enough to receive its value.