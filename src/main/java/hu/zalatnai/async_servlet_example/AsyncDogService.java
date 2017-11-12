package hu.zalatnai.async_servlet_example;

import io.vavr.control.Try;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class AsyncDogService {
    private final DogClient dogClient;

    public AsyncDogService(DogClient dogClient) {
        this.dogClient = dogClient;
    }

    public CompletableFuture<Try<Dog>> getDog() {
        return dogClient.fetchDog();
    }
}
