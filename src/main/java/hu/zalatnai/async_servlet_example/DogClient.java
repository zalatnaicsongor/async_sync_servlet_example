package hu.zalatnai.async_servlet_example;

import io.vavr.control.Try;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
public class DogClient {
    private final Executor executor;

    @Autowired
    DogClient(Executor executor) {
        this.executor = executor;
    }

    public CompletableFuture<Try<Dog>> fetchDog() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                return Try.failure(e);
            }
            if (new Random().nextInt(10) > 8) {
                return Try.failure(new RuntimeException("sad trombone - dog"));
            }
            return Try.success(new Dog("Dog"));
        }, executor);
    }
}
