package hu.zalatnai.async_servlet_example;

import io.vavr.control.Try;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
public class CatService {
    private final CatClient catClient;

    @Autowired
    CatService(CatClient catClient) {
        this.catClient = catClient;
    }

    public Try<Cat> getCat() {
        try {
            return catClient.fetchCat().get();
        } catch (InterruptedException | ExecutionException e) {
            return Try.failure(e);
        }
    }
}
