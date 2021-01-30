/*
 * Copyright 2016-2021 Stefan Kalscheuer
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

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.http.ChunkedDribbleDelay;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;
import de.stklcode.pubtrans.ura.UraClientConfiguration;
import de.stklcode.pubtrans.ura.model.Trip;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Unit test for the asynchronous URA Trip reader.
 * <p>
 * Because this test runs asynchronously, it might not work as expected in debugging environments.
 * Stream input files are normalized to equal line length and split into chunks, one line each 500ms.
 *
 * @author Stefan Kalscheuer
 */
public class AsyncUraTripReaderTest {
    private static WireMockServer httpMock;

    @BeforeAll
    public static void setUp() {
        // Initialize HTTP mock.
        httpMock = new WireMockServer(WireMockConfiguration.options().dynamicPort()
                .asynchronousResponseEnabled(true)
                .extensions(StreamTransformer.class)
        );
        httpMock.start();
        WireMock.configureFor("localhost", httpMock.port());
    }

    @AfterAll
    public static void tearDown() {
        httpMock.stop();
        httpMock = null;
    }

    /**
     * Test the reader.
     * This test contains some timing values, which is not very nice for debugging, but should do the job here
     * as 1s is most likely more than enough time on any reasonable build system to parse some simple JSON lines.
     *
     * @throws InterruptedException Thread interrupted.
     */
    @Test
    public void readerTest() throws InterruptedException {
        // Callback counter for some unhandy async mockery.
        final AtomicInteger counter = new AtomicInteger(0);

        // The list which will be populated by the callback.
        Deque<Trip> trips = new ConcurrentLinkedDeque<>();

        // Start with V1 data and read file to mock list.
        readLinesToMock(1, "/__files/stream_V1_stops_all.txt", 8);

        AsyncUraTripReader tr = new AsyncUraTripReader(
                URI.create(httpMock.baseUrl() + "/interfaces/ura/stream_V1"),
                Collections.singletonList(
                        trip -> {
                            trips.add(trip);
                            counter.incrementAndGet();
                        }
                )
        );

        // Open the reader.
        tr.open();
        // Read for 1 second.
        TimeUnit.SECONDS.sleep(1);
        assumeTrue(trips.isEmpty(), "Trips should empty after 1s without reading");

        // Wait another 1s for the callback to be triggered.
        TimeUnit.SECONDS.sleep(1);

        assertThat("Unexpected number of trips after first entry", trips.size(), is(2));

        // Flush all remaining lines.
        TimeUnit.SECONDS.sleep(3);

        assertThat("Unexpected number of trips after all lines have been flushed", trips.size(), is(7));

        // Clear trip list and repeat with V2 data.
        trips.clear();
        readLinesToMock(2, "/__files/stream_V2_stops_all.txt", 8);

        tr = new AsyncUraTripReader(
                URI.create(httpMock.baseUrl() + "/interfaces/ura/stream_V2"),
                trips::add
        );

        // Open the reader.
        tr.open();
        // Read for 1 second.
        TimeUnit.SECONDS.sleep(1);
        assumeTrue(trips.isEmpty(), "Trips should empty after 1s without reading");

        TimeUnit.SECONDS.sleep(1);
        assertThat("Unexpected number of v2 trips after first entry", trips.size(), is(2));

        // Add a second consumer that pushes to another list.
        Deque<Trip> trips2 = new ConcurrentLinkedDeque<>();
        tr.addConsumer(trips2::add);

        // Flush all remaining lines.
        TimeUnit.SECONDS.sleep(3);

        tr.close();

        assertThat("Unexpected number of v2 trips after all lines have been flushed", trips.size(), is(7));
        assertThat("Unexpected number of v2 trips in list 2 after all lines have been flushed", trips2.size(), is(5));
        assertThat("Same object should have been pushed to both lists", trips.containsAll(trips2));

        // Opening the reader twice should raise an exception.
        assertDoesNotThrow(tr::open, "Opening the reader after closing should not fail");
        assertThrows(IllegalStateException.class, tr::open, "Opening the reader twice should raise an exception");
        tr.close();
    }

