
# Flux vs List (Reactive vs Traditional Programming)

## Overview
In this lecture, we compare two different programming styles for implementing a business requirement: **Traditional (List)** vs **Reactive (Flux)**. The business requirement is to generate a specific number of random first names, where each name generation is a time-consuming process (simulated as taking 1 second per name).

## Key Concepts & Takeaways

### 1. Traditional Approach (List) - "All or Nothing"
* **Implementation:** Uses Java's `IntStream` to generate a `List<String>`.
* **Behavior:** It is **blocking**. The main thread waits until *all* names are generated before returning the list. 
* **Drawback:** If you request 10 names, you must wait 10 seconds. You get no feedback during the wait, and you cannot cancel the operation early if you find what you need.

### 2. Reactive Approach (Flux) - "Streaming & Responsive"
* **Implementation:** Uses Project Reactor's `Flux<String>` to create a reactive stream.
* **Behavior:** It is **non-blocking**. The producer pushes data to the subscriber *as soon as it is generated*. 
* **Advantage 1 (Responsiveness):** The subscriber receives names one by one every second, providing immediate visual feedback.
* **Advantage 2 (Early Cancellation/Backpressure):** The subscriber can request a specific number of items (e.g., `request(3)`). If the subscriber is satisfied with the results, they can call `cancel()` to stop the producer immediately, saving time and resources.

---

## Summary Comparison Table

| Feature | Traditional (`List`) | Reactive (`Flux`) |
| :--- | :--- | :--- |
| **Data Delivery** | All at once (after full processing) | Streamed one-by-one as it's processed |
| **Thread Blocking** | Yes (Blocks until complete) | No (Non-blocking) |
| **Visibility** | None (Blank screen while processing) | High (Immediate feedback) |
| **Early Cancellation**| Not possible (All or nothing) | Possible (Can cancel via Subscription) |
