# `Mono.just()` vs `Mono.fromSupplier()`

## 1. What are we trying to do?

Imagine we have a list of integers:

```
List<Integer> numbers = List.of(1, 2, 3);
```

We want to calculate the sum of these numbers and send the result to a subscriber.

There are two ways to create a `Mono` for this operation: `Mono.just()` and `Mono.fromSupplier()`.

## 2. Using `Mono.just()`

Here is the first approach:

```

public static void monoFromSupplier() {

    // Create a list containing three integers.
    var list1 = List.of(1, 2, 3);

    // Calculate the sum immediately, then pass the result to Mono.just().
    // The sum is calculated BEFORE the Mono is created.
    Mono.just(sum(list1));

    // Calculate the sum immediately, then subscribe to the Mono.
    Mono.just(sum(list1))
            .subscribe(Util.subscriber("subscriber1"));
}

```

### What happens step by step?

### 1.Create the list

`list1 = [1, 2, 3]`

### 2.Calculate the sum immediately

`sum(list1)` returns `6`.

### 3.Create the Mono

`Mono.just(6)` creates a publisher containing the already calculated value.

### 4.Subscribe

The subscriber receives `6`.

### What is the problem?

Look at this statement:

```
Mono.just(sum(list1));
```

Even though we don't subscribe to this `Mono`, Java evaluates `sum(list1)` before calling `Mono.just()`.

That means the sum is calculated even when nobody is interested in the result.

For a simple sum, the wasted work is negligible. But imagine that `sum()` represents an expensive operation, such as processing a large file or performing a complex calculation.

We might waste CPU time doing work that nobody needs.

Important: `Mono.just()` does not execute the `sum()` method itself. Java evaluates the argument first, and then passes its result to `Mono.just()`.

## 3. Using `Mono.fromSupplier()`

To delay the calculation until subscription, we can use `Mono.fromSupplier()`.

```

public static void monoFromSupplier() {

    // Create a list containing three integers.
    var list2 = List.of(4, 5, 6);

    // Create a Mono with a Supplier.
    // The sum is NOT calculated at this point.
    Mono.fromSupplier(() -> sum(list2));

    // Subscribe to the Mono.
    // Now the supplier executes and calculates the sum.
    Mono.fromSupplier(() -> sum(list2))
            .subscribe(Util.subscriber("subscriber2"));
}

```

### What does `() -> sum(list2)` mean?

This is a Java lambda expression representing a `Supplier`.

A `Supplier` is an object that provides a value when its `get()` method is called.

For example:

```
Supplier<Integer> supplier = () -> sum(list2);
```

Creating this supplier does not calculate the sum. It describes how to calculate the sum when the value is requested.

### What happens step by step?

### 1. Create the list

`list2 = [4, 5, 6]`

### 2. Create the supplier

`() -> sum(list2)` describes the calculation without running it.

### 3. Create the Mono

`Mono.fromSupplier(...)` prepares a publisher that can execute the supplier when subscribed to.

### 4. Subscribe

The supplier executes, `sum(list2)` returns `15`, and the subscriber receives `15`.

Key idea: `Mono.fromSupplier()` delays execution of the supplied calculation until subscription. It does not automatically move the calculation to another thread.

### Final takeaway

- `Mono.just(value)` — I already have the value.
- `Mono.fromSupplier(() -> calculateValue())` — I have a way to calculate the value; wait until subscription to execute it.