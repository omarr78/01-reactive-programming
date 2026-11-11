package com.omar.sec04;

import com.omar.common.Util;
import com.omar.sec01.subscriber.SubscriberImpl;
import com.omar.sec04.client.ExternalServiceClient;
import com.omar.sec04.helper.NameGenerator;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;

public class Demo {
    private static final Logger log = LoggerFactory.getLogger(com.omar.sec02.Demo.class);

    public static void main(String[] args) {

        /**
         * Demonstrates Flux.just()
         */
        // fluxJust();

        /**
         * Demonstrates that multiple subscribers can attach to the same base Flux,
         * and each can apply its own independent filtering and mapping logic.
         */
        // fluxMultipleSubscribers();

        /**
         * Demonstrates how to create a Flux from standard Java collections and arrays.
         */
        // fluxFromIterableOrArray();

        /**
         * Demonstrates the correct way to create a Flux from a Java Stream
         * to support multiple subscribers without throwing an IllegalStateException.
         */
        // fluxFromStream();

        // fluxRange();

        // logOperator();

        // FluxVsList();

        // nonBlockingIoStream();

        // fluxInterval();

        // fluxEmptyError();

        // fluxDefer();

        // fluxToMono();

        monoToFlux();
    }

    public static void fluxJust() {
        // Create a Flux publisher that emits a single string value
        var flux = Flux.just("Omar");

        // Print the publisher object reference, not the actual emitted value
        log.info("Flux created: {}", flux);

        // Subscribe to the Flux to trigger the data flow.
        // The subscriber receives the item, followed automatically by the onComplete signal.
        flux.subscribe(Util.subscriber());

        log.info("============================");

        // Create a Flux that emits multiple arbitrary items (integers and strings)
        Flux.just(1, 2, 3, "omar")
                // Subscribe to receive all items sequentially, followed by the completion signal
                .subscribe(Util.subscriber());
    }

    public static void fluxMultipleSubscribers() {
        // Base Flux emitting numbers 1 through 6
        var flux = Flux.just(1, 2, 3, 4, 5, 6);

        // Subscriber 1: Receives all items (1, 2, 3, 4, 5, 6) and onComplete
        flux.subscribe(Util.subscriber("sub1"));

        log.info("============================");

        // Subscriber 2: Filters for items > 7.
        // Since none match, it receives ONLY the onComplete signal immediately.
        flux
                .filter(i -> i > 7)
                .subscribe(Util.subscriber("sub2"));

        log.info("============================");

        // Subscriber 3: Filters for even numbers, then appends "a" to each.
        // Receives: "2a", "4a", "6a", followed by onComplete.
        flux
                .filter(i -> i % 2 == 0)
                .map(i -> i + "a")
                .subscribe(Util.subscriber("sub3"));
    }

    public static void fluxFromIterableOrArray() {
        // 1. From an Iterable (e.g., List or Set)
        var list = List.of("a", "b", "c");
        Flux.fromIterable(list)
                .subscribe(Util.subscriber("Iterable Subscriber"));

        log.info("============================");

        // 2. From an Array
        Integer[] arr = {1, 2, 3, 4, 5, 6};
        Flux.fromArray(arr)
                .subscribe(Util.subscriber("Array Subscriber"));
    }

    public static void fluxFromStream() {
        var list = List.of(1, 2, 3, 4);

        // BAD APPROACH (Commented out):
        // var stream = list.stream();
        // var flux = Flux.fromStream(stream);
        // -> This fails on the second subscriber because Java Streams are one-time use.

        // GOOD APPROACH:
        // Pass a Supplier (method reference) instead of a pre-created Stream object.
        // This ensures a fresh Stream is generated for EACH subscriber.
        var flux = Flux.fromStream(list::stream);

        // Both subscribers will successfully receive 1, 2, 3, 4 and onComplete
        flux.subscribe(Util.subscriber("Stream Sub 1"));
        log.info("============================");
        flux.subscribe(Util.subscriber("Stream Sub 2"));
    }

    public static void fluxRange() {
        // assignment - generate 10 random first names
        Flux.range(1, 10) // start the start number and the end with (start + end) - 1
                .map(i -> i + ". " + Util.faker().name().firstName())
                .subscribe(Util.subscriber());
    }

