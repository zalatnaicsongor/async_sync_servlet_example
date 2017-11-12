package hu.zalatnai.async_servlet_example;

import io.vavr.control.Try;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class AsyncCatService {
    private final CatClient catClient;

    @Autowired
    AsyncCatService(CatClient catClient) {
        this.catClient = catClient;
    }

    public CompletableFuture<Try<Cat>> getCat() {
        return catClient.fetchCat();
    }
}
