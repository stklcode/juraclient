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

package de.stklcode.pubtrans.ura;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.Fault;
import de.stklcode.pubtrans.ura.exception.UraClientException;
import de.stklcode.pubtrans.ura.model.Message;
import de.stklcode.pubtrans.ura.model.Stop;
import de.stklcode.pubtrans.ura.model.Trip;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for the URA Client.
 * Tests run against mocked data collected from ASEAG API (http://ivu.aseag.de) and
 * TFL API (http://http://countdown.api.tfl.gov.uk)
 *
 * @author Stefan Kalscheuer
 */
public class UraClientTest {
    private static WireMockServer httpMock;

    @BeforeAll
    public static void setUp() {
        // Initialize HTTP mock.
        httpMock = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        httpMock.start();
        WireMock.configureFor("localhost", httpMock.port());
    }

    @AfterAll
    public static void tearDown() {
        httpMock.stop();
        httpMock = null;
    }

    @Test
    public void getStopsTest() throws UraClientException {
        // Mock the HTTP call.
        mockHttpToFile(2, "instant_V2_stops.txt");

        // List stops and verify some values.
        List<Stop> stops = new UraClient(httpMock.baseUrl(), "/interfaces/ura/instant_V2", "/interfaces/ura/stream").getStops();
        assertThat(stops, hasSize(10));
        assertThat(stops.get(0).getId(), is("100210"));
        assertThat(stops.get(1).getName(), is("Brockenberg"));
        assertThat(stops.get(2).getState(), is(0));
        assertThat(stops.get(3).getLatitude(), is(50.7578775));
        assertThat(stops.get(4).getLongitude(), is(6.0708663));

        // Test Exception handling.
        mockHttpToError(500);

        try {
            new UraClient(httpMock.baseUrl()).getStops();
        } catch (RuntimeException e) {
            assertThat(e, is(instanceOf(IllegalStateException.class)));
            assertThat(e.getCause(), is(instanceOf(IOException.class)));
            assertThat(e.getCause().getMessage(), startsWith("Server returned HTTP response code: 500 for URL"));
        }
    }

    @Test
    public void getStopsForLineTest() throws UraClientException {
        // Mock the HTTP call.
        mockHttpToFile(2, "instant_V2_stops_line.txt");

        // List stops and verify some values.
        List<Stop> stops = new UraClient(httpMock.baseUrl(), "/interfaces/ura/instant_V2", "/interfaces/ura/stream")
                .forLines("33")
                .getStops();
        assertThat(stops, hasSize(47));
        assertThat(stops.get(0).getId(), is("100000"));
        assertThat(stops.get(1).getName(), is("Kuckelkorn"));
        assertThat(stops.get(2).getState(), is(0));
        assertThat(stops.get(3).getLatitude(), is(50.7690688));
        assertThat(stops.get(4).getIndicator(), is("H.1"));
        assertThat(stops.get(5).getLongitude(), is(6.2314072));
    }

    @Test
    public void getStopsForPositionTest() throws UraClientException {
        // Mock the HTTP call.
        mockHttpToFile(1, "instant_V1_stops_circle.txt");

        // List stops and verify some values.
        List<Stop> stops = new UraClient(httpMock.baseUrl())
                .forPosition(51.51009, -0.1345734, 200)
                .getStops();
        assertThat(stops, hasSize(13));
        assertThat(stops.get(0).getId(), is("156"));
        assertThat(stops.get(1).getName(), is("Piccadilly Circus"));
        assertThat(stops.get(2).getState(), is(0));
        assertThat(stops.get(3).getLatitude(), is(51.509154));
        assertThat(stops.get(4).getLongitude(), is(-0.134172));
        assertThat(stops.get(5).getIndicator(), is(nullValue()));

        mockHttpToFile(1, "instant_V1_stops_circle_name.txt");
        stops = new UraClient(httpMock.baseUrl())
                .forStopsByName("Piccadilly Circus")
                .forPosition(51.51009, -0.1345734, 200)
                .getStops();
        assertThat(stops, hasSize(7));
        assertThat(stops.stream().filter(t -> !t.getName().equals("Piccadilly Circus")).findAny(), is(Optional.empty()));
    }

    @Test
    public void getTripsForDestinationNamesTest() throws UraClientException {
        // Mock the HTTP call.
        mockHttpToFile(1, "instant_V1_trips_destination.txt");

        // List stops and verify some values.
        List<Trip> trips = new UraClient(httpMock.baseUrl()).forDestinationNames("Piccadilly Circus").getTrips();
        assertThat(trips, hasSize(9));
        assertThat(trips.stream().filter(t -> !t.getDestinationName().equals("Piccadilly Cir")).findAny(),
                is(Optional.empty()));

        mockHttpToFile(1, "instant_V1_trips_stop_destination.txt");
        trips = new UraClient(httpMock.baseUrl())
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
    public void getTripsTowardsTest() throws UraClientException {
        // Mock the HTTP call.
        mockHttpToFile(1, "instant_V1_trips_towards.txt");

        /* List stops and verify some values */
        List<Trip> trips = new UraClient(httpMock.baseUrl()).towards("Marble Arch").getTrips();
        assertThat(trips, hasSize(10));

        mockHttpToFile(1, "instant_V1_trips_stop_towards.txt");
        trips = new UraClient(httpMock.baseUrl()).forStops("156").towards("Marble Arch").getTrips();
        assertThat(trips, hasSize(17));
        assertThat(trips.stream().filter(t -> !t.getStop().getId().equals("156")).findAny(), is(Optional.empty()));
    }

    @Test
    public void getTripsTest() throws UraClientException {
        // Mock the HTTP call.
        mockHttpToFile(1, "instant_V1_trips_all.txt");

        // Get trips without filters and verify some values.
        List<Trip> trips = new UraClient(httpMock.baseUrl()).getTrips();
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
        mockHttpToFile(2, "instant_V2_trips_all.txt");

        // Get trips without filters and verify some values.
        trips = new UraClient(httpMock.baseUrl(), "/interfaces/ura/instant_V2", "/interfaces/ura/stream")
                .getTrips();
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
        mockHttpToFile(1, "instant_V1_trips_all.txt");
        trips = new UraClient(httpMock.baseUrl()).getTrips(5);
        assertThat(trips, hasSize(5));

        // Test mockException handling.
        mockHttpToError(502);
        try {
            new UraClient(httpMock.baseUrl()).getTrips();
        } catch (RuntimeException e) {
            assertThat(e, is(instanceOf(IllegalStateException.class)));
            assertThat(e.getCause(), is(instanceOf(IOException.class)));
            assertThat(e.getCause().getMessage(), startsWith("Server returned HTTP response code: 502 for URL"));
        }

        mockHttpToException();
        UraClientException exception = assertThrows(
                UraClientException.class,
                () -> new UraClient(httpMock.baseUrl()).getTrips(),
                "Expected reader to raise an exception"
        );
        assertEquals("Failed to read trips from API", exception.getMessage(), "Unexpected error message");
        assertTrue(exception.getCause() instanceof IOException, "Unexpected error cause");
    }

    @Test
    public void getTripsForStopTest() throws UraClientException {
        // Mock the HTTP call.
        mockHttpToFile(1, "instant_V1_trips_stop.txt");

        // Get trips for stop ID 100000 (Aachen Bushof) and verify some values.
        List<Trip> trips = new UraClient(httpMock.baseUrl())
                .forStops("100000")
                .getTrips();
        assertThat(trips, hasSize(10));
        assertThat(trips.stream().filter(t -> !t.getStop().getId().equals("100000")).findAny(), is(Optional.empty()));
        assertThat(trips.get(0).getId(), is("27000158010001"));
        assertThat(trips.get(1).getLineID(), is("7"));
        assertThat(trips.get(2).getLineName(), is("25"));
        assertThat(trips.get(3).getStop().getIndicator(), is("H.15"));

        // Get trips for stop name "Uniklinik" and verify some values.
        mockHttpToFile(1, "instant_V1_trips_stop_name.txt");
        trips = new UraClient(httpMock.baseUrl())
                .forStopsByName("Uniklinik")
                .getTrips();
        assertThat(trips, hasSize(10));
        assertThat(trips.stream().filter(t -> !t.getStop().getName().equals("Uniklinik")).findAny(),
                is(Optional.empty()));
        assertThat(trips.get(0).getId(), is("92000043013001"));
        assertThat(trips.get(1).getLineID(), is("5"));
        assertThat(trips.get(2).getVehicleID(), is("317"));
        assertThat(trips.get(3).getDirectionID(), is(1));

        mockHttpToException();
        UraClientException exception = assertThrows(
                UraClientException.class,
                () -> new UraClient(httpMock.baseUrl()).getStops(),
                "Expected reader to raise an exception"
        );
        assertEquals("Failed to read stops from API", exception.getMessage(), "Unexpected error message");
        assertTrue(exception.getCause() instanceof IOException, "Unexpected error cause");
    }

    @Test
    public void getTripsForLine() throws UraClientException {
        // Mock the HTTP call.
        mockHttpToFile(1, "instant_V1_trips_line.txt");

        // Get trips for line ID 3 and verify some values.
        List<Trip> trips = new UraClient(httpMock.baseUrl())
                .forLines("3")
                .getTrips();
        assertThat(trips, hasSize(10));
        assertThat(trips.stream().filter(t -> !t.getLineID().equals("3")).findAny(), is(Optional.empty()));
        assertThat(trips.get(0).getId(), is("27000154004001"));
        assertThat(trips.get(1).getLineID(), is("3"));
        assertThat(trips.get(2).getLineName(), is("3.A"));
        assertThat(trips.get(3).getStop().getIndicator(), is("H.4 (Pontwall)"));

        // Get trips for line name "3.A" and verify some values.
        mockHttpToFile(1, "instant_V1_trips_line_name.txt");
        trips = new UraClient(httpMock.baseUrl())
                .forLinesByName("3.A")
                .getTrips();
        assertThat(trips, hasSize(10));
        assertThat(trips.stream().filter(t -> !t.getLineName().equals("3.A")).findAny(), is(Optional.empty()));
        assertThat(trips.get(0).getId(), is("92000288014001"));
        assertThat(trips.get(1).getLineID(), is("3"));
        assertThat(trips.get(2).getLineName(), is("3.A"));
        assertThat(trips.get(3).getStop().getName(), is("Aachen Gartenstraße"));

        // Get trips for line 3 with direction 1 and verify some values.
        mockHttpToFile(1, "instant_V1_trips_line_direction.txt");
        trips = new UraClient(httpMock.baseUrl())
                .forLines("412")
                .forDirection(2)
                .getTrips();
        assertThat(trips, hasSize(10));
        assertThat(trips.stream().filter(t -> !t.getLineID().equals("412")).findAny(), is(Optional.empty()));
        assertThat(trips.stream().filter(t -> !t.getDirectionID().equals(2)).findAny(), is(Optional.empty()));

        // Test lineID and direction in different order.
        mockHttpToFile(1, "instant_V1_trips_line_direction.txt");
        trips = new UraClient(httpMock.baseUrl())
                .forDirection(2)
                .forLines("412")
                .getTrips();
        assertThat(trips, hasSize(10));
        assertThat(trips.stream().filter(t -> !t.getLineID().equals("412")).findAny(), is(Optional.empty()));
        assertThat(trips.stream().filter(t -> !t.getDirectionID().equals(2)).findAny(), is(Optional.empty()));
    }

    @Test
    public void getTripsForStopAndLine() throws UraClientException {
        // Mock the HTTP call.
        mockHttpToFile(1, "instant_V1_trips_stop_line.txt");

        // Get trips for line ID 25 and 25 at stop 100000 and verify some values.
        List<Trip> trips = new UraClient(httpMock.baseUrl())
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


    @Test
    public void getMessages() throws UraClientException {
        // Mock the HTTP call.
        mockHttpToFile(1, "instant_V1_messages.txt");

        // Get messages without filter and verify some values.
        List<Message> messages = new UraClient(httpMock.baseUrl())
                .getMessages();
        assertThat(messages, hasSize(2));
        assertThat(messages.get(0).getStop().getId(), is("100707"));
        assertThat(messages.get(0).getUuid(), is("016e1231d4e30014_100707"));
        assertThat(messages.get(1).getStop().getName(), is("Herzogenr. Rathaus"));
        assertThat(messages.get(1).getUuid(), is("016e2cc3a3750006_210511"));
        assertThat(messages.get(0).getType(), is(0));
        assertThat(messages.get(1).getPriority(), is(0));
        assertThat(messages.get(0).getText(), is("Sehr geehrte Fahrgäste, wegen Strassenbauarbeiten kann diese Haltestelle nicht von den Bussen der Linien 17, 44 und N2 angefahren werden."));
        assertThat(messages.get(1).getText(), is("Sehr geehrte Fahrgäste, diese Haltestelle wird vorübergehend von den Linien 47, 147 und N3 nicht angefahren."));

        mockHttpToException();
        UraClientException exception = assertThrows(
                UraClientException.class,
                () -> new UraClient(httpMock.baseUrl()).getMessages(),
                "Expected reader to raise an exception"
        );
        assertEquals("Failed to read messages from API", exception.getMessage(), "Unexpected error message");
        assertTrue(exception.getCause() instanceof IOException, "Unexpected error cause");
    }

    @Test
    public void getMessagesForStop() throws UraClientException {
        // Mock the HTTP call.
        mockHttpToFile(2, "instant_V2_messages_stop.txt");

        // Get trips for stop ID 100707 (Berensberger Str.) and verify some values.
        List<Message> messages = new UraClient(httpMock.baseUrl(), "/interfaces/ura/instant_V2", "/interfaces/ura/stream")
                .forStops("100707")
                .getMessages();
        assertThat(messages, hasSize(1));
        assertThat(messages.stream().filter(t -> !t.getStop().getId().equals("100707")).findAny(), is(Optional.empty()));
        assertThat(messages.get(0).getUuid(), is("016e1231d4e30014_100707"));
        assertThat(messages.get(0).getType(), is(0));
        assertThat(messages.get(0).getPriority(), is(3));
        assertThat(messages.get(0).getText(), is("Sehr geehrte Fahrgäste, wegen Strassenbauarbeiten kann diese Haltestelle nicht von den Bussen der Linien 17, 44 und N2 angefahren werden."));
    }

    @Test
    public void timeoutTest() throws IOException {
        // Try to read trips from TEST-NET-1 IP that is not routed (hopefully) and will not connect within 100ms.
        UraClientException exception = assertThrows(
                UraClientException.class,
                () -> new UraClient(
                        UraClientConfiguration.forBaseURL("http://192.0.2.1")
                                .withConnectTimeout(Duration.ofMillis(100))
                                .build()
                ).forDestinationNames("Piccadilly Circus").getTrips(),
                "Connection to TEST-NET-1 address should fail"
        );
        assertTrue(exception.getCause() instanceof HttpConnectTimeoutException, "Exception cause is not HttpConnectionTimeoutException");

        // Mock the HTTP call with delay of 200ms, but immediate connection.
        WireMock.stubFor(
                get(urlPathEqualTo("/interfaces/ura/instant_V1")).willReturn(
                        aResponse().withFixedDelay(200).withBodyFile("instant_V1_trips_destination.txt")
                )
        );
        assertDoesNotThrow(
                () -> new UraClient(
                        UraClientConfiguration.forBaseURL(httpMock.baseUrl())
                                .withConnectTimeout(Duration.ofMillis(100))
                                .build()
                ).forDestinationNames("Piccadilly Circus").getTrips(),
                "Connection timeout should not affect response time."
        );

        // Now specify response timeout.
        exception = assertThrows(
                UraClientException.class,
                () -> new UraClient(
                        UraClientConfiguration.forBaseURL(httpMock.baseUrl())
                                .withTimeout(Duration.ofMillis(100))
                                .build()
                ).forDestinationNames("Piccadilly Circus").getTrips(),
                "Response timeout did not raise an exception"
        );
        assertTrue(exception.getCause() instanceof HttpTimeoutException, "Exception cause is not HttpTimeoutException");

        assertDoesNotThrow(
                () -> new UraClient(
                        UraClientConfiguration.forBaseURL(httpMock.baseUrl())
                                .withTimeout(Duration.ofMillis(300))
                                .build()
                ).forDestinationNames("Piccadilly Circus").getTrips(),
                "Response timeout of 300ms with 100ms delay must not fail"
        );
    }


    private static void mockHttpToFile(int version, String resourceFile) {
        WireMock.stubFor(
                get(urlPathEqualTo("/interfaces/ura/instant_V" + version)).willReturn(
                        aResponse().withBodyFile(resourceFile)
                )
        );
    }

    private static void mockHttpToError(int code) {
        WireMock.stubFor(
                get(anyUrl()).willReturn(
                        aResponse().withStatus(code)
                )
        );
    }

    private static void mockHttpToException() {
        WireMock.stubFor(
                get(anyUrl()).willReturn(
                        aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK)
                )
        );
    }
}
