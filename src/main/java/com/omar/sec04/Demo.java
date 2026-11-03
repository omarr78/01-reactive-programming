package com.omar.sec04;

import com.omar.common.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

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
}