    public static void logOperator() {
        // ========================================================================
        // CASE 1: Basic Log Operator (Unbounded Request)
        // ========================================================================
        // The log() operator acts as a middleman, printing all reactive signals.
        // It sees the raw data BEFORE the map() transformation happens.
        log.info("====== Case 1: Basic Log ======");

        Flux.range(1, 5)
                .log() // Logs: onSubscribe, request(unbounded), onNext(1 to 5), onComplete
                .map(i -> i + ". " + Util.faker().name().firstName())
                .subscribe(Util.subscriber()); // Util.subscriber() requests unbounded by default


        // ========================================================================
        // CASE 2: Bounded Request (Requesting only 3 items)
        // ========================================================================
        // If the subscriber only requests 3 items, the producer only emits 3.
        // Notice: onComplete is NOT called because the producer still has items left.
        log.info("\n====== Case 2: Bounded Request (3 items) ======");

        Flux.range(1, 5)
                .log() // Logs: request(3), then only 3 onNext signals, NO onComplete
                .map(i -> i + ". " + Util.faker().name().firstName())
                .subscribe(new BaseSubscriber<>() {
                    @Override
                    protected void hookOnSubscribe(Subscription subscription) {
                        log.info("Subscribed! Requesting exactly 3 items...");
                        request(3); // Explicitly request only 3 items instead of unbounded
                    }
                    @Override
                    protected void hookOnNext(String value) {
                        log.info("Received: {}", value);
                    }
                });

        // ========================================================================
        // CASE 3: Multiple Log Operators (Observing the Transformation)
        // ========================================================================
        // A single log() before map() only sees raw numbers (1, 2, 3...).
        // To see the transformed names, we must add a SECOND log() AFTER map().
        log.info("\n====== Case 3: Multiple Logs ======");

        Flux.range(1, 5)
                .log() // 1st log: sees raw integers (onNext(1), onNext(2)...)
                .map(i -> i + ". " + Util.faker().name().firstName())
                .log() // 2nd log: sees transformed strings (onNext("1. John")...)
                .subscribe(Util.subscriber());

        // ========================================================================
        // CASE 4: Named Log Operators (Best Practice for Debugging)
        // ========================================================================
        // When chaining multiple logs, give them string tags.
        // This makes the console output much easier to read and trace.
        log.info("\n====== Case 4: Named Logs ======");

        Flux.range(1, 5)
                .log("1. Range-to-Map")       // Tagged log for the first segment
                .map(i -> i + ". " + Util.faker().name().firstName())
                .log("2. Map-to-Subscriber")  // Tagged log for the second segment
                .subscribe(Util.subscriber());
    }

    public static void FluxVsList() {
        // ==========================================
        // 1. TRADITIONAL APPROACH (List)
        // ==========================================
        // Blocks the thread for 10 seconds. Nothing is printed until the very end.
        log.info("--- Traditional List Approach ---");
        var list = NameGenerator.getNamesList(10);
        System.out.println(list);

        // ==========================================
        // 2. REACTIVE APPROACH (Flux) - Basic Subscribe
        // ==========================================
        // Prints names one by one every second. Highly responsive.
        log.info("--- Reactive Flux Approach (Basic) ---");
        NameGenerator.getNamesFlux(10)
                .subscribe(Util.subscriber("Basic"));

        log.info("--- REACTIVE APPROACH (Flux) - WITH subscribeOn ---");
        // ==========================================
        // 2. REACTIVE APPROACH (Flux) - WITH subscribeOn
        // ==========================================
        // subscribeOn moves the execution to a background thread!
        NameGenerator.getNamesFlux(10)
                .subscribeOn(Schedulers.boundedElastic()) // <--- MAGIC HAPPENS HERE
                .subscribe(Util.subscriber("WITH subscribeOn"));

        // ==========================================
        // 3. REACTIVE APPROACH (Flux) - Early Cancellation
        // ==========================================
        // Demonstrates backpressure and the ability to cancel early.
        log.info("--- Reactive Flux Approach (Early Cancellation) ---");
        var subscriber = new SubscriberImpl();
        NameGenerator.getNamesFlux(10)
                .subscribe(subscriber);

        // Request only 3 names initially
        subscriber.getSubscription().request(3);

        // Imagine we see the names and are satisfied with the results.
        // We can cancel the subscription immediately!
        // The producer will stop generating the remaining 7 names.
        subscriber.getSubscription().cancel();

        // Any further requests will be ignored because the subscription is canceled.
        subscriber.getSubscription().request(3);

        // keeps your main thread alive for 10 seconds, giving the boundedElastic thread enough time to finish processing the Flux.
        Util.sleepSeconds(10);
    }