    /**
     * Test behavior if the stream is closed.
     *
     * @throws InterruptedException Thread interrupted.
     */
    @Test
    public void streamClosedTest() throws InterruptedException {
        // Callback counter for some unhandy async mockery.
        final AtomicInteger counter = new AtomicInteger(0);

        // The list which will be populated by the callback.
        Deque<Trip> trips = new ConcurrentLinkedDeque<>();

        // Start with V1 data and read file to mock list.
        readLinesToMock(1, "/__files/stream_V1_stops_all.txt", 8);

        AsyncUraTripReader tr = new AsyncUraTripReader(
                URI.create(httpMock.baseUrl() + "/interfaces/ura/stream_V1"),
                Collections.singletonList(
                        trip -> {
                            trips.add(trip);
                            counter.incrementAndGet();
                        }
                )
        );

        // Open the reader.
        tr.open();

        // Read for 100ms.
        TimeUnit.MILLISECONDS.sleep(100);
        assumeTrue(trips.isEmpty(), "Trips should empty after 100ms without reading");

        // Wait for 1s for the callback to be triggered.
        TimeUnit.SECONDS.sleep(1);

        assumeTrue(1 == trips.size(), "Unexpected number of trips after first entry");

        // Close the stream.
        tr.close();

        // Wait for another second.
        TimeUnit.MILLISECONDS.sleep(1);
        assertThat("Unexpected number of trips after all lines have been flushed", trips.size(), is(1));
    }

    @Test
    public void timeoutTest() throws InterruptedException {
        // Callback counter for some unhandy async mockery.
        final AtomicInteger counter = new AtomicInteger(0);

        // The list which will be populated by the callback.
        Deque<Trip> trips = new ConcurrentLinkedDeque<>();

        // Start with V1 data and read file to mock list.
        readLinesToMock(1, "/__files/stream_V1_stops_all.txt", 8);

        AsyncUraTripReader tr = new AsyncUraTripReader(
                URI.create(httpMock.baseUrl() + "/interfaces/ura/stream_V1"),
                UraClientConfiguration.forBaseURL(httpMock.baseUrl())
                        .withConnectTimeout(Duration.ofMillis(100))
                        .build(),
                Collections.singletonList(
                        trip -> {
                            trips.add(trip);
                            counter.incrementAndGet();
                        }
                )
        );

        // Open the reader.
        tr.open();
        // Read for 1 second.
        TimeUnit.SECONDS.sleep(1);
        assumeTrue(trips.isEmpty(), "Trips should empty after 1s without reading");

        // Wait another 1s for the callback to be triggered.
        TimeUnit.SECONDS.sleep(1);

        assertThat("Unexpected number of trips after first entry", trips.size(), is(2));

        // Flush all remaining lines.
        TimeUnit.SECONDS.sleep(3);

        assertThat("Unexpected number of trips after all lines have been flushed", trips.size(), is(7));

        // Clear trip list and repeat with V2 data.
        trips.clear();
        readLinesToMock(2, "/__files/stream_V2_stops_all.txt", 8);

        tr = new AsyncUraTripReader(
                URI.create(httpMock.baseUrl() + "/interfaces/ura/stream_V2"),
                Collections.singletonList(trips::add)
        );

        // Open the reader.
        tr.open();
        // Read for 1 second.
        TimeUnit.SECONDS.sleep(1);
        assumeTrue(trips.isEmpty(), "Trips should empty after 1s without reading");

        TimeUnit.SECONDS.sleep(1);
        assertThat("Unexpected number of v2 trips after first entry", trips.size(), is(2));

        // Add a second consumer that pushes to another list.
        Deque<Trip> trips2 = new ConcurrentLinkedDeque<>();
        tr.addConsumer(trips2::add);

        // Flush all remaining lines.
        TimeUnit.SECONDS.sleep(3);

        tr.close();

        assertThat("Unexpected number of v2 trips after all lines have been flushed", trips.size(), is(7));
        assertThat("Unexpected number of v2 trips in list 2 after all lines have been flushed", trips2.size(), is(5));
        assertThat("Same object should have been pushed to both lists", trips.containsAll(trips2));
    }

    /**
     * Read an input file to the line buffer.
     *
     * @param version      API version.
     * @param resourceFile Resource file name.
     * @param chunks       Number of chunks.
     */
    private void readLinesToMock(int version, String resourceFile, int chunks) {
        WireMock.stubFor(get(urlPathEqualTo("/interfaces/ura/stream_V" + version))
                .willReturn(aResponse()
                        .withTransformer("stream-transformer", "source", resourceFile)
                        .withTransformer("stream-transformer", "chunks", chunks)
                )
        );
    }

    public static class StreamTransformer extends ResponseTransformer {
        @Override
        public Response transform(Request request, Response response, FileSource files, Parameters parameters) {
            int chunks = parameters.getInt("chunks", 1);
            return Response.Builder.like(response)
                    // Read source file to response.
                    .body(() -> AsyncUraTripReaderTest.class.getResourceAsStream(parameters.getString("source")))
                    // Split response in given number of chunks with 500ms delay.
                    .chunkedDribbleDelay(new ChunkedDribbleDelay(chunks, chunks * 500))
                    .build();
        }

        @Override
        public String getName() {
            return "stream-transformer";
        }
    }
}
