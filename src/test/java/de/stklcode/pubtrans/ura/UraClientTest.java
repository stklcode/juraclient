/*
 * Copyright 2016-2026 Stefan Kalscheuer
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

import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import de.stklcode.pubtrans.ura.exception.UraClientException;
import de.stklcode.pubtrans.ura.model.Message;
import de.stklcode.pubtrans.ura.model.Stop;
import de.stklcode.pubtrans.ura.model.Trip;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for the URA Client.
 * Tests run against mocked data collected from ASEAG API (no longer available) and
 * TFL API (<a href="https://countdown.api.tfl.gov.uk">https://countdown.api.tfl.gov.uk</a>)
 *
 * @author Stefan Kalscheuer
 */
class UraClientTest {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
        .options(wireMockConfig().dynamicPort())
        .build();

    @Test
    void getStopsTest() throws UraClientException {
        // Mock the HTTP call.
        mockHttpToFile(2, "instant_V2_stops.txt");

        // List stops and verify some values.
        List<Stop> stops = new UraClient(wireMock.baseUrl(), "/interfaces/ura/instant_V2", "/interfaces/ura/stream").getStops();
        assertEquals(10, stops.size());
        assertEquals("100210", stops.get(0).id());
        assertEquals("Brockenberg", stops.get(1).name());
        assertEquals(0, stops.get(2).state());
        assertEquals(50.7578775, stops.get(3).latitude());
        assertEquals(6.0708663, stops.get(4).longitude());

        // Test Exception handling.
        mockHttpToError(500);

        var uraClient = new UraClient(wireMock.baseUrl());
        Exception e = assertThrows(UraClientException.class, uraClient::getStops);
        assertEquals("Failed to read stops from API", e.getMessage());
        e = assertInstanceOf(IOException.class, e.getCause());
        assertEquals("API request failed with status 500", e.getMessage());
    }

    @Test
    void getStopsForLineTest() throws UraClientException {
        // Mock the HTTP call.
        mockHttpToFile(2, "instant_V2_stops_line.txt");

        // List stops and verify some values.
        List<Stop> stops = new UraClient(wireMock.baseUrl(), "/interfaces/ura/instant_V2", "/interfaces/ura/stream")
            .forLines("33")
            .getStops();
        assertEquals(47, stops.size());
        assertEquals("100000", stops.get(0).id());
        assertEquals("Kuckelkorn", stops.get(1).name());
        assertEquals(0, stops.get(2).state());
        assertEquals(50.7690688, stops.get(3).latitude());
        assertEquals("H.1", stops.get(4).indicator());
        assertEquals(6.2314072, stops.get(5).longitude());
    }

    @Test
    void getStopsForPositionTest() throws UraClientException {
        // Mock the HTTP call.
        mockHttpToFile(1, "instant_V1_stops_circle.txt");

        // List stops and verify some values.
        List<Stop> stops = new UraClient(wireMock.baseUrl())
            .forPosition(51.51009, -0.1345734, 200)
            .getStops();
        assertEquals(13, stops.size());
        assertEquals("156", stops.get(0).id());
        assertEquals("Piccadilly Circus", stops.get(1).name());
        assertEquals(0, stops.get(2).state());
        assertEquals(51.509154, stops.get(3).latitude());
        assertEquals(-0.134172, stops.get(4).longitude());
        assertNull(stops.get(5).indicator());

        mockHttpToFile(1, "instant_V1_stops_circle_name.txt");
        stops = new UraClient(wireMock.baseUrl())
            .forStopsByName("Piccadilly Circus")
            .forPosition(51.51009, -0.1345734, 200)
            .getStops();
        assertEquals(7, stops.size());
        assertEquals(Optional.empty(), stops.stream().filter(t -> !t.name().equals("Piccadilly Circus")).findAny());
    }

    @Test
    void getTripsForDestinationNamesTest() throws UraClientException {
        // Mock the HTTP call.
        mockHttpToFile(1, "instant_V1_trips_destination.txt");

        // List stops and verify some values.
        List<Trip> trips = new UraClient(wireMock.baseUrl()).forDestinationNames("Piccadilly Circus").getTrips();
        assertEquals(9, trips.size());
        assertEquals(Optional.empty(), trips.stream().filter(t -> !t.destinationName().equals("Piccadilly Cir")).findAny());

        mockHttpToFile(1, "instant_V1_trips_stop_destination.txt");
        trips = new UraClient(wireMock.baseUrl())
            .forStops("156")
            .forDestinationNames("Marble Arch")
            .getTrips();
        assertEquals(5, trips.size());
        assertEquals(Optional.empty(), trips.stream().filter(t -> !t.stop().id().equals("156")).findAny());
        assertEquals(Optional.empty(), trips.stream().filter(t -> !t.destinationName().equals("Marble Arch")).findAny());
    }

