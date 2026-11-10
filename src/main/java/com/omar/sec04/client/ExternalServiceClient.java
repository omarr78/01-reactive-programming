package com.omar.sec04.client;

import com.omar.common.AbstractHttpClient;
import reactor.core.publisher.Flux;

public class ExternalServiceClient extends AbstractHttpClient {

    public Flux<String> getNames() {
        return this.httpClient.get()
                .uri("/demo02/name/stream")
                // Get the response body as a Flux of ByteBuf (raw network bytes)
                .responseContent()
                // Convert the stream of ByteBuf into a stream (Flux) of Strings
                .asString();
    }
}
