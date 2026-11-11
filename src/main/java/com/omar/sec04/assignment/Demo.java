package com.omar.sec04.assignment;

import com.omar.common.Util;
import com.omar.sec04.client.ExternalServiceClient;

public class Demo {
    public static void main(String[] args) {

        var client = new ExternalServiceClient();
        var subscriber = new StockPriceObserver();

        client.getStockPrices()
                .subscribe(subscriber);

        Util.sleepSeconds(25);
    }
}