    public static void nonBlockingIoStream() {
        var client = new ExternalServiceClient();

        // Subscriber 1 independently consumes the stream
        client.getNames()
                .subscribe(Util.subscriber("sub1"));

        // Subscriber 2 independently consumes the stream
        client.getNames()
                .subscribe(Util.subscriber("sub2"));

        // Block the main thread for 8 seconds to allow the async stream to emit and process
        // (The external service emits ~10 messages every 500ms)
        Util.sleepSeconds(8);
    }

    public static void fluxInterval() {
        // Emits an infinite sequence of numbers (0, 1, 2...) every 500ms
        Flux.interval(Duration.ofMillis(500))
                // Transforms each number into a random first name
                .map(i -> Util.faker().name().firstName())
                .subscribe(Util.subscriber());

        // Keeps the main thread alive for 5s so the background thread has time to run
        Util.sleepSeconds(5);
    }

    public static void fluxEmptyError() {
        Flux.empty()
                .subscribe(Util.subscriber());

        log.info("============================");

        Flux.error(new RuntimeException("oops"))
                .subscribe(Util.subscriber());
    }

    public static void fluxDefer() {
        log.info("🔴 --- WITHOUT Flux.defer (The Photograph) ---");
        // Evaluated IMMEDIATELY when the code is assembled
        Flux<Long> fluxWithoutDefer = Flux.just(System.currentTimeMillis());

        Util.sleepSeconds(2); // Wait 2 seconds before anyone listens
        fluxWithoutDefer.subscribe(Util.subscriber());

        Util.sleepSeconds(1); // Wait 1 more second
        fluxWithoutDefer.subscribe(Util.subscriber());


        log.info("🟢 --- WITH Flux.defer (The Live Camera) ---");
        // Creation is DELAYED until the exact moment of subscription
        Flux<Long> fluxWithDefer = Flux.defer(() -> Flux.just(System.currentTimeMillis()));

        Util.sleepSeconds(2); // Wait 2 seconds before anyone listens
        fluxWithDefer.subscribe(Util.subscriber());

        Util.sleepSeconds(1); // Wait 1 more second
        fluxWithDefer.subscribe(Util.subscriber());
    }

    public static void fluxToMono() {
        // Create a Flux that emits integers from 1 to 10
        var flux = Flux.range(1, 10);

        // Convert the Flux into a Mono (takes only the first emitted element)
        Mono<Integer> monoFrom = Mono.from(flux);
        monoFrom.subscribe(Util.subscriber("[Mono.from]"));

        // Alternative shortcut to achieve the exact same thing (emits the first item as a Mono)
        Mono<Integer> mono = flux.next();
        mono.subscribe(Util.subscriber("[flux.next]"));
    }

    public static void monoToFlux() {
        // Convert individual Monos into Fluxes and process/save them one by one
        var mono1 = getUsername(1);
        save(Flux.from(mono1));

        var mono2 = getUsername(2);
        save(Flux.from(mono2));

        var mono3 = getUsername(3);
        save(Flux.from(mono3)); // Note: This will trigger an error signal
    }

    private static Mono<String> getUsername(int userId) {
        // Return a username, an empty result, or an error based on the ID
        return switch (userId) {
            case 1 -> Mono.just("Omar");
            case 2 -> Mono.empty();
            default -> Mono.error(new RuntimeException("Invalid input"));
        };
    }

    private static void save(Flux<String> flux) {
        // Subscribe to the Flux to trigger execution
        flux.subscribe(Util.subscriber());
    }
}
