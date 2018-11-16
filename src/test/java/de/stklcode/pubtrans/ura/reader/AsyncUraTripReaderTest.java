/*
 * Copyright 2016-2018 Stefan Kalscheuer
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
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;

/**
 * Unit test for the asynchronous URA Trip reader.
 *
 * @author Stefan Kalscheuer
 */
public class AsyncUraTripReaderTest {

    @Test
    public void readerTest() throws InterruptedException, MalformedURLException {
        // The list which will be populated by the callback.
        List<Trip> trips = new ArrayList<>();

        // Start with V1 data.
        AsyncUraTripReader tr = new AsyncUraTripReader(
                UraClientTest.class.getResource("stream_V1_stops_all.txt"),
                Collections.singletonList(trips::add)
        );

        // Read for 2 seconds before closing.
        tr.open();
        TimeUnit.SECONDS.sleep(1);
        tr.close();

        assertThat("Trips should not be empty after 1s", trips, is(not(empty())));
        assertThat("Unexpected number of trips after 1s", trips.size(), is(7));

        // Clear trip list and repeat with V2 data.
        trips.clear();
        tr = new AsyncUraTripReader(
                UraClientTest.class.getResource("stream_V2_stops_all.txt"),
                Collections.singletonList(trips::add)
        );

        // Read for 2 seconds before closing.
        tr.open();
        TimeUnit.SECONDS.sleep(1);
        tr.close();

        assertThat("Trips should not be empty after 1s", trips, is(not(empty())));
        assertThat("Unexpected number of trips after 1s", trips.size(), is(7));
    }


}
