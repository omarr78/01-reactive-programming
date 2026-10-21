# `Mono.defer()` in Project Reactor

## Overview
`Mono.defer()` is a powerful method in Project Reactor used to **delay the creation of a Publisher** (`Mono` or `Flux`) until the exact moment a subscriber actually subscribes to it.

---

## The Problem: Eager Publisher Creation
Normally, creating a `Publisher` is a very lightweight operation. Time-consuming business logic is usually wrapped inside a `Supplier` (e.g., using `Mono.fromSupplier()`), which successfully delays the actual execution of the business logic until a subscription occurs.

However, what if the **creation of the Publisher itself** is a time-consuming operation?

If you define the Publisher normally, its creation happens **eagerly**—meaning the heavy operation is triggered immediately when the code is compiled/read, **even if there is no subscriber**.

### Example of the Problem
```java
// This will take 1 second to execute, even if NO ONE subscribes!
Mono<Integer> myPublisher = createPublisher(); 
```

---

## The Solution: `Mono.defer()`
`Mono.defer()` solves this by taking a `Supplier<Mono<T>>` (or a method reference that returns a `Mono`) as an argument.

* **Lazy Creation:** It defers the creation of the actual `Mono` until a subscription happens.
* **No Subscriber, No Execution:** If there is no subscriber, the method exits immediately, and the time-consuming Publisher creation is completely avoided.
* **On Subscription:** When a subscriber attaches, `Mono.defer()` invokes the supplier, creates the Publisher, and then subscribes to it to get the value.

---

*(Notice how the first `Mono.defer()` without a subscriber prints nothing and takes no time!)*

## 💡 Key Takeaways

1. **Lightweight vs. Heavyweight Creation:** Creating a Publisher should ideally be a lightweight operation. If it is lightweight, `Mono.defer()` is usually unnecessary.
2. **Delaying the Delay:** `Mono.fromSupplier()` delays **business logic execution**. `Mono.defer()` delays **Publisher creation**.
3. **True Laziness:** `Mono.defer()` guarantees that your expensive Publisher creation logic will **never run** unless there is an active subscriber waiting for the data.
4. **Method References:** `Mono.defer()` accepts a `Supplier<Mono>`, making it very clean to use with Java method references (e.g., `Demo::createPublisher`).

# `Mono.defer()` vs `Mono.fromSupplier()` in Project Reactor

Replacing `Mono.defer()` with `Mono.fromSupplier()` (or vice versa) is not a direct swap because they accept different return types and serve distinct structural purposes.

---

## Key Differences

| Feature | `Mono.defer()` | `Mono.fromSupplier()` |
| :--- | :--- | :--- |
| **Expected Argument** | `Supplier<Mono<T>>` | `Supplier<T>` |
| **Return Type of Supplier** | A Publisher (`Mono<T>`) | A raw value (`T`) |
| **Primary Use Case** | Deferring execution of a method that **already returns a `Mono`**. | Deferring synchronous execution of a block that **returns a raw value**. |

---

## Why They Are Not Interchangeable

### 1. Type Mismatch / Nested Mono Anti-Pattern
- **`Mono.defer`** expects a supplier returning a `Mono<T>`.
- **`Mono.fromSupplier`** expects a supplier returning a plain value `T`.
- **The Pitfall:** If you pass a method returning `Mono<T>` into `Mono.fromSupplier()`, it treats the entire `Mono<T>` as the raw value `T`, producing a nested **`Mono<Mono<T>>`**.

### 2. Execution Timing & Scope
- **`Mono.defer()`** delays the **invocation of the method** that generates the `Mono` until subscription time.
- **`Mono.fromSupplier()`** only delays the code **inside its own lambda**. Any logic outside the lambda (such as eager logging or setup steps) executes immediately upon method call.

---

## How to Choose

- **Use `Mono.defer()`** when you already have a method returning `Mono<T>` and need to ensure the creation and setup logic inside that method is delayed until a subscriber subscribes.
- **Use `Mono.fromSupplier()`** when wrapping synchronous, blocking operations (e.g., database lookup, computational tasks) that produce a raw value `T`.