    @Test
    void getTripsTowardsTest() throws UraClientException {
        // Mock the HTTP call.
        mockHttpToFile(1, "instant_V1_trips_towards.txt");

        /* List stops and verify some values */
        List<Trip> trips = new UraClient(wireMock.baseUrl()).towards("Marble Arch").getTrips();
        assertEquals(10, trips.size());

        mockHttpToFile(1, "instant_V1_trips_stop_towards.txt");
        trips = new UraClient(wireMock.baseUrl()).forStops("156").towards("Marble Arch").getTrips();
        assertEquals(17, trips.size());
        assertEquals(Optional.empty(), trips.stream().filter(t -> !t.stop().id().equals("156")).findAny());
    }

    @Test
    void getTripsTest() throws UraClientException {
        // Mock the HTTP call.
        mockHttpToFile(1, "instant_V1_trips_all.txt");

        // Get trips without filters and verify some values.
        List<Trip> trips = new UraClient(wireMock.baseUrl()).getTrips();
        assertEquals(10, trips.size());
        assertEquals("27000165015001", trips.get(0).id());
        assertEquals("55", trips.get(1).lineID());
        assertEquals("28", trips.get(2).lineName());
        assertEquals(1, trips.get(3).directionID());
        assertEquals("Verlautenheide Endstr.", trips.get(4).destinationName());
        assertEquals("Aachen Bushof", trips.get(5).destinationText());
        assertEquals("247", trips.get(6).vehicleID());
        assertEquals(1482854580000L, trips.get(7).estimatedTime());
        assertEquals(30, trips.get(8).visitID());
        assertEquals("100002", trips.get(9).stop().id());

        // With limit.
        trips = new UraClient(wireMock.baseUrl()).getTrips(5);
        assertEquals(5, trips.size());
        trips = new UraClient(wireMock.baseUrl()).getTrips(11);
        assertEquals(10, trips.size());

        // Repeat test for API V2.
        mockHttpToFile(2, "instant_V2_trips_all.txt");

        // Get trips without filters and verify some values.
        trips = new UraClient(wireMock.baseUrl(), "/interfaces/ura/instant_V2", "/interfaces/ura/stream")
            .getTrips();
        assertEquals(10, trips.size());
        assertEquals("27000165015001", trips.get(0).id());
        assertEquals("55", trips.get(1).lineID());
        assertEquals("28", trips.get(2).lineName());
        assertEquals(1, trips.get(3).directionID());
        assertEquals("Verlautenheide Endstr.", trips.get(4).destinationName());
        assertEquals("Aachen Bushof", trips.get(5).destinationText());
        assertEquals("247", trips.get(6).vehicleID());
        assertEquals(1482854580000L, trips.get(7).estimatedTime());
        assertEquals(30, trips.get(8).visitID());
        assertEquals("100002", trips.get(9).stop().id());

        // Get limited number of trips.
        mockHttpToFile(1, "instant_V1_trips_all.txt");
        trips = new UraClient(wireMock.baseUrl()).getTrips(5);
        assertEquals(5, trips.size());

        // Test Exception handling.
        mockHttpToError(502);
        var uraClient = new UraClient(wireMock.baseUrl());
        Exception e = assertThrows(UraClientException.class, uraClient::getStops);
        assertEquals("Failed to read stops from API", e.getMessage());
        e = assertInstanceOf(IOException.class, e.getCause());
        assertEquals("API request failed with status 502", e.getMessage());

        mockHttpToException();
        e = assertThrows(
            UraClientException.class,
            () -> new UraClient(wireMock.baseUrl()).getTrips(),
            "Expected reader to raise an exception"
        );
        assertEquals("Failed to read trips from API", e.getMessage(), "Unexpected error message");
        assertInstanceOf(IOException.class, e.getCause(), "Unexpected error cause");
        assertEquals("Failed to read trips from API", e.getMessage(), "Unexpected error message");
        assertInstanceOf(IOException.class, e.getCause(), "Unexpected error cause");

    }

