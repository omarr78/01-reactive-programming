# Multiple Subscribers, Iterables, Arrays, and Streams

## 1. Multiple Subscribers to a Single Flux
* A single `Flux` publisher can have multiple subscribers.
* Each subscriber receives its own independent subscription.
* You can chain operators (like `filter` or `map`) **before** calling `subscribe()`. These operators only affect the specific subscriber's data flow, not the original publisher or other subscribers.
* If a filter condition matches no items (e.g., `i > 7` on a stream of 1-6), that specific subscriber will immediately receive an `onComplete` signal without any `onNext` items.

## 2. Creating Flux from Iterable or Array
* **`Flux.fromIterable()`**: The standard way to convert existing in-memory collections (like `List` or `Set`) into a reactive `Flux`.
* **`Flux.fromArray()`**: The standard way to convert primitive or object arrays into a `Flux`.

## 3. Creating Flux from a Java Stream (The "Tricky" Part)
* **The Problem**: Java Streams are **one-time use**. If you create a `Stream` object and pass it to `Flux.fromStream(stream)`, the first subscriber will consume and close it. Any subsequent subscriber will throw an `IllegalStateException: stream has already been operated upon or closed`.
* **The Solution**: Instead of passing a pre-created `Stream` object, pass a **`Supplier<Stream>`** (e.g., using a method reference like `list::stream`). This tells the `Flux` to generate a *brand new* Stream for **every** subscriber, safely supporting multiple subscriptions.

