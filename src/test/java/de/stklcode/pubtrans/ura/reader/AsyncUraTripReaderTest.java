/*
 * Copyright 2016-2019 Stefan Kalscheuer
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

import de.stklcode.pubtrans.ura.UraClientTest;
import de.stklcode.pubtrans.ura.model.Trip;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static net.bytebuddy.implementation.MethodDelegation.to;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Unit test for the asynchronous URA Trip reader.
 *
 * @author Stefan Kalscheuer
 */
public class AsyncUraTripReaderTest {
    private static final Queue<String> MOCK_LINES = new ArrayDeque<>();
    private static PipedOutputStream mockOutputStream = new PipedOutputStream();

    @BeforeAll
    public static void initByteBuddy() {
        // Install ByteBuddy Agent.
        ByteBuddyAgent.install();

        // Mock the URL.openStream() call.
        new ByteBuddy().redefine(AsyncUraTripReader.class)
                .method(named("getInputStream"))
                .intercept(to(AsyncUraTripReaderTest.class))
                .make()
                .load(AsyncUraTripReader.class.getClassLoader(), ClassReloadingStrategy.fromInstalledAgent());
    }

    /**
     * Test the reader.
     * This test contains some timing values, which is not very nice for debugging, but should do the job here
     * as 1s is most likely more than enough time on any reasonable build system to parse some simple JSON lines.
     *
     * @throws InterruptedException Thread interrupted.
     * @throws IOException          Error reading or writing mocked data.
     */
    @Test
    public void readerTest() throws InterruptedException, IOException {
        // Callback counter for some unhandy async mockery.
        final AtomicInteger counter = new AtomicInteger(0);

        // The list which will be populated by the callback.
        Deque<Trip> trips = new ConcurrentLinkedDeque<>();

        // Start with V1 data and read file to mock list.
        readLinesToMock(UraClientTest.class.getResource("stream_V1_stops_all.txt"));

        AsyncUraTripReader tr = new AsyncUraTripReader(
                UraClientTest.class.getResource("stream_V1_stops_all.txt"),
                Collections.singletonList(
                        trip -> {
                            trips.add(trip);
                            counter.incrementAndGet();
                        }
                )
        );

        // Open the rewader.
        tr.open();
        // Read for 1 second.
        TimeUnit.SECONDS.sleep(1);
        assumeTrue(trips.isEmpty(), "Trips should empty after 1s without reading");

        // Now write a single line to the stream pipe.
        assumeTrue(writeNextLine(), "First line (version info) should be written");
        assumeTrue(writeNextLine(), "Second line (first record) should be written");

        // Wait up to 1s for the callback to be triggered.
        int i = 10;
        while (counter.get() < 1 && i-- > 0) {
            TimeUnit.MILLISECONDS.sleep(100);
        }

        assertThat("Unexpected number of trips after first entry", trips.size(), is(1));

        // Flush all remaining lines.
        while (writeNextLine()) {
            TimeUnit.MILLISECONDS.sleep(10);
        }

        i = 10;
        counter.set(0);
        while (counter.get() < 1 && i-- > 0) {
            TimeUnit.MILLISECONDS.sleep(100);
        }

        tr.close();

        assertThat("Unexpected number of trips after all lines have been flushed", trips.size(), is(7));

        // Clear trip list and repeat with V2 data.
        trips.clear();
        readLinesToMock(UraClientTest.class.getResource("stream_V2_stops_all.txt"));
        tr = new AsyncUraTripReader(
                UraClientTest.class.getResource("stream_V2_stops_all.txt"),
                Collections.singletonList(trips::add)
        );

        // Open the reader.
        tr.open();
        // Read for 1 second.
        TimeUnit.SECONDS.sleep(1);
        assumeTrue(trips.isEmpty(), "Trips should empty after 1s without reading");

        assumeTrue(writeNextLine(), "First line of v2 (version info) should be written");
        assumeTrue(writeNextLine(), "Second line of v2 (first record) should be written");

        i = 10;
        counter.set(0);
        while (counter.get() < 1 && i-- > 0) {
            TimeUnit.MILLISECONDS.sleep(100);
        }
        assertThat("Unexpected number of v2 trips after first entry", trips.size(), is(1));

        // Add a second consumer that pushes to another list.
        Deque<Trip> trips2 = new ConcurrentLinkedDeque<>();
        tr.addConsumer(trips2::add);

        // Flush all remaining lines.
        while (writeNextLine()) {
            TimeUnit.MILLISECONDS.sleep(10);
        }

        i = 10;
        counter.set(0);
        while (counter.get() < 1 && i-- > 0) {
            TimeUnit.MILLISECONDS.sleep(100);
        }

        tr.close();

        assertThat("Unexpected number of v2 trips after all lines have been flushed", trips.size(), is(7));
        assertThat("Unexpected number of v2 trips in list 2 after all lines have been flushed", trips2.size(), is(6));
        assertThat("Same object should have been pushed to both lists", trips.containsAll(trips2));
    }

    /**
     * Test behavior if the stream is closed.
     *
     * @throws InterruptedException Thread interrupted.
     * @throws IOException          Error reading or writing mocked data.
     */
    @Test
    public void streamClosedTest() throws InterruptedException, IOException {
        // Callback counter for some unhandy async mockery.
        final AtomicInteger counter = new AtomicInteger(0);

        // The list which will be populated by the callback.
        Deque<Trip> trips = new ConcurrentLinkedDeque<>();

        // Start with V1 data and read file to mock list.
        readLinesToMock(UraClientTest.class.getResource("stream_V1_stops_all.txt"));

        AsyncUraTripReader tr = new AsyncUraTripReader(
                UraClientTest.class.getResource("stream_V1_stops_all.txt"),
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

        // Now write a single line to the stream pipe.
        assumeTrue(writeNextLine(), "First line (version info) should be written");
        assumeTrue(writeNextLine(), "Second line (first record) should be written");

        // Wait up to 1s for the callback to be triggered.
        int i = 10;
        while (counter.get() < 1 && i-- > 0) {
            TimeUnit.MILLISECONDS.sleep(100);
        }

        assumeTrue(1 == trips.size(), "Unexpected number of trips after first entry");

        // Close the stream.
        mockOutputStream.close();

        i = 10;
        counter.set(0);
        while (counter.get() < 1 && i-- > 0) {
            TimeUnit.MILLISECONDS.sleep(100);
        }

        tr.close();

        assertThat("Unexpected number of trips after all lines have been flushed", trips.size(), is(1));

    }

    /**
     * Read an input file to the line buffer.
     *
     * @param url Input URL.
     * @throws IOException Error reading the data.
     */
    private static void readLinesToMock(URL url) throws IOException {
        try (InputStream is = url.openStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line = br.readLine();
            while (line != null) {
                MOCK_LINES.add(line);
                line = br.readLine();
            }
        }
    }

    /**
     * Write next line from the buffer to the mocked stream pipe.
     *
     * @return {@code true} if a line has been written.
     * @throws IOException Errir writing the data.
     */
    private static boolean writeNextLine() throws IOException {
        String line = MOCK_LINES.poll();
        if (line != null) {
            line += "\n";
            mockOutputStream.write(line.getBytes(StandardCharsets.UTF_8));
            mockOutputStream.flush();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Function to mock the static {@code AsyncUraTripReader#getInputStream(URL)} method.
     *
     * @param url URL to read from.
     * @return Input Stream.
     * @throws IOException On errors.
     */
    public static InputStream getInputStream(URL url) throws IOException {
        mockOutputStream = new PipedOutputStream();
        return new PipedInputStream(mockOutputStream);
    }
}
