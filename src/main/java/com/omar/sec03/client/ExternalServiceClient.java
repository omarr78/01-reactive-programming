package com.omar.sec03.client;

import com.omar.common.AbstractHttpClient;
import reactor.core.publisher.Mono;

public class ExternalServiceClient extends AbstractHttpClient {

    // Returns a Mono (0 or 1 item) containing the product name as a String
    public Mono<String> getProduct(int productId) {
        return this.httpClient.get()
                .uri("/demo01/product/" + productId)
                // Get the response body as a Flux of ByteBuf (raw network bytes)
                .responseContent()
                // Convert the stream of ByteBuf into a stream (Flux) of Strings
                .asString()
                // Extract only the FIRST emitted string and wrap it in a Mono.
                .next();
    }
}
