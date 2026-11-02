package com.omar.assignment;

import reactor.core.publisher.Mono;

import java.io.FileNotFoundException;

public interface FileService {
    Mono<String> read(String fileName);
    Mono<Void> write(String fileName, String content);
    Mono<Void> delete(String fileName);
}
