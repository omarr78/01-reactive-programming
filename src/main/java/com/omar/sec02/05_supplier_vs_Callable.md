# Java & Project Reactor: `Supplier` vs `Callable`

## 1. The Basics: Checked vs. Unchecked Exceptions

| Exception Type | What it is | Example | How to Handle | 
| ----- | ----- | ----- | ----- | 
| **Checked** | Java forces you to handle these at compile-time. | `IOException`, `SQLException` | Must use `try-catch` or `throws` | 
| **Unchecked** | Runtime errors; handling is optional. | `RuntimeException`, `NullPointerException` | Optional | 

## 2. `Supplier<T>` vs `Callable<V>`

Both represent "deferred" operations (they wait to run until asked), but they handle exceptions differently.

| Feature | `Supplier<T>` | `Callable<V>` | 
| ----- | ----- | ----- | 
| **Method** | `T get()` | `V call() throws Exception` | 
| **Checked Exceptions?** | ❌ No (Causes compile error) | ✅ Yes (Allowed) | 
| **Best For** | Simple value generation / Unchecked exceptions | Operations that might throw Checked Exceptions (e.g., I/O) | 

**Simple Examples:**

```java
// 1. Unchecked Exception: Supplier handles simple operations or runtime exceptions
Supplier<String> supplier = () -> {
    // Throws NullPointerException (Unchecked) at runtime
    throw new NullPointerException("Runtime exception occurred!");
};

// 2. Checked Exception: Callable allows throwing checked exceptions directly
Callable<String> callable = () -> {
    // Throws IOException (Checked) - allowed because call() signature includes 'throws Exception'
    return Files.readString(Path.of("data.txt"));
};
```

How They Handle Errors:

* Unchecked Exceptions (Runtime): Both Supplier and Callable will catch the runtime error and safely convert it into a reactive onError() signal.

* Checked Exceptions: Callable catches them and safely converts them into an onError() signal. Supplier fails at compile-time because its method signature does not allow checked exceptions to be thrown.