package hu.zalatnai.async_servlet_example;

import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@RestController
public class Controller {
    private static final Logger LOGGER = LoggerFactory.getLogger(Controller.class);

    private final AsyncCatService asyncCatService;
    private final AsyncDogService asyncDogService;
    private final CatService catService;
    private final DogService dogService;
    private final Executor executor;

    @Autowired
    public Controller(
            AsyncCatService asyncCatService,
            AsyncDogService asyncDogService,
            CatService catService,
            DogService dogService,
            Executor executor
    ) {
        this.asyncCatService = asyncCatService;
        this.asyncDogService = asyncDogService;
        this.catService = catService;
        this.dogService = dogService;
        this.executor = executor;
    }

    @RequestMapping(path = "/sync", method = RequestMethod.GET)
    public Result getResultSync() {
        return new Result(catService.getCat().get().getName() + dogService.getDog().get().getName());
    }

    @RequestMapping(path = "/blocking", method = RequestMethod.GET)
    public Result getResultBlocking() throws Exception {
        CompletableFuture<Cat> catFuture = CompletableFuture.supplyAsync(() -> catService.getCat().get(), executor);
        CompletableFuture<Dog> dogFuture = CompletableFuture.supplyAsync(() -> dogService.getDog().get(), executor);
        return new Result(catFuture.get().getName() + dogFuture.get().getName());
    }

    //to suppress exceptions being logged twice, see https://jira.spring.io/browse/SPR-12608
    @RequestMapping(path = "/nonblocking", method = RequestMethod.GET)
    public CompletableFuture<Result> getResultNonBlocking() {
        CompletableFuture<Result> result = new CompletableFuture<>();

        CompletableFuture<Try<Cat>> tryCatFuture = asyncCatService.getCat();
        CompletableFuture<Try<Dog>> tryDogFuture = asyncDogService.getDog();

        tryCatFuture.thenCompose((tryCat) ->
                tryDogFuture.thenApply((tryDog) ->
                        tryCat.flatMap((cat) ->
                                tryDog.map((dog) ->
                                        new Result(cat.getName() + dog.getName())
                                )
                        )
                )
        ).thenAccept((tryResult) -> {
            if (tryResult.isSuccess()) {
                LOGGER.info("completed");
                result.complete(tryResult.get());
            } else {
                LOGGER.error("completed exceptionally");
                result.completeExceptionally(tryResult.getCause());
            }
        });

        /*
        //Unfortunately vavr's for comprehension BLOCKS (calls get on the future) the thread.
        Future<Try<Cat>> tryCatFuture = Future.fromCompletableFuture(asyncCatService.getCat());
        Future<Try<Dog>> tryDogFuture = Future.fromCompletableFuture(asyncDogService.getDog());
        For(
                tryCatFuture,
                tryDogFuture
        ).yield(
                //vavr's for comprehension discards the exception information, hence the flatMap/map
                (tryCat, tryDog) ->
                        tryCat.flatMap(cat ->
                                tryDog.map(
                                        dog -> new Result(cat.getName() + dog.getName())
                                )
                        )
        ).flatMap((tryResult) -> {
            if (tryResult.isSuccess()) {
                result.complete(tryResult.get());
                LOGGER.info("completed");
            } else {
                result.completeExceptionally(tryResult.getCause());
                LOGGER.error("completed exceptionally");
            }

            return Try.success(null);
        });
        */

        LOGGER.info("returned");
        return result;
    }
}
