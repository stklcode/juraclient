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

package de.stklcode.pubtrans.ura;

import de.stklcode.pubtrans.ura.model.Stop;
import de.stklcode.pubtrans.ura.model.Trip;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static net.bytebuddy.implementation.MethodDelegation.to;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;

/**
 * Unit test for the URA Client.
 * Tests run against mocked data collected from ASEAG API (http://ivu.aseag.de) and
 * TFL API (http://http://countdown.api.tfl.gov.uk)
 *
 * @author Stefan Kalscheuer
 */
public class UraClientTest {
    // Mocked resource URL and exception message.
    private static String mockResource = null;
    private static String mockException = null;

    @BeforeAll
    public static void initByteBuddy() {
        // Install ByteBuddy Agent.
        ByteBuddyAgent.install();

        new ByteBuddy().redefine(UraClient.class)
                .method(named("request"))
                .intercept(to(UraClientTest.class))
                .make()
                .load(UraClient.class.getClassLoader(), ClassReloadingStrategy.fromInstalledAgent());
    }

    @Test
    public void getStopsTest() {
        // Mock the HTTP call.
        mockHttpToFile("instant_V2_stops.txt");

        // List stops and verify some values.
        List<Stop> stops = new UraClient("mocked").getStops();
        assertThat(stops, hasSize(10));
        assertThat(stops.get(0).getId(), is("100210"));
        assertThat(stops.get(1).getName(), is("Brockenberg"));
        assertThat(stops.get(2).getState(), is(0));
        assertThat(stops.get(3).getLatitude(), is(50.7578775));
        assertThat(stops.get(4).getLongitude(), is(6.0708663));

        // Test Exception handling.
        mockHttpToException("Provoked Exception 1");

        try {
            new UraClient("mocked").getStops();
        } catch (RuntimeException e) {
            assertThat(e, is(instanceOf(IllegalStateException.class)));
            assertThat(e.getCause(), is(instanceOf(IOException.class)));
            assertThat(e.getCause().getMessage(), is("Provoked Exception 1"));
        }
    }

    public static InputStream request(String originalURL) throws IOException {
        if (mockResource == null && mockException != null) {
            IOException e = new IOException(mockException);
            mockException = null;
            throw e;
        }

        InputStream res = UraClientTest.class.getResourceAsStream(mockResource);
        mockResource = null;
        return res;
    }

    @Test
    public void getStopsForLineTest() {
        // Mock the HTTP call.
        mockHttpToFile("instant_V2_stops_line.txt");

        // List stops and verify some values.
        List<Stop> stops = new UraClient("mocked").forLines("33").getStops();
        assertThat(stops, hasSize(47));
        assertThat(stops.get(0).getId(), is("100000"));
        assertThat(stops.get(1).getName(), is("Kuckelkorn"));
        assertThat(stops.get(2).getState(), is(0));
        assertThat(stops.get(3).getLatitude(), is(50.7690688));
        assertThat(stops.get(4).getIndicator(), is("H.1"));
        assertThat(stops.get(5).getLongitude(), is(6.2314072));
    }

    @Test
    public void getStopsForPositionTest() {
        // Mock the HTTP call.
        mockHttpToFile("instant_V1_stops_circle.txt");

        // List stops and verify some values.
        List<Stop> stops = new UraClient("mocked")
                .forPosition(51.51009, -0.1345734, 200)
                .getStops();
        assertThat(stops, hasSize(13));
        assertThat(stops.get(0).getId(), is("156"));
        assertThat(stops.get(1).getName(), is("Piccadilly Circus"));
        assertThat(stops.get(2).getState(), is(0));
        assertThat(stops.get(3).getLatitude(), is(51.509154));
        assertThat(stops.get(4).getLongitude(), is(-0.134172));
        assertThat(stops.get(5).getIndicator(), is(nullValue()));

        mockHttpToFile("instant_V1_stops_circle_name.txt");
        stops = new UraClient("mocked")
                .forStopsByName("Piccadilly Circus")
                .forPosition(51.51009, -0.1345734, 200)
                .getStops();
        assertThat(stops, hasSize(7));
        assertThat(stops.stream().filter(t -> !t.getName().equals("Piccadilly Circus")).findAny(), is(Optional.empty()));
    }

