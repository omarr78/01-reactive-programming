package com.omar.assignment;

import com.omar.sec02.Demo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class FileServiceImpl implements FileService {
    private static final Logger log = LoggerFactory.getLogger(Demo.class);

    @Override
    public Mono<String> read(String fileName) {
        return Mono.fromCallable(() -> Files.readString(Path.of(fileName)));
    }

    @Override
    public Mono<Void> write(String fileName, String content) {
        return Mono.fromRunnable(() -> this.writeFile(fileName, content));
    }

    @Override
    public Mono<Void> delete(String fileName) {
        return Mono.fromRunnable(() -> this.deleteFile(fileName));
    }

    private void writeFile(String fileName, String content) {
        try {
            Files.writeString(Path.of(fileName), content);
            log.info("written on file {}, content: {}", fileName, content);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void deleteFile(String fileName) {
        try {
            Files.delete(Path.of(fileName));
            log.info("file deleted {}", fileName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
