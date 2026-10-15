package com.omar.publisher;

import com.github.javafaker.Faker;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubscriptionImpl implements Subscription {
    private static final Logger log = LoggerFactory.getLogger(SubscriptionImpl.class);
    private static final int MAX_ITEMS = 10;
    private final Subscriber<? super String> subscriber;
    private final Faker faker;
    private boolean isCancelled;
    private int counter = 0;

    public SubscriptionImpl(Subscriber<? super String> subscriber) {
        this.subscriber = subscriber;
        this.faker = Faker.instance();
        isCancelled = false;
    }

    @Override
    public void request(long requested) {
        if (isCancelled) {
            log.warn("request({}) ignored: subscription is already cancelled or completed", requested);
            return;
        }
        log.info("subscriber has requested {} items", requested);
        if(requested > MAX_ITEMS) {
            this.subscriber.onError(new RuntimeException("Requested requested more than " + MAX_ITEMS + " items"));
            this.isCancelled = true;
            return;
        }
        for (int i = 0; i < requested && counter < MAX_ITEMS; i++) {
            counter++;
            this.subscriber.onNext(faker.internet().emailAddress());
        }
        if(counter == MAX_ITEMS) {
            log.info("no more data to produce");
            this.subscriber.onComplete();
            this.isCancelled = true;
        }
    }

    @Override
    public void cancel() {
        log.info("subscriber has cancelled");
        this.isCancelled = true;
    }
}
