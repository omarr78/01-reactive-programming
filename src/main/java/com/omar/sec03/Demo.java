package com.omar.sec03;

import com.omar.common.Util;
import com.omar.sec03.client.ExternalServiceClient;

public class Demo {
    static void main(String[] args) {
        var client = new ExternalServiceClient();

        // Send 100 concurrent requests using a loop
        for (int i = 0; i < 100; i++) {
            client.getProduct(i)
                    .subscribe(Util.subscriber());

            // NOTE: Using .block() here is commented out.
            // If uncommented, it would force the thread to wait for each response
            // sequentially, taking ~100 seconds and defeating the purpose of non-blocking I/O.
            //            client.getProduct(i)
            //                    .block();

        }

        // Sleep the main thread for 2 seconds to keep the application alive.
        // Because the I/O is non-blocking, the main thread would otherwise
        // exit immediately before the background thread can receive the responses.
        // (The external service has a 1-second artificial delay).
        Util.sleepSeconds(2);
    }
}
