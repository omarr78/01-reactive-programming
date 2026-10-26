
# Reactor Netty HTTP Client & Non-Blocking I/O Lecture Notes

## 📌 Key Topics & Points Discussed

1. **Introduction to Reactor Netty**
    - Reactor Netty is used to send HTTP requests and receive responses.
    - It is more low-level compared to Spring WebFlux (which handles serialization/deserialization automatically). It is used here for educational purposes to understand the underlying mechanics of reactive HTTP clients.

2. **Creating an Abstract HTTP Client (`AbstractHttpClient`)**
    - An abstract class is created to avoid code duplication across multiple demos.
    - It defines a `BASE_URL` (e.g., `http://localhost:7070`).
    - It configures a Reactor Netty `HttpClient`.
    - **Custom `LoopResources`**: By default, Reactor creates one thread per CPU core. The configuration is overridden to use exactly **one worker thread** (as a daemon thread) with a custom name prefix. This is done intentionally to demonstrate the power of non-blocking I/O using a single thread.
    - `LoopResources` acts as a manager/helper to configure the underlying event loop group.

3. **Building the External Service Client (`ExternalServiceClient`)**
    - Extends `AbstractHttpClient` to inherit the configured `HttpClient`.
    - The `getProduct(int productId)` method returns a `Mono<String>`.
    - It constructs a GET request to `/demo01/product/{productId}`.
    - `responseContent()` returns a `Flux<ByteBuf>` (a stream of raw byte buffers, as everything over the network is fundamentally bytes).
    - `.asString()` converts the byte stream into a `Flux<String>`.
    - `.next()` is used to convert the `Flux` into a `Mono` by taking only the *first* emitted value, since we expect exactly one product name per request.

4. **Demonstrating Non-Blocking I/O**
    - A single request in a `main` method will cause the program to exit immediately because the main thread does not block and wait for the response.
    - `Util.sleepSeconds(2)` is added to keep the main thread alive long enough to receive the response (the external service has an artificial 1-second delay).
    - When scaled to a `for` loop (e.g., 5, 50, or 100 requests), all requests are dispatched almost simultaneously.
    - Responses may arrive **out of order** due to network latency and remote server processing times. This is normal and not a bug.
    - **The Big Deal**: A *single thread* successfully handles 100 concurrent requests in ~1 second. In traditional blocking I/O, this would either take 100 seconds (sequential) or require 100 threads (concurrent).

5. **How the Event Loop Works Behind the Scenes**
    - **Outbound Queue**: Tasks (requests) are added to a queue. The single event loop thread picks them up and sends them to the remote server *without waiting* for the response.
    - **Inbound Queue**: When the OS receives a response, it notifies the thread. The response is added to an inbound queue and processed as it arrives. Order is not guaranteed.

6. **Why We Should Avoid `.block()`**
    - Calling `.block()` on a `Mono` or `Flux` forces the current thread to wait synchronously for the result.
    - This turns non-blocking, concurrent code into slow, sequential blocking code (defeating the purpose of reactive programming).
    - It is acceptable in unit tests, but should be strictly avoided in production application code (`src/main/java`).

7. **Why Use Reactor Netty Instead of Plain Java?**
    - Writing plain Java non-blocking I/O (using `AsynchronousSocketChannel`) requires managing complex, low-level callbacks for connecting, building raw byte requests, sending, and reading responses.
    - Reactor Netty abstracts this complexity, handling the low-level callbacks and providing a clean, reactive API (`subscribe`, `onNext`, `onComplete`, `onError`).

