# `Mono.fromRunnable()`

---

## 1. Core Concept: Lazy Side Effects Before an Empty Signal

In Reactive Programming, you frequently need to represent an operation that yields no value—only an empty completion signal. How you create this signal depends on whether a side effect or action must occur beforehand:

* **`Mono.empty()`**: Use when you want to emit an immediate completion signal without performing any computation or side effect. *(Analogy: `Mono.just()` for pre-existing values).*
* **`Mono.fromRunnable()`**: Use when you need to execute a specific side-effecting action before emitting the empty completion signal. *(Analogy: `Mono.fromSupplier()` for computed values).*

---

## 2. Real-World Use Case: E-Commerce Lookup

Consider a system looking up a product by its ID:

* **Product Exists (`productId == 1`)**: Uses `Mono.fromSupplier()` to lazily generate and return the product details.
* **Product Missing (`productId != 1`)**: Instead of silently returning `Mono.empty()`, the system must alert the business operations team so they can consider stocking it. It uses `Mono.fromRunnable()` to trigger `notifyBusiness()` and then completes with an empty signal.

> **Note on Return Types:** `Mono.fromRunnable()` returns a `Mono<Void>`. If a method signature requires a specific type like `Mono<String>`, chain `.then(Mono.<String>empty())` to ensure type safety while maintaining the side effect.

---

## 3. The Power of "Laziness"

A foundational principle of Project Reactor is: **Nothing happens until you subscribe.**

The action inside `Mono.fromRunnable()` is not executed when the method defining the `Mono` is called. It executes **only at the moment a subscriber subscribes** to the `Mono`. If no subscription occurs, the action never executes, avoiding unnecessary resource usage or unexpected side effects.

---

## 4. Handling Checked Exceptions in `Mono.fromRunnable()`

The Java `Runnable` interface's `run()` method does not declare `throws Exception`. Consequently, lambda expressions passed to `Mono.fromRunnable()` cannot throw checked exceptions directly.

If your side effect performs operations that throw checked exceptions (e.g., I/O or Database calls), you must handle or rethrow them as unchecked exceptions within the block.

### Example Code

```java
private static Mono<Void> notifyBusinessWithDb(int productId) {
    return Mono.fromRunnable(() -> {
        try {
            // Simulated operation throwing a checked exception
            saveMissingProductToDatabase(productId); 
            log.info("Successfully notified business for product {}", productId);
        } catch (Exception e) {
            // Handle or wrap in an unchecked RuntimeException
            log.error("Failed to notify business for product {}", productId, e);
            throw new RuntimeException("Database notification failed", e);
        }
    });
}

private static void saveMissingProductToDatabase(int productId) throws Exception {
    // Simulated database interaction failure
    throw new Exception("Database connection failed");
}
```

> **Alternative Note:** If you prefer to allow checked exceptions directly without manual `try-catch` blocks, consider using `Mono.fromCallable()`, which naturally supports thrown checked exceptions.

---

## 🔑 Key Takeaways

1. **`fromSupplier` vs. `fromRunnable`**: Use `fromSupplier` when producing a computed value; use `fromRunnable` when performing an action with no return value (`Mono<Void>`).
3. **Exception Handling**: Because `Runnable` cannot throw checked exceptions, any checked exceptions inside `Mono.fromRunnable()` must be caught or wrapped into a `RuntimeException`.