    @Test
    public void getTripsForDestinationNamesTest() {
        // Mock the HTTP call.
        mockHttpToFile("instant_V1_trips_destination.txt");

        // List stops and verify some values.
        List<Trip> trips = new UraClient("mocked").forDestinationNames("Piccadilly Circus").getTrips();
        assertThat(trips, hasSize(9));
        assertThat(trips.stream().filter(t -> !t.getDestinationName().equals("Piccadilly Cir")).findAny(),
                is(Optional.empty()));

        mockHttpToFile("instant_V1_trips_stop_destination.txt");
        trips = new UraClient("mocked")
                .forStops("156")
                .forDestinationNames("Marble Arch")
                .getTrips();
        assertThat(trips, hasSize(5));
        assertThat(trips.stream().filter(t -> !t.getStop().getId().equals("156")).findAny(),
                is(Optional.empty()));
        assertThat(trips.stream().filter(t -> !t.getDestinationName().equals("Marble Arch")).findAny(),
                is(Optional.empty()));
    }

    @Test
    public void getTripsTowardsTest() {
        // Mock the HTTP call.
        mockHttpToFile("instant_V1_trips_towards.txt");

        /* List stops and verify some values */
        List<Trip> trips = new UraClient("mocked").towards("Marble Arch").getTrips();
        assertThat(trips, hasSize(10));

        mockHttpToFile("instant_V1_trips_stop_towards.txt");
        trips = new UraClient("mocked").forStops("156").towards("Marble Arch").getTrips();
        assertThat(trips, hasSize(17));
        assertThat(trips.stream().filter(t -> !t.getStop().getId().equals("156")).findAny(), is(Optional.empty()));
    }

    @Test
    public void getTripsTest() {
        // Mock the HTTP call.
        mockHttpToFile("instant_V1_trips_all.txt");

        // Get trips without filters and verify some values.
        List<Trip> trips = new UraClient("mocked").getTrips();
        assertThat(trips, hasSize(10));
        assertThat(trips.get(0).getId(), is("27000165015001"));
        assertThat(trips.get(1).getLineID(), is("55"));
        assertThat(trips.get(2).getLineName(), is("28"));
        assertThat(trips.get(3).getDirectionID(), is(1));
        assertThat(trips.get(4).getDestinationName(), is("Verlautenheide Endstr."));
        assertThat(trips.get(5).getDestinationText(), is("Aachen Bushof"));
        assertThat(trips.get(6).getVehicleID(), is("247"));
        assertThat(trips.get(7).getEstimatedTime(), is(1482854580000L));
        assertThat(trips.get(8).getVisitID(), is(30));
        assertThat(trips.get(9).getStop().getId(), is("100002"));

        // Repeat test for API V2.
        mockHttpToFile("instant_V2_trips_all.txt");

        // Get trips without filters and verify some values.
        trips = new UraClient("mocked").getTrips();
        assertThat(trips, hasSize(10));
        assertThat(trips.get(0).getId(), is("27000165015001"));
        assertThat(trips.get(1).getLineID(), is("55"));
        assertThat(trips.get(2).getLineName(), is("28"));
        assertThat(trips.get(3).getDirectionID(), is(1));
        assertThat(trips.get(4).getDestinationName(), is("Verlautenheide Endstr."));
        assertThat(trips.get(5).getDestinationText(), is("Aachen Bushof"));
        assertThat(trips.get(6).getVehicleID(), is("247"));
        assertThat(trips.get(7).getEstimatedTime(), is(1482854580000L));
        assertThat(trips.get(8).getVisitID(), is(30));
        assertThat(trips.get(9).getStop().getId(), is("100002"));

        // Get limited number of trips.
        mockHttpToFile("instant_V1_trips_all.txt");
        trips = new UraClient("mocked").getTrips(5);
        assertThat(trips, hasSize(5));

        // Test mockException handling.
        mockHttpToException("Provoked mockException 2");
        try {
            new UraClient("mocked").getTrips();
        } catch (RuntimeException e) {
            assertThat(e, is(instanceOf(IllegalStateException.class)));
            assertThat(e.getCause(), is(instanceOf(IOException.class)));
            assertThat(e.getCause().getMessage(), is("Provoked mockException 2"));
        }
    }

