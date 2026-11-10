
## Consuming HTTP Streams with Reactor (Flux)

This lecture demonstrates how to consume a continuous stream of messages over HTTP using Project Reactor. Unlike traditional request-response cycles, a streaming endpoint keeps the connection open and pushes data incrementally (e.g., every 500 milliseconds).

---

## Key Concepts

1. **Streaming HTTP Endpoint**: 
   - The target endpoint (`/demo02/name/stream`) generates and pushes random first names continuously.
   - Standard tools like Swagger UI may fail to render continuous streams correctly due to browser limitations, but **Reactor Netty** handles them seamlessly.

2. **Flux vs. Mono**: 
   - Because the endpoint returns *multiple* messages over time, the return type must be `Flux<T>` (not `Mono<T>`, which represents 0 or 1 item).

3. **Non-Blocking Execution**: 
   - Subscribing to a `Flux` is entirely non-blocking. The main thread will exit immediately unless explicitly paused.
   - A utility like `Util.sleepSeconds(8)` is used in demos to keep the JVM alive long enough to observe the emitted items.

4. **Multiple Subscribers**: 
   - You can attach multiple subscribers to the same `Flux`. Each subscriber will independently receive the full stream of messages as they are emitted.

5. **Beyond Strings**: 
   - While this demo uses `Flux<String>` for simplicity, real-world applications stream complex Java objects (e.g., `Flux<Customer>`, `Flux<PurchaseOrder>`). 
   - Frameworks like **Spring WebFlux** automatically handle the necessary JSON deserialization for these objects.

---

## Important Takeaways
- **Reactive programming** allows highly efficient, non-blocking transfer of messages between applications.
- Always ensure the external service is up and running before executing the client.
- The thread handling the subscription remains highly efficient, often processing the entire stream using a single event-loop thread.
