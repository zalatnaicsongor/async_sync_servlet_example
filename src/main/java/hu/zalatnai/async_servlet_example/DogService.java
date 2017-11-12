package hu.zalatnai.async_servlet_example;

import io.vavr.control.Try;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
public class DogService {
    private final DogClient dogClient;

    @Autowired
    DogService(DogClient dogClient) {
        this.dogClient = dogClient;
    }

    public Try<Dog> getDog() {
        try {
            return dogClient.fetchDog().get();
        } catch (InterruptedException | ExecutionException e) {
            return Try.failure(e);
        }
    }
}