    @Test
    void getTripsForStopTest() throws UraClientException {
        // Mock the HTTP call.
        mockHttpToFile(1, "instant_V1_trips_stop.txt");

        // Get trips for stop ID 100000 (Aachen Bushof) and verify some values.
        List<Trip> trips = new UraClient(wireMock.baseUrl())
            .forStops("100000")
            .getTrips();
        assertEquals(10, trips.size());
        assertEquals(Optional.empty(), trips.stream().filter(t -> !t.stop().id().equals("100000")).findAny());
        assertEquals("27000158010001", trips.get(0).id());
        assertEquals("7", trips.get(1).lineID());
        assertEquals("25", trips.get(2).lineName());
        assertEquals("H.15", trips.get(3).stop().indicator());

        // With limit.
        trips = new UraClient(wireMock.baseUrl())
            .forStops("100000")
            .getTrips(7);
        assertEquals(7, trips.size());

        // Get trips for stop name "Uniklinik" and verify some values.
        mockHttpToFile(1, "instant_V1_trips_stop_name.txt");
        trips = new UraClient(wireMock.baseUrl())
            .forStopsByName("Uniklinik")
            .getTrips();
        assertEquals(10, trips.size());
        assertEquals(Optional.empty(), trips.stream().filter(t -> !t.stop().name().equals("Uniklinik")).findAny());
        assertEquals("92000043013001", trips.get(0).id());
        assertEquals("5", trips.get(1).lineID());
        assertEquals("317", trips.get(2).vehicleID());
        assertEquals(1, trips.get(3).directionID());

        mockHttpToException();
        UraClientException exception = assertThrows(
            UraClientException.class,
            () -> new UraClient(wireMock.baseUrl()).getStops(),
            "Expected reader to raise an exception"
        );
        assertEquals("Failed to read stops from API", exception.getMessage(), "Unexpected error message");
        assertInstanceOf(IOException.class, exception.getCause(), "Unexpected error cause");
    }

    @Test
    void getTripsForLine() throws UraClientException {
        // Mock the HTTP call.
        mockHttpToFile(1, "instant_V1_trips_line.txt");

        // Get trips for line ID 3 and verify some values.
        List<Trip> trips = new UraClient(wireMock.baseUrl())
            .forLines("3")
            .getTrips();
        assertEquals(10, trips.size());
        assertEquals(Optional.empty(), trips.stream().filter(t -> !t.lineID().equals("3")).findAny());
        assertEquals("27000154004001", trips.get(0).id());
        assertEquals("3", trips.get(1).lineID());
        assertEquals("3.A", trips.get(2).lineName());
        assertEquals("H.4 (Pontwall)", trips.get(3).stop().indicator());

        // Get trips for line name "3.A" and verify some values.
        mockHttpToFile(1, "instant_V1_trips_line_name.txt");
        trips = new UraClient(wireMock.baseUrl())
            .forLinesByName("3.A")
            .getTrips();
        assertEquals(10, trips.size());
        assertEquals(Optional.empty(), trips.stream().filter(t -> !t.lineName().equals("3.A")).findAny());
        assertEquals("92000288014001", trips.get(0).id());
        assertEquals("3", trips.get(1).lineID());
        assertEquals("3.A", trips.get(2).lineName());
        assertEquals("Aachen Gartenstraße", trips.get(3).stop().name());

        // Get trips for line 3 with direction 1 and verify some values.
        mockHttpToFile(1, "instant_V1_trips_line_direction.txt");
        trips = new UraClient(wireMock.baseUrl())
            .forLines("412")
            .forDirection(2)
            .getTrips();
        assertEquals(10, trips.size());
        assertEquals(Optional.empty(), trips.stream().filter(t -> !t.lineID().equals("412")).findAny());
        assertEquals(Optional.empty(), trips.stream().filter(t -> t.directionID() != 2).findAny());

        // Test lineID and direction in different order.
        mockHttpToFile(1, "instant_V1_trips_line_direction.txt");
        trips = new UraClient(wireMock.baseUrl())
            .forDirection(2)
            .forLines("412")
            .getTrips();
        assertEquals(10, trips.size());
        assertEquals(Optional.empty(), trips.stream().filter(t -> !t.lineID().equals("412")).findAny());
        assertEquals(Optional.empty(), trips.stream().filter(t -> t.directionID() != 2).findAny());
    }

    @Test
    void getTripsForStopAndLine() throws UraClientException {
        // Mock the HTTP call.
        mockHttpToFile(1, "instant_V1_trips_stop_line.txt");

        // Get trips for line ID 25 and 25 at stop 100000 and verify some values.
        List<Trip> trips = new UraClient(wireMock.baseUrl())
            .forLines("25", "35")
            .forStops("100000")
            .getTrips();
        assertEquals(10, trips.size());
        assertEquals(Optional.empty(), trips.stream().filter(t -> !t.lineID().equals("25") && !t.lineID().equals("35")).findAny());
        assertEquals(Optional.empty(), trips.stream().filter(t -> !t.stop().id().equals("100000")).findAny());
        assertEquals("27000078014001", trips.get(0).id());
        assertEquals("25", trips.get(1).lineID());
        assertEquals("35", trips.get(3).lineName());
        assertEquals("H.12", trips.get(5).stop().indicator());
    }


