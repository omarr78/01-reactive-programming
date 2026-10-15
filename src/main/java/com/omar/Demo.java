package com.omar;

/*
   1. publisher does not produce data unless subscriber requests for it.
   2. publisher will produce only <= subscriber requested items. publisher can also produce 0 items!
   3. subscriber can cancel the subscription. producer should stop at that moment as subscriber is no longer
    interested in consuming the data
   4. producer can send the error signal
*/

/*
Implementation notes

Variable type = SubscriberImpl -> can call every method in the class, e.g. getSubscription()
var subscriber = new SubscriberImpl(); // inferred as SubscriberImpl subscriber = new SubscriberImpl(); // same thing, explicit type
Subscription subscription = subscriber.getSubscription(); // Works because the variable type is SubscriberImpl

var subscriber = (Subscriber<String>) new SubscriberImpl();
Variable type = Subscriber<String> -> can only call interface methods(onSubscribe, onNext, onError, onComplete).
Useful when you want to swap implementations.
*/


import com.omar.publisher.PublisherImpl;
import com.omar.subscriber.SubscriberImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class Demo {
    private static final Logger log = LoggerFactory.getLogger(Demo.class);

    public static void main(String[] args) throws InterruptedException {
        log.info("--- Demo 1: Normal Incremental Requests until Completion ---");
        demo1_NormalFlow();

        log.info("--- Demo 2: Cancellation Mid-Stream ---");
        demo2_Cancellation();

        log.info("--- Demo 3: Exceeding Max Items Limit (Triggers onError) ---");
        demo3_ExceedLimitError();
    }

    private static void demo1_NormalFlow() throws InterruptedException {
        var publisher = new PublisherImpl();
        var subscriber = new SubscriberImpl();
        publisher.subscribe(subscriber);

        // Requests 3 items (counter = 3)
        subscriber.getSubscription().request(3);
        Thread.sleep(Duration.ofSeconds(1));

        // Requests 4 items (counter = 7)
        subscriber.getSubscription().request(4);
        Thread.sleep(Duration.ofSeconds(1));

        // Requests 5 items, but will cap at MAX_ITEMS (10), complete, and ignore the remaining 2
        subscriber.getSubscription().request(5);
        Thread.sleep(Duration.ofSeconds(1));
    }

    private static void demo2_Cancellation() throws InterruptedException {
        var publisher = new PublisherImpl();
        var subscriber = new SubscriberImpl();
        publisher.subscribe(subscriber);

        // Request 3 items
        subscriber.getSubscription().request(3);
        Thread.sleep(Duration.ofSeconds(1));

        // Cancel subscription
        subscriber.getSubscription().cancel();
        Thread.sleep(Duration.ofSeconds(1));

        // Subsequent request should be ignored because isCancelled is true
        subscriber.getSubscription().request(3);
        Thread.sleep(Duration.ofSeconds(1));
    }

    private static void demo3_ExceedLimitError() throws InterruptedException {
        var publisher = new PublisherImpl();
        var subscriber = new SubscriberImpl();
        publisher.subscribe(subscriber);

        // Requesting 11 items triggers the guard condition (> 10) and calls onError()
        subscriber.getSubscription().request(11);
        Thread.sleep(Duration.ofSeconds(1));

        // Further requests will be ignored because the subscription is canceled on error
        subscriber.getSubscription().request(3);
        Thread.sleep(Duration.ofSeconds(1));
    }
}
