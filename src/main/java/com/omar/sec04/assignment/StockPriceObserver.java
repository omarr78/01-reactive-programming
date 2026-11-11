package com.omar.sec04.assignment;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StockPriceObserver implements Subscriber<Integer> {

    private static final Logger log = LoggerFactory.getLogger(StockPriceObserver.class);
    private static final int MIN_PRICE = 90;
    private static final int MAX_PRICE = 110;
    private static final int INITIAL_PRICE = 1000;
    private int quantity = 0;
    private int balance = INITIAL_PRICE;
    private Subscription subscription;

    @Override
    public void onSubscribe(Subscription subscription) {
        subscription.request(Long.MAX_VALUE);
        this.subscription = subscription;
    }

    @Override
    public void onNext(Integer price) {
        log.debug("onNext price {}", price);
        if(price < MIN_PRICE && balance > price) {
            quantity++;
            balance -= price;
            log.info("bought a stock at {}. total quantity: {}, remaining balance: {}", price, quantity, balance);
        }
        else if(price > MAX_PRICE && quantity > 0) {
            log.info("selling {} quantity at price {}", quantity, price);
            balance += price * quantity;
            quantity = 0;
            subscription.cancel();
            log.info("profit: {}", (balance - INITIAL_PRICE));
        }
    }

    @Override
    public void onError(Throwable t) {
        log.error(t.getMessage(), t);
    }

    @Override
    public void onComplete() {
        log.info("Stock Price Observer has been completed");
    }
}