    @Test
    public void getTripsForStopTest() {
        // Mock the HTTP call.
        mockHttpToFile("instant_V1_trips_stop.txt");

        // Get trips for stop ID 100000 (Aachen Bushof) and verify some values.
        List<Trip> trips = new UraClient("mocked")
                .forStops("100000")
                .getTrips();
        assertThat(trips, hasSize(10));
        assertThat(trips.stream().filter(t -> !t.getStop().getId().equals("100000")).findAny(), is(Optional.empty()));
        assertThat(trips.get(0).getId(), is("27000158010001"));
        assertThat(trips.get(1).getLineID(), is("7"));
        assertThat(trips.get(2).getLineName(), is("25"));
        assertThat(trips.get(3).getStop().getIndicator(), is("H.15"));

        // Get trips for stop name "Uniklinik" and verify some values.
        mockHttpToFile("instant_V1_trips_stop_name.txt");
        trips = new UraClient("mocked")
                .forStopsByName("Uniklinik")
                .getTrips();
        assertThat(trips, hasSize(10));
        assertThat(trips.stream().filter(t -> !t.getStop().getName().equals("Uniklinik")).findAny(),
                is(Optional.empty()));
        assertThat(trips.get(0).getId(), is("92000043013001"));
        assertThat(trips.get(1).getLineID(), is("5"));
        assertThat(trips.get(2).getVehicleID(), is("317"));
        assertThat(trips.get(3).getDirectionID(), is(1));
    }

    @Test
    public void getTripsForLine() {
        // Mock the HTTP call.
        mockHttpToFile("instant_V1_trips_line.txt");

        // Get trips for line ID 3 and verify some values.
        List<Trip> trips = new UraClient("mocked")
                .forLines("3")
                .getTrips();
        assertThat(trips, hasSize(10));
        assertThat(trips.stream().filter(t -> !t.getLineID().equals("3")).findAny(), is(Optional.empty()));
        assertThat(trips.get(0).getId(), is("27000154004001"));
        assertThat(trips.get(1).getLineID(), is("3"));
        assertThat(trips.get(2).getLineName(), is("3.A"));
        assertThat(trips.get(3).getStop().getIndicator(), is("H.4 (Pontwall)"));

        // Get trips for line name "3.A" and verify some values.
        mockHttpToFile("instant_V1_trips_line_name.txt");
        trips = new UraClient("mocked")
                .forLinesByName("3.A")
                .getTrips();
        assertThat(trips, hasSize(10));
        assertThat(trips.stream().filter(t -> !t.getLineName().equals("3.A")).findAny(), is(Optional.empty()));
        assertThat(trips.get(0).getId(), is("92000288014001"));
        assertThat(trips.get(1).getLineID(), is("3"));
        assertThat(trips.get(2).getLineName(), is("3.A"));
        assertThat(trips.get(3).getStop().getName(), is("Aachen Gartenstraße"));

        // Get trips for line 3 with direction 1 and verify some values.
        mockHttpToFile("instant_V1_trips_line_direction.txt");
        trips = new UraClient("mocked")
                .forLines("412")
                .forDirection(2)
                .getTrips();
        assertThat(trips, hasSize(10));
        assertThat(trips.stream().filter(t -> !t.getLineID().equals("412")).findAny(), is(Optional.empty()));
        assertThat(trips.stream().filter(t -> !t.getDirectionID().equals(2)).findAny(), is(Optional.empty()));

        // Test lineID and direction in different order.
        mockHttpToFile("instant_V1_trips_line_direction.txt");
        trips = new UraClient("mocked")
                .forDirection(2)
                .forLines("412")
                .getTrips();
        assertThat(trips, hasSize(10));
        assertThat(trips.stream().filter(t -> !t.getLineID().equals("412")).findAny(), is(Optional.empty()));
        assertThat(trips.stream().filter(t -> !t.getDirectionID().equals(2)).findAny(), is(Optional.empty()));
    }

    @Test
    public void getTripsForStopAndLine() {
        // Mock the HTTP call.
        mockHttpToFile("instant_V1_trips_stop_line.txt");

        // Get trips for line ID 25 and 25 at stop 100000 and verify some values.
        List<Trip> trips = new UraClient("mocked")
                .forLines("25", "35")
                .forStops("100000")
                .getTrips();
        assertThat(trips, hasSize(10));
        assertThat(trips.stream().filter(t -> !t.getLineID().equals("25") && !t.getLineID().equals("35")).findAny(),
                is(Optional.empty()));
        assertThat(trips.stream().filter(t -> !t.getStop().getId().equals("100000")).findAny(), is(Optional.empty()));
        assertThat(trips.get(0).getId(), is("27000078014001"));
        assertThat(trips.get(1).getLineID(), is("25"));
        assertThat(trips.get(3).getLineName(), is("35"));
        assertThat(trips.get(5).getStop().getIndicator(), is("H.12"));
    }


    private static void mockHttpToFile(String newResourceFile) {
        mockResource = newResourceFile;
    }

    private static void mockHttpToException(String newException) {
        mockException = newException;
    }
}
