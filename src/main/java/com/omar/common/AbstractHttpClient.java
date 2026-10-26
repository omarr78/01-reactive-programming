package com.omar.common;

import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.LoopResources;

public abstract class AbstractHttpClient {
    private static final String BASE_URL = "http://localhost:7070";
    protected final HttpClient httpClient;

    public AbstractHttpClient() {
        // Configure LoopResources to use exactly 1 worker thread (daemon = true)
        // with the prefix "omar".
        // Default behavior creates 1 thread per CPU core. We override this to
        // prove that a single thread can handle hundreds of concurrent non-blocking requests.
        var loopResources = LoopResources.create("omar", 1, true);


        this.httpClient = HttpClient.create()
                .runOn(loopResources)
                .baseUrl(BASE_URL);
    }
}
