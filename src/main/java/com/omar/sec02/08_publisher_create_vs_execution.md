
# Publisher Creation vs. Execution in Project Reactor

## 🎯 Objective
To clarify a common point of confusion regarding **lazy evaluation** (delayed execution) in Project Reactor when using `Supplier`, `Callable`, or similar constructs.

---

## ❓ The Common Misconception
Students often ask: *"If Reactor delays execution, why does my log statement inside the method print immediately, even when I don't call `subscribe()`?"*

**Example of the confusion:**
```java
private static Mono<String> getFirstname() {
    log.info("executing getFirstname method..."); // <-- This prints immediately!
    return Mono.fromSupplier(() -> {
        log.info("generating name..."); 
        return "John";
    });
}
```
If you call `getFirstname()` without subscribing, the first log statement still prints. This makes it seem like execution is *not* delayed. **This is a misunderstanding of what is actually being delayed.**

---

## 💡 Key Concept: Creation vs. Execution
In Project Reactor, **creating a publisher** and **executing a publisher** are two completely different phases:

1. **Publisher Creation (Lightweight & Immediate):**
    - When you call a method like `getFirstname()`, you are merely *building* or *assembling* the publisher (the `Mono`).
    - Any standard Java code outside the reactive operators (like the first `log.info`) runs immediately because it is just standard method execution.
    - Creating a publisher is a very lightweight operation and is not a problem.

2. **Publisher Execution (Delayed & Lazy):**
    - The actual business logic (especially time-consuming operations) is placed *inside* the `Supplier` lambda.
    - This logic is **strictly delayed** and will **only** run when a terminal operation like `.subscribe()` is invoked.


---

## 🚀 Key Takeaways & Best Practices

- **Reactor is Lazy:** Nothing happens in a reactive pipeline until you `subscribe()`.
- **Keep Creation Lightweight:** The method that returns a `Mono` or `Flux` should only *assemble* the pipeline. Avoid putting heavy business logic directly in the method body outside of reactive operators.

