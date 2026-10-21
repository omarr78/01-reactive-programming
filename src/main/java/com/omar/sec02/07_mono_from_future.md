# Java Asynchronous Programming & Project Reactor

---

## 1. `CompletableFuture<T>`
Represents a result that will become available at some point in the future. It allows the current thread to continue working without blocking while a background task completes.
- **`T`**: The type of the expected result (e.g., `String`, `User`, `Void`).
- **Benefit**: Enables non-blocking coordination of slow or independent tasks (e.g., API calls, I/O).

## 2. `CompletableFuture.supplyAsync()`
Schedules a task to run asynchronously (typically on the shared `ForkJoinPool.commonPool()`).
- **Why a lambda?** It accepts a `Supplier<T>` (e.g., `() -> { ... }`). This passes the *instructions* for the work to be done, rather than executing the work immediately on the current thread.
- **Example**:
  ```java
  CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
      log.info("generating name");
      return Util.faker().name().firstName();
  });
  ```

## 3. Adapting to Reactor: `Mono.fromFuture()`
Reactor provides `Mono.fromFuture()` to bridge Java's `CompletableFuture` into a reactive `Mono` publisher. There are two distinct ways to use it, with critically different behaviors:

| Approach | Code Example | Execution Behavior |
| :--- | :--- | :--- |
| **Eager** | `Mono.fromFuture(getName())` | `getName()` is evaluated **immediately**. The async task starts *before* any subscription occurs. |
| **Lazy** | `Mono.fromFuture(Demo::getName)` | Passes a method reference (Supplier). `getName()` is **deferred** and only invoked *when a subscriber subscribes*. |

> **Best Practice**: Prefer the lazy approach (`Demo::getName` or `() -> getName()`) to align with reactive programming principles.

## 4. Laziness in Reactive Programming
**Lazy** means: *Do not start the work until somebody explicitly asks for the result (subscribes).*
- Creating a `Mono` is like setting up a coffee machine.
- Calling `.subscribe()` is like pressing the "Start" button.
- Without a subscription, a lazy `Mono` does nothing, saving resources.

## 5. The Role of `Util.sleepSeconds(1)`
In tutorial code, you will often see a sleep at the end of the main method:
```java
Mono.fromFuture(Demo::getName).subscribe(Util.subscriber("sub1"));
Util.sleepSeconds(1); // Demo only!
```
- **Why it's there**: `supplyAsync` uses daemon threads. If the main thread exits immediately, the JVM shuts down, killing the background task before it can finish. The sleep artificially keeps the main thread alive.
- **Production Reality**: Never use `sleep` for synchronization in production. Rely on proper application lifecycle management or testing tools like Reactor's `StepVerifier`.

---

# CompletableFuture vs. Mono: Quick Reference

---

## ❓ Why do we need to use `Mono` since we have `CompletableFuture`?

While `CompletableFuture` handles basic asynchronous tasks, **`Mono` is required to integrate those tasks into a reactive programming model.**

`CompletableFuture` is eager (starts immediately) and belongs to standard Java. `Mono` is lazy (starts only on subscription), supports backpressure, enables seamless composition with other reactive types (like `Flux`), and is the native requirement for reactive frameworks like Spring WebFlux.

If your app is purely standard Java, `CompletableFuture` is fine. If your app is reactive, you use `Mono` to keep the entire pipeline consistent, lazy, and composable.

---

## 1. Core Distinction
- **`CompletableFuture<T>`**: Java's standard **asynchronous** API. Represents a single eventual result or failure. Typically begins execution the moment it is created.
- **`Mono<T>`**: Project Reactor's **reactive** API. Represents a publisher that emits zero or one value. Integrates into reactive pipelines and supports subscription-based execution and cancellation.

---

## 2. Key Differences

| Feature | `CompletableFuture` | `Mono` |
| :--- | :--- | :--- |
| **Execution Model** | Eager (starts immediately upon creation) | Lazy (starts only upon `.subscribe()`) |
| **Composition** | `thenApply()`, `thenAccept()`, `thenCombine()` | `map()`, `flatMap()`, `zip()`, `concat()` |
| **Ecosystem** | Standard Java | Project Reactor (Spring WebFlux, R2DBC, etc.) |
| **Cancellation** | Limited / complex | Native via `Disposable` on subscription |

---

## 3. The Laziness Factor
This is the most critical behavioral difference when bridging the two:

```java
// EAGER: getName() is called immediately. Work starts now.
Mono.fromFuture(getName()); 

// LAZY: Method reference is passed. Work is deferred until .subscribe() is called.
Mono.fromFuture(Demo::getName); 
```
*Note: `CompletableFuture.supplyAsync()` is inherently eager. Wrapping it in a lazy `Mono` supplier is the recommended way to integrate it into a reactive pipeline.*

---

## 4. When to Use Which?

| Scenario | Recommended Choice |
| :--- | :--- |
| Simple, standalone asynchronous Java tasks | `CompletableFuture` |
| Building a Reactor-based application (e.g., Spring WebFlux) | `Mono` / `Flux` |
| Adapting a legacy/third-party async API into Reactor | `Mono.fromFuture(Supplier)` |
| Processing streams of multiple values over time | `Flux` |
