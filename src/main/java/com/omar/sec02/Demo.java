package com.omar.sec02;

import com.omar.common.Util;
import com.omar.sec01.subscriber.SubscriberImpl;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class Demo {
    private static final Logger log = LoggerFactory.getLogger(Demo.class);

    static void main(String[] args) {
        // Demonstrate that Java Streams are lazy.
        // lazyBehavior();

        // Demonstrate how Mono.just() works.
        // monoJust();

        // Demonstrate different ways to subscribe to a Mono.
        // monoSubscribe();

        // Demonstrate how Mono behaves with a value, no value, or an error.
        // emptyMonoOrError();

        // Demonstrate the difference between Mono.just() and Mono.fromSupplier().
        // monoFromSupplier();

        // Demonstrate how Mono.fromCallable() delays execution and handles exceptions from the calculation.
        // monoFromCallable();

        // Demonstrate how Mono.fromRunnable() works
        // monoFromRunnable();

        // Demonstrate how Mono.fromFuture() works
        // monoFromFuture();

        // Demonstrate the difference between publisher creation vs execution
        // publisherCreateVsExecution();

        // Demonstrate how Mono.defer() works
        monoDefer();
    }

    private static void lazyBehavior() {
        log.info("result from the first peek");

        // Create a stream containing the number 1.
        // peek() defines a logging action, but does not execute it yet.
        // There is no terminal operation, so the pipeline is not executed.
        Stream.of(1)
                .peek(i -> log.info("received: {}", i));

        log.info("result from the second peek");

        // Create another stream containing the number 1.
        // peek() defines the logging action.
        // toList() is a terminal operation that triggers stream processing.
        // The element passes through peek(), and its value is logged.
        Stream.of(1)
                .peek(i -> log.info("received: {}", i))
                .toList();
    }

    private static void monoJust() {
        // Create a Mono that can emit the value "Omar".
        var mono = Mono.just("Omar");

        // Print the publisher object, not its actual value.
        log.info("Mono created: {}", mono);

        // Create our custom subscriber.
        var subscriber = new SubscriberImpl();

        // Subscribe to the Mono.
        // The subscriber receives a subscription.
        mono.subscribe(subscriber);

        // Request up to 10 items from the publisher.
        // Mono emits only one item: "Omar".
        subscriber.getSubscription().request(10);

        // Request again, but the Mono has already completed.
        // No additional item will be emitted.
        subscriber.getSubscription().request(10);

        // Cancel the subscription.
        // This has no effect because the Mono has completed.
        subscriber.getSubscription().cancel();

        // Wrap an existing String in a Mono and pass it
        // to a method that accepts a Publisher<String>.
        save(Mono.just("Ahmed"));
    }

    public static void monoSubscribe() {
        /**
         * Demonstrates different ways to subscribe to a Mono publisher.
         *
         * Covers:
         * 1. Subscribing with an onNext handler only.
         * 2. Handling values, errors, and completion with manual demand.
         * 3. Cancelling a subscription before receiving values.
         * 4. Handling an error caused by a map transformation.
         */

        // Case 1: Subscribe with only an onNext handler.
        // Reactor automatically requests data on our behalf.
        var mono1 = Mono.just(1);

        mono1.subscribe(
                i -> log.info("[Mono 1] received: {}", i)
        );


        // Mono 2: Handle all signals and explicitly request data.
        // request(10) requests up to 10 items; Mono emits at most one.
        var mono2 = Mono.just(1);

        mono2.subscribe(
                i -> log.info("[Mono 2] received: {}", i),
                err -> log.error("[Mono 2] error: {}", err.getMessage(), err),
                () -> log.info("[Mono 2] completed"),
                subscription -> subscription.request(10)
        );


        // Mono 3: Cancel the subscription immediately.
        // No value or completion signal is expected.
        var mono3 = Mono.just(1);

        mono3.subscribe(
                i -> log.info("[Mono 3] received: {}", i),
                err -> log.error("[Mono 3] error: {}", err.getMessage(), err),
                () -> log.info("[Mono 3] completed"),
                subscription -> subscription.cancel()
        );


        // Mono 4: Simulate an error during value transformation.
        // Dividing an integer by zero throws an ArithmeticException.
        // Request one item so the transformation can be evaluated.
        var mono4 = Mono.just(1)
                .map(i -> i / 0);

        mono4.subscribe(
                i -> log.info("[Mono 4] received: {}", i),
                err -> log.error("[Mono 4] error: {}", err.getMessage(), err),
                () -> log.info("[Mono 4] completed"),
                subscription -> subscription.request(1)
        );
    }

    public static void emptyMonoOrError() {

        // Case 1: Emit a username, then complete.
        getUsername(1).subscribe(Util.subscriber("subscriber1"));

        // Case 2: Complete without emitting an item.
        getUsername(2).subscribe(Util.subscriber("subscriber2"));

        // Case 3: Emit an error because the user ID is invalid.
        getUsername(3).subscribe(Util.subscriber("subscriber3"));

        // --------------------------------------------------
        // Case 4: Handle only onNext.
        // If an error occurs, no onError handler is provided.
        // Reactor reports the unhandled error, which may produce:
        // "Operator called default onErrorDropped".
        // --------------------------------------------------
        getUsername(4).subscribe(
                s -> log.info("subscriber4: {}", s)
        );

        // --------------------------------------------------
        // Case 5: Handle onNext and onError.
        // The empty error handler ignores errors, so they won't be logged.
        // --------------------------------------------------
        getUsername(5).subscribe(
                s -> log.info("subscriber5: {}", s),
                err -> {}
        );
    }

    public static void monoFromSupplier() {
        var list1 = List.of(1, 2, 3);

        // Calculate the sum immediately, then pass the result to Mono.just().
        // The sum is calculated BEFORE the Mono is created.
        Mono.just(sum(list1));

        // Calculate the sum immediately, then subscribe to the Mono.
        Mono.just(sum(list1))
                .subscribe(Util.subscriber("subscriber1"));

        var list2 = List.of(4, 5, 6);

        // Create a Mono with a Supplier.
        // The sum is NOT calculated at this point.
        Mono.fromSupplier(() -> sum(list2));

        // Subscribe to the Mono.
        // Now the supplier executes and calculates the sum.
        Mono.fromSupplier(() -> sum(list2))
                .subscribe(Util.subscriber("subscriber2"));
    }

    public static void monoFromCallable() {
        var list = List.of(1, 2, 3);

        // Create a Mono from a Callable.
        // The sum is not calculated yet.
        // The calculation starts when a subscriber subscribes.
        // fromCallable() also supports methods that throw checked exceptions.
        Mono.fromCallable(() -> sumThrowsException(list))
                .subscribe(Util.subscriber());
    }

    public static void monoFromRunnable() {
        // Scenario 1: Product ID 1 exists.
        // The supplier will generate a name, and the subscriber will receive it.
        getProductName(1)
                .subscribe(Util.subscriber("subscriber1"));

        // Scenario 2: Product ID 2 does NOT exist.
        // The runnable will trigger the business notification side effect,
        // and the subscriber will receive an empty completion signal.
        getProductName(2)
                .subscribe(Util.subscriber("subscriber2"));
    }

    public static void monoFromFuture() {
        // getName() is called immediately.
        // The future's task can start before subscription.
        Mono.fromFuture(getName())
                .subscribe(Util.subscriber("subscriber1"));

        // Creates another future immediately,
        // but this Mono has no subscriber.
        Mono.fromFuture(getName());

        // The method reference is passed without invoking getName().
        // Reactor (waits) defers the supplier invocation until subscription.
        Mono.fromFuture(Demo::getName)
                .subscribe(Util.subscriber("subscriber2"));

        // Creates a lazy Mono but never subscribes to it.
        // Therefore, this publisher does not invoke getName().
        Mono.fromFuture(Demo::getName);

        // we use this to keep the main thread alive briefly while the asynchronous task has a chance to finish.
        Util.sleepSeconds(1);
    }

    public static void publisherCreateVsExecution() {
        // SCENARIO 1: Only Creation
        // Output: "executing getFirstname method..."
        // Notice: It does NOT wait 3 seconds, and does NOT print "generating name..."
        getFirstname();

        System.out.println("---");

        // SCENARIO 2: Creation + Execution
        // Output:
        // 1. "executing getFirstname method..." (Creation phase)
        // 2. [Waits 3 seconds...]
        // 3. "generating name..." (Execution phase triggered by subscribe)
        // 4. Emits the generated name to the subscriber
        getFirstname()
                .subscribe(Util.subscriber());
    }

    public static void monoDefer() {
        // 1. No Subscriber
        // The createPublisher() method is NEVER called.
        // It exits immediately without spending 1 second.
        Mono.defer(Demo::createPublisher);

        System.out.println("---");

        // 2. With Subscriber
        // A subscriber is attached, so Mono.defer() triggers createPublisher().
        // It spends 1 second creating the publisher, then subscribes and gets the result.
        Mono.defer(Demo::createPublisher)
                .subscribe(Util.subscriber());
    }

    private static Mono<Integer> createPublisher() {
        log.info("Creating publisher");
        var list = List.of(1, 2, 3);
        Util.sleepSeconds(1);
        return Mono.fromSupplier(() -> sumSomeNumbers(list));
    }

    // time-consuming business logic
    private static int sumSomeNumbers(List<Integer> list) {
        log.info("Calculating the sum of {}", list);
        Util.sleepSeconds(3);
        return list.stream().reduce(0, Integer::sum);
    }

    private static Mono<String> getFirstname() {
        // THIS RUNS IMMEDIATELY upon method call (Creation Phase)
        log.info("executing getFirstname method...");

        // THIS IS DELAYED until .subscribe() is called (Execution Phase)
        return Mono.fromSupplier(() -> {
            log.info("generating name...");
            Util.sleepSeconds(3);
            return Util.faker().name().firstName();
        });
    }

    private static CompletableFuture<String> getName() {
        return CompletableFuture.supplyAsync(() -> {
            log.info("generating name");
            return Util.faker().name().firstName();
        });
    }

    private static Mono<String> getProductName(int productId) {
        if(productId == 1){
            // Product exists: Lazily compute and return the product name only when subscribed.
            return Mono.fromSupplier(() -> Util.faker().commerce().productName());
        }
        // Product does NOT exist:
        // 1. Execute the side effect (notify business).
        // 2. Return an empty Mono<String> to satisfy the method's return type.
        // Note: Mono.fromRunnable() returns Mono<Void>, so we chain .then() to
        // convert it to Mono<String> while preserving the lazy execution behavior.
        return Mono.fromRunnable(() -> notifyBusiness(productId))
                .then(Mono.<String>empty());
    }

    private static void notifyBusiness(int productId){
        log.info("notifying business on unavailable product {}", productId);
    }

    private static int sumThrowsException(List<Integer> list) throws Exception {
        // Suppose this is a complex calculation
        log.info("Calculating the sum of {}", list);
        return list.stream().reduce(0, Integer::sum);
    }

    private static int sum(List<Integer> list) {
       // Suppose this is a complex calculation
        log.info("Calculating the sum of {}", list);
        return list.stream().reduce(0, Integer::sum);
    }

    private static Mono<String> getUsername(int userId){
        // Choose what the Mono should return based on the user ID.
        return switch (userId) {

            // User ID 1: Emit the username "omar", then complete.
            case 1 -> Mono.just("omar");

            // User ID 2: Emit no item, but complete successfully.
            case 2 -> Mono.empty();

            // Any other ID: Terminate with an error.
            default -> Mono.error(new RuntimeException("invalid userId"));
        };
    }

    private static void save(Publisher<String> publisher) {
        var subscriber = new SubscriberImpl();

        // Subscribe to the publisher.
        publisher.subscribe(subscriber);

        // Request up to 10 items.
        subscriber.getSubscription().request(10);
    }
}
