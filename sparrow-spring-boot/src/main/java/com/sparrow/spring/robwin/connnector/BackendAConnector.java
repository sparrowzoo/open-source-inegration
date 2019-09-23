package com.sparrow.spring.robwin.connnector;


import com.sparrow.spring.robwin.exception.BusinessException;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static io.github.resilience4j.bulkhead.annotation.Bulkhead.*;

/**
 * This Connector shows how to use the CircuitBreaker annotation.
 */
@CircuitBreaker(name = "backendA")
@RateLimiter(name = "backendA")
@Component(value = "backendAConnector")
public class BackendAConnector implements Connector {

    @Override
    @Bulkhead(name = "backendA")
    public String failure() {
        throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "This is a remote exception");
    }

    @Override
    public String ignoreException() {
        throw new BusinessException("This exception is ignored by the CircuitBreaker of backend A");
    }

    @Override
    @Bulkhead(name = "backendA")
    public String success() {
        try {
            Thread.sleep(3000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "Hello World from backend A";
    }

    @Override
    public String successException() {
        throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "This is a remote client exception");
    }

    @Override
    @Bulkhead(name = "backendA")
    public Flux<String> fluxFailure() {
        return Flux.error(new IOException("BAM!"));
    }

    @Override
    @Bulkhead(name = "backendA")
    public Mono<String> monoSuccess() {
        return Mono.just("Hello World from backend A AAA");
    }

    @Override
    @Bulkhead(name = "backendA")
    public Mono<String> monoFailure() {
        return Mono.error(new IOException("BAM!"));
    }

    @Override
    @Bulkhead(name = "backendA")
    public Flux<String> fluxSuccess() {
        return Flux.just("Hello", "World");
    }

    @Override
    @CircuitBreaker(name = "backendA", fallbackMethod = "fallback")
    public String failureWithFallback() {
        return failure();
    }

    @Override
    @Bulkhead(name = "backendA", type = Type.THREADPOOL)
    public CompletableFuture<String> futureSuccess() {
        return CompletableFuture.completedFuture("Hello World from backend A");
    }

    @Override
    @Bulkhead(name = "backendA", type = Type.THREADPOOL)
    public CompletableFuture<String> futureFailure() {
        CompletableFuture<String> future = new CompletableFuture<>();
        future.completeExceptionally(new IOException("BAM!"));
        return future;
    }

    private String fallback(HttpServerErrorException ex) {
        return "Recovered HttpServerErrorException: " + ex.getMessage();
    }

    private String fallback(Throwable ex) {
        return "Recovered Throwable: " + ex.getMessage();
    }
}
