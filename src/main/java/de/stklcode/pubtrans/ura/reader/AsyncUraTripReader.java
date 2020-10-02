/*
 * Copyright 2016-2020 Stefan Kalscheuer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.stklcode.pubtrans.ura.reader;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.stklcode.pubtrans.ura.model.Trip;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Asynchronous stream reader for URA stream API.
 * <p>
 * This reader provides a handler for asynchronous stream events.
 *
 * @author Stefan Kalscheuer
 * @since 1.2.0
 */
public class AsyncUraTripReader implements AutoCloseable {
    private static final Integer RES_TYPE_PREDICTION = 1;
    private static final Integer RES_TYPE_URA_VERSION = 4;

    private final List<Consumer<Trip>> consumers;
    private final URI uri;
    private JsonLineSubscriber subscriber;
    private CompletableFuture<Void> future;

    /**
     * Initialize trip reader.
     *
     * @param uri      URL to read trips from.
     * @param consumer Initial consumer.
     * @since 2.0 Parameter of Type {@link URI} instead of {@link java.net.URL}.
     */
    public AsyncUraTripReader(URI uri, Consumer<Trip> consumer) {
        this.uri = uri;
        this.consumers = new ArrayList<>();
        this.consumers.add(consumer);
    }

    /**
     * Initialize trip reader.
     *
     * @param uri       URL to read trips from.
     * @param consumers Initial list of consumers.
     * @since 2.0 Parameter of Type {@link URI} instead of {@link java.net.URL}.
     */
    public AsyncUraTripReader(URI uri, List<Consumer<Trip>> consumers) {
        this.uri = uri;
        this.consumers = new ArrayList<>(consumers);
    }

    /**
     * Open the reader, i.e. initiate connection to the API and start reading the response stream.
     */
    public void open() {
        // Throw exception, if future is already present.
        if (future != null) {
            throw new IllegalStateException("Reader already opened");
        }

        this.subscriber = new JsonLineSubscriber();
        HttpClient.newHttpClient().sendAsync(
                HttpRequest.newBuilder(uri).GET().build(),
                HttpResponse.BodyHandlers.fromLineSubscriber(subscriber)
        ).exceptionally(throwable -> {
            subscriber.onError(throwable);
            return null;
        });
        this.future = subscriber.getState();
    }

    /**
     * Register an additional consumer.
     *
     * @param consumer New consumer.
     */
    public void addConsumer(Consumer<Trip> consumer) {
        consumers.add(consumer);
    }

    /**
     * Close the reader.
     * This is done by signaling cancel to the asynchronous task. If the task is not completed
     * within 1 second however it is canceled hard.
     */
    @Override
    public void close() {
        // Nothing to do if future is not yet started.
        if (future == null) {
            return;
        }

        // Signal cancelling to gracefully stop future.
        subscriber.cancel();
        try {
            future.get(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed to close API connection", e);
        } catch (TimeoutException e) {
            // Task failed to finish within 1 second.
            future.cancel(true);
        }
    }

    /**
     * JSON line subscriber for asynchronous response handling.
     *
     * @since 2.0
     */
    private class JsonLineSubscriber implements Flow.Subscriber<String> {
        private final ObjectMapper mapper = new ObjectMapper();
        private final CompletableFuture<Void> state = new CompletableFuture<>();
        private Flow.Subscription subscription;
        private String version = null;

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            this.subscription = subscription;
            this.subscription.request(1);
        }

        @Override
        public void onNext(String item) {
            try {
                List<Serializable> l = mapper.readValue(item, mapper.getTypeFactory().constructCollectionType(List.class, Serializable.class));
                // Check if result exists and has correct response type.
                if (l != null && !l.isEmpty()) {
                    if (l.get(0).equals(RES_TYPE_URA_VERSION)) {
                        version = l.get(1).toString();
                    } else if (l.get(0).equals(RES_TYPE_PREDICTION)) {
                        // Parse Trip and pass to each consumer.
                        Trip trip = new Trip(l, version);
                        consumers.forEach(c -> c.accept(trip));
                    }
                }

                // Request next item.
                this.subscription.request(1);
            } catch (IOException e) {
                onError(e);
            }
        }

        @Override
        public void onError(Throwable throwable) {
            state.completeExceptionally(throwable);
        }

        @Override
        public void onComplete() {
            state.complete(null);
        }

        /**
         * Retrieve the state future.
         *
         * @return State future.
         */
        public CompletableFuture<Void> getState() {
            return state;
        }

        /**
         * Cancel the current subscription.
         */
        public void cancel() {
            state.complete(null);
            if (subscription != null) {
                subscription.cancel();
            }
        }
    }
}