    @Test
    void getMessages() throws UraClientException {
        UraClient uraClient = new UraClient(wireMock.baseUrl());

        // Mock the HTTP call.
        mockHttpToFile(1, "instant_V1_messages.txt");

        // Get messages without filter and verify some values.
        List<Message> messages = uraClient.getMessages();
        assertEquals(2, messages.size());
        assertEquals("100707", messages.get(0).stop().id());
        assertEquals("016e1231d4e30014_100707", messages.get(0).uuid());
        assertEquals("Herzogenr. Rathaus", messages.get(1).stop().name());
        assertEquals("016e2cc3a3750006_210511", messages.get(1).uuid());
        assertEquals(0, messages.get(0).type());
        assertEquals(0, messages.get(1).priority());
        assertEquals("Sehr geehrte Fahrgäste, wegen Strassenbauarbeiten kann diese Haltestelle nicht von den Bussen der Linien 17, 44 und N2 angefahren werden.", messages.get(0).text());
        assertEquals("Sehr geehrte Fahrgäste, diese Haltestelle wird vorübergehend von den Linien 47, 147 und N3 nicht angefahren.", messages.get(1).text());

        // With limit.
        messages = uraClient.getMessages(1);
        assertEquals(1, messages.size());
        messages = uraClient.getMessages(3);
        assertEquals(2, messages.size());

        mockHttpToException();
        UraClientException exception = assertThrows(
            UraClientException.class,
            () -> new UraClient(wireMock.baseUrl()).getMessages(),
            "Expected reader to raise an exception"
        );
        assertEquals("Failed to read messages from API", exception.getMessage(), "Unexpected error message");
        assertInstanceOf(IOException.class, exception.getCause(), "Unexpected error cause");
    }

    @Test
    void getMessagesForStop() throws UraClientException {
        UraClient uraClient = new UraClient(wireMock.baseUrl(), "/interfaces/ura/instant_V2", "/interfaces/ura/stream");

        // Mock the HTTP call.
        mockHttpToFile(2, "instant_V2_messages_stop.txt");

        // Get trips for stop ID 100707 (Berensberger Str.) and verify some values.
        List<Message> messages = uraClient.forStops("100707").getMessages();
        assertEquals(1, messages.size());
        assertEquals(Optional.empty(), messages.stream().filter(t -> !t.stop().id().equals("100707")).findAny());
        assertEquals("016e1231d4e30014_100707", messages.get(0).uuid());
        assertEquals(0, messages.get(0).type());
        assertEquals(3, messages.get(0).priority());
        assertEquals("Sehr geehrte Fahrgäste, wegen Strassenbauarbeiten kann diese Haltestelle nicht von den Bussen der Linien 17, 44 und N2 angefahren werden.", messages.get(0).text());

        // With limit.
        messages = uraClient.forStops("100707").getMessages(0);
        assertEquals(0, messages.size());
        messages = uraClient.forStops("100707").getMessages(2);
        assertEquals(1, messages.size());
    }

    @Test
    void timeoutTest() {
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
        assertInstanceOf(HttpConnectTimeoutException.class, exception.getCause(), "Exception cause is not HttpConnectionTimeoutException");

        // Mock the HTTP call with delay of 200ms, but immediate connection.
        wireMock.stubFor(
            get(urlPathEqualTo("/interfaces/ura/instant_V1")).willReturn(
                aResponse().withFixedDelay(200).withBodyFile("instant_V1_trips_destination.txt")
            )
        );
        assertDoesNotThrow(
            () -> new UraClient(
                UraClientConfiguration.forBaseURL(wireMock.baseUrl())
                    .withConnectTimeout(Duration.ofMillis(100))
                    .build()
            ).forDestinationNames("Piccadilly Circus").getTrips(),
            "Connection timeout should not affect response time."
        );

        // Now specify response timeout.
        exception = assertThrows(
            UraClientException.class,
            () -> new UraClient(
                UraClientConfiguration.forBaseURL(wireMock.baseUrl())
                    .withTimeout(Duration.ofMillis(100))
                    .build()
            ).forDestinationNames("Piccadilly Circus").getTrips(),
            "Response timeout did not raise an exception"
        );
        assertInstanceOf(HttpTimeoutException.class, exception.getCause(), "Exception cause is not HttpTimeoutException");

        assertDoesNotThrow(
            () -> new UraClient(
                UraClientConfiguration.forBaseURL(wireMock.baseUrl())
                    .withTimeout(Duration.ofMillis(300))
                    .build()
            ).forDestinationNames("Piccadilly Circus").getTrips(),
            "Response timeout of 300ms with 100ms delay must not fail"
        );
    }

    private static void mockHttpToFile(int version, String resourceFile) {
        wireMock.stubFor(
            get(urlPathEqualTo("/interfaces/ura/instant_V" + version)).willReturn(
                aResponse().withBodyFile(resourceFile)
            )
        );
    }

    private static void mockHttpToError(int code) {
        wireMock.stubFor(
            get(anyUrl()).willReturn(
                aResponse().withStatus(code)
            )
        );
    }

    private static void mockHttpToException() {
        wireMock.stubFor(
            get(anyUrl()).willReturn(
                aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK)
            )
        );
    }
}
